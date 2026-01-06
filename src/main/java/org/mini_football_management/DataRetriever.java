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
        String createPlayer = """
              
                insert into Player(id,name,age,position,id_team) values(?,?,?,?::enum_position,?)
              """;
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement createPlayerStatement = connection.prepareStatement(createPlayer)) {

            connection.setAutoCommit(false);

            for (int i = 0; i < newPlayers.size(); i++) {
                for (int j = i + 1; j < newPlayers.size(); j++) {
                    if (newPlayers.get(i).getId() == newPlayers.get(j).getId()) {
                        throw new RuntimeException("Doublon d'ID dans la liste : " + newPlayers.get(i).getId());
                    }
                }
            }
            try {
                for (Player player : newPlayers) {
                    createPlayerStatement.setInt(1, player.getId());
                    createPlayerStatement.setString(2, player.getName());
                    createPlayerStatement.setInt(3, player.getAge());
                    createPlayerStatement.setString(4, player.getPosition().name());
                    createPlayerStatement.executeUpdate();
                }


                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Échec de l'insertion, transaction annulée : " + e.getMessage(), e);
            }
        }

        return newPlayers;
    }

    public Team saveTeam(Team teamToSave) throws SQLException {
        String verifyTeamByIdQuery = "SELECT id FROM team WHERE id = ?";
        String verifyTeamByNameQuery = "SELECT id FROM team WHERE name = ? AND id <> ?";
        String insertTeamQuery = "INSERT INTO team (id, name, continent) VALUES (?, ?, ?::enum_continent)";
        String updateTeamQuery = "UPDATE team SET name = ?, continent = ?::enum_continent WHERE id = ?";
        String deletePlayerQuery = "UPDATE player SET id_team = NULL WHERE id_team = ?";
        String insertPlayerQuery = "UPDATE player SET id_team = ? WHERE id = ?";

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);


            boolean teamExists;
            try (PreparedStatement verifyTeamByIdStmt = connection.prepareStatement(verifyTeamByIdQuery)) {
                verifyTeamByIdStmt.setInt(1, teamToSave.getId());
                try (ResultSet rs = verifyTeamByIdStmt.executeQuery()) {
                    teamExists = rs.next();
                }
            }


            try (PreparedStatement verifyTeamByNameStmt = connection.prepareStatement(verifyTeamByNameQuery)) {
                verifyTeamByNameStmt.setString(1, teamToSave.getName());
                verifyTeamByNameStmt.setInt(2, teamToSave.getId());
                try (ResultSet rs = verifyTeamByNameStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new RuntimeException("Une autre équipe existe déjà avec le nom : " + teamToSave.getName());
                    }
                }
            }


            if (teamExists) {
                try (PreparedStatement updateTeamStmt = connection.prepareStatement(updateTeamQuery)) {
                    updateTeamStmt.setString(1, teamToSave.getName());
                    updateTeamStmt.setString(2, teamToSave.getContinent().name());
                    updateTeamStmt.setInt(3, teamToSave.getId());
                    updateTeamStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertTeamStmt = connection.prepareStatement(insertTeamQuery)) {
                    insertTeamStmt.setInt(1, teamToSave.getId());
                    insertTeamStmt.setString(2, teamToSave.getName());
                    insertTeamStmt.setString(3, teamToSave.getContinent().name());
                    insertTeamStmt.executeUpdate();
                }
            }


            try (PreparedStatement deletePlayerStmt = connection.prepareStatement(deletePlayerQuery)) {
                deletePlayerStmt.setInt(1, teamToSave.getId());
                deletePlayerStmt.executeUpdate();
            }


            if (teamToSave.getPlayers() != null) {
                try (PreparedStatement insertPlayerStmt = connection.prepareStatement(insertPlayerQuery)) {
                    for (Player player : teamToSave.getPlayers()) {
                        insertPlayerStmt.setInt(1, teamToSave.getId());
                        insertPlayerStmt.setInt(2, player.getId());
                        insertPlayerStmt.addBatch();
                    }
                    insertPlayerStmt.executeBatch();
                }
            }


            connection.commit();
            System.out.println("Équipe sauvegardée avec succès : " + teamToSave.getName());

        } catch (SQLException e) {
            throw new RuntimeException("Échec de la sauvegarde de l'équipe : " + e.getMessage(), e);
        }

        return teamToSave;
    }

    public List<Team> findTeamsByPlayerName(String playerName) throws SQLException{
        throw new RuntimeException("Not Implemented");
    }

    public List<Player>
    findPlayersByCriteria(String playerName, PlayerPositionEnum position, String teamName, ContinentEnum continent, int page, int size) throws SQLException{
        throw new RuntimeException("Not Implemented");
    }
}
