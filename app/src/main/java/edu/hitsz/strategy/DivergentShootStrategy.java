package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 扩散射击策略（对应 base 的 DivergentShoot）
 */
public class DivergentShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        if (aircraft instanceof HeroAircraft) {
            aircraft.setShootNum(8);
        }
        int shootNum = aircraft.getShootNum();
        int power = aircraft.getpower();
        int direction = aircraft.getDirection();

        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        double speedX = 10;
        double speedY = aircraft.getSpeedY() + direction * 10;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            double bSpeedX = ((double)(i * 2 - shootNum + 1)) / 4.0;
            int bX = x + (int)((i * 2 - shootNum + 1) * speedX * 0.2);
            if (direction > 0) {
                bullet = new EnemyBullet(bX, y, bSpeedX, speedY, power);
            } else {
                bullet = new HeroBullet(bX, y, bSpeedX, speedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}
