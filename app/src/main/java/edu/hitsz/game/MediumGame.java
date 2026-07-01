package edu.hitsz.game;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;

import edu.hitsz.application.ImageManager;

/**
 * 普通模式（对应 base 的 GameNormal）
 * Boss血量不随出现次数提升
 */
public class MediumGame extends Game {

    public MediumGame(Context context, Handler handler) throws IOException, ClassNotFoundException {
        super(context, handler);
        this.backGround = ImageManager.BACKGROUND2_IMAGE;
    }

    @Override
    protected void setDifficulty() {
        difficulty = "Normal";
        cycleDuration = 600;
        enemyMaxNumber = 2;
        enemyCreateNumber = 1;
        mobProbability = 0.5;
        elitePlusProbability = 0.2;
        eliteProbability = 0.25;
        BossEnemyHP = 1000;
        enemyHP = 2.0;
        BossEnemyScoreThreshold = 300;
        bossEnabled = true;
    }

    @Override
    protected void difficultyUpdate() {
        enemyCreateNumber += 1;
        mobProbability = Math.max(0, mobProbability - 0.003);
        elitePlusProbability = Math.min(0.7, elitePlusProbability + 0.001);
        eliteProbability = Math.min(0.5, eliteProbability + 0.003);
        enemyMaxNumber += 1;
        enemyHP += 0.05;
        System.out.println("[Normal] 难度增加 每轮:" + enemyCreateNumber
                + " mob:" + mobProbability + " elite:" + eliteProbability
                + " elitePlus:" + elitePlusProbability + " enemyHP:" + enemyHP);
    }
}
