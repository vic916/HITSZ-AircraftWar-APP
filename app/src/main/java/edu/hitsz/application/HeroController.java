package edu.hitsz.application;

import android.view.MotionEvent;
import android.view.View;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.game.Game;

/**
 * 英雄机控制类
 * 监听鼠标，控制英雄机的移动
 *
 * @author hitsz
 */
public class HeroController implements View.OnTouchListener {
    private final Game game;
    private final HeroAircraft heroAircraft;

    public HeroController(Game game, HeroAircraft heroAircraft) {
        this.game = game;
        this.heroAircraft = heroAircraft;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (game.isGameOverFlag()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float targetX = clamp(event.getX(), heroAircraft.getWidth() / 2f,
                        v.getWidth() - heroAircraft.getWidth() / 2f);
                float targetY = clamp(event.getY(), heroAircraft.getHeight() / 2f,
                        v.getHeight() - heroAircraft.getHeight() / 2f);
                heroAircraft.setLocation(targetX, targetY);
                return true;
            default:
                return true;
        }
    }
}
