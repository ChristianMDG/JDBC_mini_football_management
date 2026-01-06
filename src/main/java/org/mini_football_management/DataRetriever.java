package org.mini_football_management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    DBConnection dbConnection = new DBConnection();

    public Team findTeamById(Integer id) throws SQLException {
        Team team = null;
        List<Player> players = new ArrayList<>();
        String findTeamByIdQuery = """
                select team.id as teamId, team.name as teamName, team.continent as continent , player.id as playerId, player.name as playerName,player.age as age,player.position as position from team
                left join player on team.id = player.id_team
                where team.id = ?""";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement findTeamByIdStatement = connection.prepareStatement(findTeamByIdQuery)) {
            findTeamByIdStatement.setInt(1, id);
            ResultSet resultSet = findTeamByIdStatement.executeQuery();
            while (resultSet.next()) {
                if (team == null) {
                    team = new Team();
                    team.setId(resultSet.getInt("teamId"));
                    team.setName(resultSet.getString("teamName"));
                    team.setContinent(ContinentEnum.valueOf(resultSet.getString("continent")));

                    team.setPlayers(players);
                }
                if (resultSet.getInt("playerId") != 0) {
                    Player player = new Player();
                    player.setId(resultSet.getInt("playerId"));
                    player.setName(resultSet.getString("playerName"));
                    player.setAge(resultSet.getInt("age"));
                    player.setPosition(PlayerPositionEnum.valueOf(resultSet.getString("position")));
                    players.add(player);
                }
            }

        }
        return team;
    }

    public List<Player> findPlayers(int page, int size) throws SQLException {
        List<Player> players = new ArrayList<>();
        int offset = (page - 1) * size;
        String findPlayerQuery = """
                select player.id as playerId, player.name as playerName, player.age as age , player.position as position , team.name as team , team.continent as continent\s
                from player left join team on player.id_team = team.id
                limit ? offset ?
                """;
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement findPlayerStatement = connection.prepareStatement(findPlayerQuery)) {
            findPlayerStatement.setInt(1, size);
            findPlayerStatement.setInt(2, offset);
            ResultSet resultSet = findPlayerStatement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getInt("playerId") != 0) {
                    Player player = new Player();
                    player.setId(resultSet.getInt("playerId"));
                    player.setName(resultSet.getString("playerName"));
                    player.setAge(resultSet.getInt("age"));
                    player.setPosition(PlayerPositionEnum.valueOf(resultSet.getString("position")));
                    players.add(player);
                }
            }

        }

        return players;
    }

    public List<Player> createPlayers(List<Player> newPlayers) throws SQLException {

        String insertPlayer = """
        insert into player(id, name, age, position, id_team)
        values (?, ?, ?, ?::enum_position, ?)
    """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertPlayer)) {


            connection.setAutoCommit(false);


            for (int i = 0; i < newPlayers.size(); i++) {
                Player p1 = newPlayers.get(i);

                for (int j = i + 1; j < newPlayers.size(); j++) {
                    Player p2 = newPlayers.get(j);

                    if (p1.getId() == p2.getId()) {
                        throw new RuntimeException(
                                "doublon d'id dans la liste : " + p1.getId()
                        );
                    }

                    if (p1.getName().equalsIgnoreCase(p2.getName())) {
                        throw new RuntimeException(
                                "doublon de nom dans la liste : " + p1.getName()
                        );
                    }
                }
            }

            try {
                for (Player player : newPlayers) {
                    insertStatement.setInt(1, player.getId());
                    insertStatement.setString(2, player.getName());
                    insertStatement.setInt(3, player.getAge());
                    insertStatement.setString(4, player.getPosition().name());
                    insertStatement.setObject(5, null);

                    insertStatement.executeUpdate();
                }


                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException(
                        "opération annulée : atomicité non respectée", e
                );
            }
        }

        return newPlayers;
    }

    public Team saveTeam(Team teamToSave) throws SQLException {

        String isTeamByIdQuery = "select id from team where id = ?";
        String isTeamByNameQuery = "select id from team where name = ? and id <> ?";
        String insertTeamQuery = "insert into team (id, name, continent) values (?, ?, ?::enum_continent)";
        String updateTeamQuery = "update team set name = ?, continent = ?::enum_continent where id = ?";
        String deletePlayersFromTeamQuery = "update player set id_team = null where id_team = ?";
        String addPlayerToTeamQuery = "update player set id_team = ? where id = ?";

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            boolean teamExists;
            try (PreparedStatement statement = connection.prepareStatement(isTeamByIdQuery)) {
                statement.setInt(1, teamToSave.getId());
                teamExists = statement.executeQuery().next();
            }

            try (PreparedStatement stmt = connection.prepareStatement(isTeamByNameQuery)) {
                stmt.setString(1, teamToSave.getName());
                stmt.setInt(2, teamToSave.getId());
                if (stmt.executeQuery().next()) {
                    throw new RuntimeException(
                            "une autre équipe existe déjà avec le nom : " + teamToSave.getName()
                    );
                }
            }

            if (teamExists) {
                try (PreparedStatement stmt = connection.prepareStatement(updateTeamQuery)) {
                    stmt.setString(1, teamToSave.getName());
                    stmt.setString(2, teamToSave.getContinent().name());
                    stmt.setInt(3, teamToSave.getId());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = connection.prepareStatement(insertTeamQuery)) {
                    stmt.setInt(1, teamToSave.getId());
                    stmt.setString(2, teamToSave.getName());
                    stmt.setString(3, teamToSave.getContinent().name());
                    stmt.executeUpdate();
                }
            }

            if (teamToSave.getPlayers() != null) {
                if (teamToSave.getPlayers().isEmpty()) {
                    try (PreparedStatement stmt = connection.prepareStatement(deletePlayersFromTeamQuery)) {
                        stmt.setInt(1, teamToSave.getId());
                        stmt.executeUpdate();
                    }
                }
                else {
                    try (PreparedStatement stmt = connection.prepareStatement(addPlayerToTeamQuery)) {
                        for (Player player : teamToSave.getPlayers()) {
                            stmt.setInt(1, teamToSave.getId());
                            stmt.setInt(2, player.getId());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
            }

            connection.commit();
            return teamToSave;

        } catch (Exception e) {
            throw new RuntimeException("échec de la sauvegarde de l'équipe", e);
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

    public List<Player> findPlayersByCriteria(String playerName, PlayerPositionEnum position, String teamName,
                                              ContinentEnum continent, int page, int size
    ) throws SQLException {
        List<Player> players = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                select player.id as player_id, player.name as player_name, player.age as age, player.position as position,
                team.name as team from  player left join team on player.id_team = team.id
                where 1 = 1
               """);

        List<Object> parameters = new ArrayList<>();

        if (playerName != null) {
            sql.append("and player.name ilike ? ");
            parameters.add("%" + playerName + "%");
        }
        if (position != null) {
            sql.append("and player.position = ?::enum_position ");
            parameters.add(position.name());
        }
        if (teamName != null && !teamName.isBlank()) {
            sql.append("and team.name ilike  ? ");
            parameters.add("%"+teamName+"%");
        }
        if (continent != null) {
            sql.append("and team.continent = ?::enum_continent ");
            parameters.add(continent.name());
        }
        sql.append("limit ? offset ?");

        int offset = (page - 1) * size;
        parameters.add(size);
        parameters.add(offset);

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                    Player player = new Player();
                    player.setId(resultSet.getInt("player_id"));
                    player.setName(resultSet.getString("player_name"));
                    player.setAge(resultSet.getInt("age"));
                    player.setPosition(PlayerPositionEnum.valueOf(resultSet.getString("position")));
                    players.add(player);

            }
        }
        return players;
    }

}
