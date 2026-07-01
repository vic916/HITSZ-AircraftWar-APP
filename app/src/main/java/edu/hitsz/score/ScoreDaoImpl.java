package edu.hitsz.score;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreDaoImpl implements ScoreDao {

    private static final String FILE_NAME = "scores.txt";

    private final Context context;
    private final List<Score> scores = new ArrayList<>();

    public ScoreDaoImpl(Context context) {
        this.context = context.getApplicationContext();
        loadFromFile();
    }

    @Override
    public Score findById(int scoreId) {
        for (Score score : scores) {
            if (score.getScoreId() == scoreId) {
                return score;
            }
        }
        return null;
    }

    @Override
    public List<Score> getAllScores() {
        loadFromFile();
        return new ArrayList<>(scores);
    }

    @Override
    public void add(Score score) {
        loadFromFile();
        int index = Collections.binarySearch(scores, score);
        if (index < 0) {
            index = -index - 1;
        }
        scores.add(index, score);
        saveToFile();
    }

    @Override
    public void delete(int scoreId) {
        loadFromFile();
        if (scores.removeIf(score -> score.getScoreId() == scoreId)) {
            saveToFile();
        }
    }

    public int nextId() {
        loadFromFile();
        int maxId = 0;
        for (Score score : scores) {
            maxId = Math.max(maxId, score.getScoreId());
        }
        return maxId + 1;
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE),
                StandardCharsets.UTF_8
        ))) {
            for (Score score : scores) {
                writer.println(score.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save scores", e);
        }
    }

    private void loadFromFile() {
        scores.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.openFileInput(FILE_NAME),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Score score = Score.fromString(line);
                if (score != null) {
                    scores.add(score);
                }
            }
            Collections.sort(scores);
        } catch (IOException ignored) {
            // First run has no scores file yet.
        }
    }
}
