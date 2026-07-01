package edu.hitsz.factory;

import static edu.hitsz.game.Game.sharedRandom;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.application.ImageManager;

public class EliteProEnemyFactory implements AircraftFactory {
    @Override
    public AbstractAircraft createAircraft(double speedX, double speedY, int hp) {
        return new EliteProEnemy(
                (int) (sharedRandom.nextDouble() * (MainActivity.WINDOW_WIDTH - ImageManager.ELITE_IMAGE.getWidth())),
                (int) (sharedRandom.nextDouble() * MainActivity.WINDOW_HEIGHT * 0.05),
                speedX, speedY, hp
        );
    }
}
