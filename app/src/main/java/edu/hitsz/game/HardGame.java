package edu.hitsz.game;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;

import edu.hitsz.application.ImageManager;

/**
 * 困难模式（对应 base 的 GameHard）
 * Boss随出现次数血量递增
 */
public class HardGame extends Game {

    public HardGame(Context context, Handler handler) throws IOException, ClassNotFoundException {
        super(context, handler);
        this.backGround = ImageManager.BACKGROUND3_IMAGE;
    }

    @Override
    protected void setDifficulty() {
        difficulty = "Hard";
        cycleDuration = 600;
        enemyMaxNumber = 3;
        enemyCreateNumber = 1;
        mobProbability = 0.5;
        elitePlusProbability = 0.2;
        eliteProbability = 0.25;
        BossEnemyHP = 1600;
        enemyHP = 2.0;
        BossEnemyScoreThreshold = 600;
        bossEnabled = true;
    }

    @Override
    protected void difficultyUpdate() {
        enemyCreateNumber += 1;
        mobProbability = Math.max(0, mobProbability - 0.006);
        eliteProbability = Math.max(0, eliteProbability - 0.006);
        elitePlusProbability = Math.min(0.8, elitePlusProbability + 0.006);
        BossEnemyHP += 800;
        enemyMaxNumber += 2;
        enemyHP += 0.2;
        System.out.println("[Hard] 难度增加 每轮:" + enemyCreateNumber
                + " mob:" + mobProbability + " elite:" + eliteProbability
                + " elitePlus:" + elitePlusProbability + " BossHP:" + BossEnemyHP
                + " enemyHP:" + enemyHP);
    }
}
