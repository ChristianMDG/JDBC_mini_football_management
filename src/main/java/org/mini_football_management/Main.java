package org.mini_football_management;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        System.out.println("Hello and welcome Chris!");

        DataRetriever dataRetriever = new DataRetriever();
        System.out.println("Test a:");
        System.out.println(dataRetriever.findTeamById(1));

        System.out.println("\nTest b:");
        System.out.println(dataRetriever.findTeamById(5));

        System.out.println("\nTest c:");
        System.out.println(dataRetriever.findPlayers(1, 2));

        System.out.println("\nTest d:");
        System.out.println(dataRetriever.findPlayers(3, 5));

        System.out.println("\nTest e:");
        System.out.println(dataRetriever.findTeamsByPlayerName("an"));

        System.out.println("\nTest f:");
        System.out.println(dataRetriever.findPlayersByCriteria("ud", null, null, null, 1, 10));

        System.out.println("\nTest g:");

        List<Player> addPlayers = new ArrayList<>();
        addPlayers.add(new Player(6, "Jude Bellingham", 23, PlayerPositionEnum.STR, null));
        addPlayers.add(new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null));
        System.out.println(dataRetriever.createPlayers(addPlayers));


        System.out.println("\nTest h:");
        List<Player> newPlayers = new ArrayList<>();
        newPlayers.add(new Player(6, "Vini", 25, PlayerPositionEnum.STR, null));
        newPlayers.add(new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null));
        System.out.println(dataRetriever.createPlayers(newPlayers));

        System.out.println("\nTest i:");
        Team realDeMadrid = dataRetriever.findTeamById(1);
        Player vini = new Player(6, "Vini", 25, PlayerPositionEnum.STR, 5);
        Player pedri = new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null);
        realDeMadrid.getPlayers().add(vini);
        realDeMadrid.getPlayers().add(pedri);
        Team savedTeam = dataRetriever.saveTeam(realDeMadrid);
        System.out.println("Team saved: " + savedTeam.getName() + " with " + savedTeam.getPlayers());


        System.out.println("\nTest j:");
        Team barca = dataRetriever.findTeamById(2);
        barca.setPlayers(new ArrayList<>());
        System.out.println(dataRetriever.saveTeam(barca));

        System.out.println("******************** Test findTeamByID *******************\n");
        Team team = dataRetriever.findTeamById(1);
        System.out.println("Team retrieved:");
        System.out.println(team);
        System.out.print("Calculate players goals: ");
        Integer goals = team.getPlayersGoals();
        System.out.println(goals);

        System.out.println("\n-----------------------------------------------------\n");
        System.out.println("************ Save Team ****************");
        Team rm = dataRetriever.findTeamById(1);
        Player vinicus = new Player(6, "Vini", 25, PlayerPositionEnum.STR, 5,null);
        Player pedrigal = new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null,null);
        rm.getPlayers().add(vinicus);
        rm.getPlayers().add(pedrigal);
        Team savedTeams = dataRetriever.saveTeam(rm);
        System.out.println("Team saved: " + savedTeams.getName() + " :" + savedTeams.getPlayers());

    }
}