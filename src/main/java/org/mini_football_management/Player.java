package org.mini_football_management;

import javax.swing.text.Position;
import java.util.Objects;

public class Player {
    private int id;
    private String name;
    private int age;
    private Position PlayerPositionEnum;
    private Team team;

    public Player(int id, String name, int age, Position playerPositionEnum, Team team) {
        this.id = id;
        this.name = name;
        this.age = age;
        PlayerPositionEnum = playerPositionEnum;
        this.team = team;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Position getPlayerPositionEnum() {
        return PlayerPositionEnum;
    }

    public void setPlayerPositionEnum(Position playerPositionEnum) {
        PlayerPositionEnum = playerPositionEnum;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getTeamName() {
        return team.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id && age == player.age && Objects.equals(name, player.name) && Objects.equals(PlayerPositionEnum, player.PlayerPositionEnum) && Objects.equals(team, player.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, PlayerPositionEnum, team);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", PlayerPositionEnum=" + PlayerPositionEnum +
                ", team=" + team +
                '}';
    }
}
