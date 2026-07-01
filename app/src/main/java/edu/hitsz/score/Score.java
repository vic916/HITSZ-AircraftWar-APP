package edu.hitsz.score;

public class Score implements Comparable<Score> {

    private final int scoreId;
    private final String username;
    private final int score;
    private final String difficulty;
    private final String time;
    private final String teammateName;
    private final int teammateScore;
    private final String mode;

    public Score(int scoreId, String username, int score, String difficulty, String time) {
        this(scoreId, username, score, difficulty, time, "", 0, "single");
    }

    public Score(int scoreId, String username, int score, String difficulty, String time,
                 String teammateName, int teammateScore, String mode) {
        this.scoreId = scoreId;
        this.username = username;
        this.score = score;
        this.difficulty = difficulty;
        this.time = time;
        this.teammateName = teammateName == null ? "" : teammateName;
        this.teammateScore = teammateScore;
        this.mode = mode == null || mode.isEmpty() ? "single" : mode;
    }

    public int getScoreId() {
        return scoreId;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getTime() {
        return time;
    }

    public String getTeammateName() {
        return teammateName;
    }

    public int getTeammateScore() {
        return teammateScore;
    }

    public String getMode() {
        return mode;
    }

    public boolean isCoop() {
        return "coop".equals(mode);
    }

    public int getTotalScore() {
        return isCoop() ? score + teammateScore : score;
    }

    @Override
    public int compareTo(Score other) {
        int compareResult = Integer.compare(other.getTotalScore(), getTotalScore());
        if (compareResult != 0) {
            return compareResult;
        }
        return Integer.compare(scoreId, other.scoreId);
    }

    @Override
    public String toString() {
        if (isCoop()) {
            return scoreId + "," + username + "," + score + "," + difficulty + "," + time + ","
                    + teammateName + "," + teammateScore + "," + mode;
        }
        return scoreId + "," + username + "," + score + "," + difficulty + "," + time;
    }

    public static Score fromString(String line) {
        String[] parts = line.split(",", 8);
        if (parts.length == 5) {
            return new Score(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    Integer.parseInt(parts[2]),
                    parts[3],
                    parts[4]
            );
        }
        if (parts.length != 8) {
            return null;
        }
        return new Score(
                Integer.parseInt(parts[0]),
                parts[1],
                Integer.parseInt(parts[2]),
                parts[3],
                parts[4],
                parts[5],
                Integer.parseInt(parts[6]),
                parts[7]
        );
    }
}
