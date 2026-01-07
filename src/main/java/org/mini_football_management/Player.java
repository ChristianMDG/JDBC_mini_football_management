package org.mini_football_management;

import javax.swing.text.Position;
import java.util.Objects;

public class Player {
    private int id;
    private String name;
    private int age;
    private  PlayerPositionEnum position;
    private Integer goalNb;
    private Team team;

    public Player(){}
    public Player(int id, String name, int age, PlayerPositionEnum position,Integer goalNb) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.position = position;
        this.goalNb = goalNb;
    }
    public Player(int id, String name, int age, PlayerPositionEnum position,Integer goalNb, Team team) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.position = position;
        this.goalNb = goalNb;
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

    public PlayerPositionEnum getPosition() {
        return position;
    }

    public void setPosition(PlayerPositionEnum position) {
        this.position = position;
    }

    public Integer getGoalNb() {
        return goalNb;
    }

    public void setGoalNb(Integer goalNb) {
        this.goalNb = goalNb;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id && age == player.age && Objects.equals(name, player.name) && position == player.position && Objects.equals(goalNb, player.goalNb) && Objects.equals(team, player.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, position, goalNb, team);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", position=" + position +
                ", goalNb=" + goalNb +
                '}';
    }
}
