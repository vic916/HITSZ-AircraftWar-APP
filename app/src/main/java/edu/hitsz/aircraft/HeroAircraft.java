package edu.hitsz.aircraft;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.application.ImageManager;
import edu.hitsz.game.Game;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * 单例模式【双重锁定检查】
 */
public class HeroAircraft extends AbstractAircraft {

    private volatile static HeroAircraft heroAircraft;

    private HeroAircraft(int locationX, int locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.direction = -1;
        this.power = 30;
        this.shootNum = 1;
    }

    public static HeroAircraft getHeroAircraft() {
        if (heroAircraft == null) {
            synchronized (HeroAircraft.class) {
                if (heroAircraft == null) {
                    heroAircraft = new HeroAircraft(
                            MainActivity.WINDOW_WIDTH / 2,
                            MainActivity.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                            0, 0, 10000);
                    heroAircraft.setStrategy(new StraightShootStrategy());
                }
            }
        }
        return heroAircraft;
    }

    public static void refreshHero() {
        if (heroAircraft == null) {
            synchronized (HeroAircraft.class) {
                if (heroAircraft == null) {
                    heroAircraft = new HeroAircraft(
                            MainActivity.WINDOW_WIDTH / 2,
                            MainActivity.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                            0, 0, 10000);
                    heroAircraft.setStrategy(new StraightShootStrategy());
                }
            }
        } else {
            heroAircraft.locationX = MainActivity.WINDOW_WIDTH / 2;
            heroAircraft.locationY = MainActivity.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight();
            heroAircraft.speedX = 0;
            heroAircraft.speedY = 0;
            heroAircraft.hp = 10000;
            heroAircraft.maxHp = 10000;
            heroAircraft.direction = -1;
            heroAircraft.power = 30;
            heroAircraft.shootNum = 1;
            heroAircraft.setStrategy(new StraightShootStrategy());
            heroAircraft.clearTimedShootBuff();
            heroAircraft.isValid = true;
            Game.score = 0;
        }
    }

    @Override
    public void forward() {
        // 英雄机由触摸控制，不通过forward函数移动
    }

    @Override
    public List<AbstractProp> dropProps() {
        return new LinkedList<>();
    }


    private static volatile boolean useSnapshot = false;
    private static float snapshotX, snapshotY;

    public static void setCoopSnapshot(float x, float y) {
        snapshotX = x;
        snapshotY = y;
        useSnapshot = true;
    }

    public static void clearCoopSnapshot() {
        useSnapshot = false;
    }

    @Override
    public int getLocationX() {
        if (useSnapshot) {
            return (int) snapshotX;
        }
        return locationX;
    }

    @Override
    public int getLocationY() {
        if (useSnapshot) {
            return (int) snapshotY;
        }
        return locationY;
    }

}
