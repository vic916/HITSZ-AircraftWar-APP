package edu.hitsz.factory;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.game.Game;

public class ElitePlusEnemyFactory implements AircraftFactory {
    @Override
    public AbstractAircraft createAircraft(double speedX, double speedY, int hp) {
        return new ElitePlusEnemy(
                (int) (Game.sharedRandom.nextDouble() * (MainActivity.WINDOW_WIDTH - ImageManager.ELITE_IMAGE.getWidth())),
                (int) (Game.sharedRandom.nextDouble() * MainActivity.WINDOW_HEIGHT * 0.05),
                speedX, speedY, hp
        );
    }
}
