package org.mini_football_management;

import javax.swing.text.Position;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException{
        System.out.println("Hello and welcome Chris!");

        DataRetriever dataRetriever = new DataRetriever();
        System.out.println("Test a:");
        System.out.println(dataRetriever.findTeamById(1));

        System.out.println("\nTest b:");
        System.out.println(dataRetriever.findTeamById(5));

        System.out.println("\nTest c:");
        System.out.println(dataRetriever.findPlayers(1,2));

        System.out.println("\nTest d:");
        System.out.println(dataRetriever.findPlayers(3,5));


        System.out.println("\nTest f:");
        System.out.println(dataRetriever.findPlayersByCriteria("ud",PlayerPositionEnum.MIDF,"Madrid",ContinentEnum.EUROPA,1,10));

        System.out.println("\nTest g:");

        List<Player> testPlayers = new ArrayList<>();
        testPlayers.add(new Player(6, "Jude Bellingham", 23, PlayerPositionEnum.STR, null));
        testPlayers.add(new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null));
        System.out.println(dataRetriever.createPlayers(testPlayers));


        System.out.println("\nTest h:");
        List<Player> newPlayers = new ArrayList<>();
        newPlayers.add(new Player(6, "Vini", 25, PlayerPositionEnum.STR, null));
        newPlayers.add(new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null));
        System.out.println(dataRetriever.createPlayers(newPlayers));

        System.out.println("\nTest i:");
        Team rm = dataRetriever.findTeamById(1);
        Player vini = new Player(6, "Vini", 25, PlayerPositionEnum.STR, rm);
        rm.getPlayers().add(vini);
        System.out.println(dataRetriever.saveTeam(rm));

        System.out.println("\nTest j:");
        Team barca = dataRetriever.findTeamById(2);
        barca.setPlayers(new ArrayList<>());
        System.out.println(dataRetriever.saveTeam(barca));

    }
}