package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 散射策略（与 base 的 DivergentShoot 等价）
 */
public class ScatterShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        int shootNum = aircraft.getShootNum();
        int power = aircraft.getpower();
        int direction = aircraft.getDirection();

        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        double speedY = aircraft.getSpeedY() + direction * 10;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // Centered horizontal spread: 2 bullets => -1, +1
            double speedX = (i * 2 - shootNum + 1);
            if (direction > 0) {
                bullet = new EnemyBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            } else {
                bullet = new HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}
