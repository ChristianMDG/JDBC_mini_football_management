package org.mini_football_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRetriever {
    DBConnection dbConnection = new DBConnection();


    public Team findTeamById(Integer id) throws SQLException {
        Team team = null;

        String teamSql =
                "SELECT id, name, continent FROM team WHERE id = ?";

        String playerSql =
                "SELECT id, name, age, position, goal_nb FROM player WHERE id_team = ?";

        try (Connection conn = dbConnection.getConnection()) {

            try (PreparedStatement psTeam = conn.prepareStatement(teamSql)) {
                psTeam.setInt(1, id);
                ResultSet rsTeam = psTeam.executeQuery();
                if (!rsTeam.next()) return null;
                team = new Team();
                team.setId(rsTeam.getInt("id"));
                team.setName(rsTeam.getString("name"));
                team.setContinent(
                        ContinentEnum.valueOf(rsTeam.getString("continent"))
                );
                team.setPlayers(new ArrayList<>());
            }


            try (PreparedStatement psPlayer = conn.prepareStatement(playerSql)) {
                psPlayer.setInt(1, id);
                ResultSet rsPlayer = psPlayer.executeQuery();

                while (rsPlayer.next()) {
                    Player p = new Player();
                    p.setId(rsPlayer.getInt("id"));
                    p.setName(rsPlayer.getString("name"));
                    p.setAge(rsPlayer.getInt("age"));
                    p.setPosition(
                            PlayerPositionEnum.valueOf(rsPlayer.getString("position"))
                    );
                    p.setGoalNb(rsPlayer.getInt("goal_nb"));
                    team.getPlayers().add(p);
                }
            }
        }
        return team;
    }

    public List<Player> findPlayers(int page, int size) throws SQLException {
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        String playerSql =
                "SELECT id, name, age, position, goal_nb, id_team " +
                        "FROM player LIMIT ? OFFSET ?";

        String teamSql =
                "SELECT id, name, continent FROM team WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement psPlayer = conn.prepareStatement(playerSql);
             PreparedStatement psTeam = conn.prepareStatement(teamSql)) {

            psPlayer.setInt(1, size);
            psPlayer.setInt(2, offset);

            ResultSet rs = psPlayer.executeQuery();

            while (rs.next()) {
                Player p = new Player();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setPosition(
                        PlayerPositionEnum.valueOf(rs.getString("position"))
                );
                p.setGoalNb(rs.getInt("goal_nb"));

                int teamId = rs.getInt("id_team");
                if (!rs.wasNull()) {
                    psTeam.setInt(1, teamId);
                    ResultSet rsTeam = psTeam.executeQuery();

                    if (rsTeam.next()) {
                        Team t = new Team();
                        t.setId(rsTeam.getInt("id"));
                        t.setName(rsTeam.getString("name"));
                        t.setContinent(
                                ContinentEnum.valueOf(rsTeam.getString("continent"))
                        );
                        p.setTeam(t);
                    }
                }
                players.add(p);
            }
        }
        return players;
    }


    public List<Player> createPlayers(List<Player> players) throws SQLException {

        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("La liste des joueurs est vide");
        }

        String sql =
                "INSERT INTO player (id, name, age, position, goal_nb, id_team) " +
                        "VALUES (?, ?, ?, ?::enum_position, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);


            Set<Integer> ids = new HashSet<>();
            for (Player p : players) {
                if (!ids.add(p.getId())) {
                    throw new RuntimeException("ID dupliqué dans la liste : " + p.getId());
                }
            }


            try {
                for (Player p : players) {
                    ps.setInt(1, p.getId());
                    ps.setString(2, p.getName());
                    ps.setInt(3, p.getAge());
                    ps.setString(4, p.getPosition().name());
                    ps.setInt(5, p.getGoalNb());
                    ps.setObject(6, p.getTeam() != null ? p.getTeam().getId() : null);

                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Insertion annulée. Tous les joueurs ont été annulés.", e);
            }

            return players;
        }
    }

    public Team saveTeam(Team teamToSave) throws SQLException {
        String upsertTeamQuery =
                "INSERT INTO team (id, name, continent) " +
                        "VALUES (?, ?, ?::enum_continent) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "name = EXCLUDED.name, continent = EXCLUDED.continent";

        String clearPlayersQuery = "UPDATE player SET id_team = NULL WHERE id_team = ?";
        String assignPlayerQuery = "UPDATE player SET id_team = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);


            try (PreparedStatement ps = conn.prepareStatement(upsertTeamQuery)) {
                ps.setInt(1, teamToSave.getId());
                ps.setString(2, teamToSave.getName());
                ps.setString(3, teamToSave.getContinent().name());
                ps.executeUpdate();
            }

            // 🔹 Gestion des joueurs
            if (teamToSave.getPlayers() != null) {
                if (teamToSave.getPlayers().isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(clearPlayersQuery)) {
                        ps.setInt(1, teamToSave.getId());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(assignPlayerQuery)) {
                        for (Player player : teamToSave.getPlayers()) {
                            ps.setInt(1, teamToSave.getId());
                            ps.setInt(2, player.getId());
                            ps.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return teamToSave;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de l'équipe", e);
        }
    }

    public List<Team> findTeamsByPlayerName(String playerName)  throws SQLException {

        List<Team> teams = new ArrayList<>();
        StringBuilder findTeamsByPlayerNameQuery = new StringBuilder("""
               select team.id as teamId, team.name as teamName, team.continent as continent ,player.name as playerName from  team
               left join player on player.id_team = team.id
               where 1 = 1
               """);
        List<Object> parameters = new ArrayList<>();

        if (playerName != null) {
            findTeamsByPlayerNameQuery.append("and player.name ilike ? ");
            parameters.add("%" + playerName + "%");
        }
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(findTeamsByPlayerNameQuery.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                if (rs.getInt("teamId") != 0) {
                    Team team = new Team();
                    team.setId(rs.getInt("teamId"));
                    team.setName(rs.getString("teamName"));
                    team.setContinent(ContinentEnum.valueOf(rs.getString("continent")));
                    teams.add(team);
                }
            }

        }
        return teams;
    }

    public List<Player> findPlayersByCriteria(
            String playerName,
            PlayerPositionEnum position,
            String teamName,
            ContinentEnum continent,
            int page,
            int size
    ) throws SQLException {

        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;

        // 🔹 SQL simple, lisible
        StringBuilder sql = new StringBuilder(
                "SELECT p.id AS player_id, p.name AS player_name, p.age, p.position, p.goal_nb, " +
                        "p.id_team AS team_id " +
                        "FROM player p " +
                        "LEFT JOIN team t ON p.id_team = t.id " +
                        "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (playerName != null && !playerName.isBlank()) {
            sql.append("AND p.name ILIKE ? ");
            params.add("%" + playerName + "%");
        }
        if (position != null) {
            sql.append("AND p.position = ?::enum_position ");
            params.add(position.name());
        }
        if (teamName != null && !teamName.isBlank()) {
            sql.append("AND t.name ILIKE ? ");
            params.add("%" + teamName + "%");
        }
        if (continent != null) {
            sql.append("AND t.continent = ?::enum_continent ");
            params.add(continent.name());
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {


            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Player p = new Player();
                p.setId(rs.getInt("player_id"));
                p.setName(rs.getString("player_name"));
                p.setAge(rs.getInt("age"));
                p.setPosition(PlayerPositionEnum.valueOf(rs.getString("position")));
                p.setGoalNb(rs.getInt("goal_nb"));
                players.add(p);
            }
        }

        return players;
    }

}
