package edu.hitsz.DAO;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int score;
    private String overTime;

    public User() {
        this("", 0, "");
    }

    public User(String name, int score, String overTime) {
        this.name = name;
        this.score = score;
        this.overTime = overTime;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public String getName() {
        return name;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public int getScore() {
        return score;
    }

    public void setOverTime(String overTime) {
        this.overTime = overTime == null ? "" : overTime;
    }

    public String getOverTime() {
        return overTime;
    }

    public String getTime() {
        return getOverTime();
    }
}
