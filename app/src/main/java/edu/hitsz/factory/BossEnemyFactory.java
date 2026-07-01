package edu.hitsz.factory;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.game.Game;
import edu.hitsz.strategy.ScatterShootStrategy;

public class BossEnemyFactory implements AircraftFactory {
    private static final double BOSS_HEIGHT_RATIO = 0.13;

    @Override
    public AbstractAircraft createAircraft(double speedX, double speedY, int hp) {
        int direction = Game.sharedRandom.nextBoolean() ? 1 : -1;
        AbstractAircraft bossEnemy = new BossEnemy(
                MainActivity.WINDOW_WIDTH / 2,
                (int) (MainActivity.WINDOW_HEIGHT * BOSS_HEIGHT_RATIO),
                direction * speedX,
                speedY,
                hp
        );
        bossEnemy.setStrategy(new ScatterShootStrategy());
        return bossEnemy;
    }
}
