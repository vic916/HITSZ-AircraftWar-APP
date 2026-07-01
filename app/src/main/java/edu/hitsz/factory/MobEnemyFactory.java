package edu.hitsz.factory;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.game.Game;
import edu.hitsz.strategy.NoneShootStrategy;

public class MobEnemyFactory implements AircraftFactory {
    private static final double SPAWN_HEIGHT_RATIO = 0.05;

    private int randomSpawnX() {
        return (int) (Game.sharedRandom.nextDouble()
                * (MainActivity.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth()));
    }

    private int randomSpawnY() {
        return (int) (Game.sharedRandom.nextDouble() * MainActivity.WINDOW_HEIGHT * SPAWN_HEIGHT_RATIO);
    }

    @Override
    public AbstractAircraft createAircraft(double speedX, double speedY, int hp) {
        AbstractAircraft mobEnemy = new MobEnemy(
                randomSpawnX(),
                randomSpawnY(),
                speedX,
                speedY,
                hp
        );
        mobEnemy.setStrategy(new NoneShootStrategy());
        return mobEnemy;
    }
}
