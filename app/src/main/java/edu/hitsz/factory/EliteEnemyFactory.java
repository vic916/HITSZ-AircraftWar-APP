package edu.hitsz.factory;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.game.Game;
import edu.hitsz.strategy.StraightShootStrategy;

public class EliteEnemyFactory implements AircraftFactory {
    private static final double SPAWN_HEIGHT_RATIO = 0.05;

    private int randomSpawnX() {
        return (int) (Game.sharedRandom.nextDouble()
                * (MainActivity.WINDOW_WIDTH - ImageManager.ELITE_IMAGE.getWidth()));
    }

    private int randomSpawnY() {
        return (int) (Game.sharedRandom.nextDouble() * MainActivity.WINDOW_HEIGHT * SPAWN_HEIGHT_RATIO);
    }

    @Override
    public AbstractAircraft createAircraft(double speedX, double speedY, int hp) {
        AbstractAircraft eliteEnemy = new EliteEnemy(
                randomSpawnX(),
                randomSpawnY(),
                speedX,
                speedY,
                hp
        );
        eliteEnemy.setStrategy(new StraightShootStrategy());
        return eliteEnemy;
    }
}
