package edu.hitsz.game;

import android.app.Activity;
import android.os.Handler;

import java.io.IOException;

import edu.hitsz.application.ImageManager;

/**
 * 简单模式：无Boss机，难度不随时间增加（对应 base 的 GameEasy）
 */
public class EasyGame extends Game {

    public EasyGame(Activity context, Handler handler) throws IOException, ClassNotFoundException {
        super(context, handler);
        this.backGround = ImageManager.BACKGROUND1_IMAGE;
    }

    @Override
    protected void setDifficulty() {
        difficulty = "Easy";
        enemyMaxNumber = 3;
        enemyCreateNumber = 1;
        mobProbability = 0.6;
        elitePlusProbability = 0.15;
        eliteProbability = 0.2;
        BossEnemyHP = 300;
        enemyHP = 1.0;
        BossEnemyScoreThreshold = 600;
        bossEnabled = false;   // Easy 模式不出现 Boss
    }

    @Override
    protected void generateBoss() {
        // Easy 模式不生成 Boss
    }

    @Override
    protected void difficultyUpdate() {
        // Easy 模式难度不递增
    }
}
