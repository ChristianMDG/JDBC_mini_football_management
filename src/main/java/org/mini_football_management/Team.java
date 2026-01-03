package org.mini_football_management;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Team {
    private int id;
    private String name;
    private Continent ContinentEnum;
    private List<Player> players;

    public Team(int id, String name, Continent ContinentEnum) {
        this.id = id;
        this.name = name;
        this.ContinentEnum = ContinentEnum;
        this.players = new ArrayList<>();
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

    public Continent getContinentEnum() {
        return ContinentEnum;
    }

    public void setContinentEnum(Continent continentEnum) {
        ContinentEnum = continentEnum;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Integer getPlayersCount() {
        return players.size();
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id && Objects.equals(name, team.name) && ContinentEnum == team.ContinentEnum && Objects.equals(players, team.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, ContinentEnum, players);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ContinentEnum=" + ContinentEnum +
                ", players=" + players +
                '}';
    }
}
