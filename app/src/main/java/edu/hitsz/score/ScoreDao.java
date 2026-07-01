package edu.hitsz.score;

import java.util.List;

public interface ScoreDao {

    Score findById(int scoreId);

    List<Score> getAllScores();

    void add(Score score);

    void delete(int scoreId);
}
