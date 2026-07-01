package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 圆形扩散射击策略（对应 base 的 CircularShoot）
 */
public class CircularShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        if (aircraft instanceof HeroAircraft) {
            aircraft.setShootNum(32);
        }
        int shootNum = aircraft.getShootNum();
        int power = aircraft.getpower();

        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY();
        double angleStep = 360.0 / shootNum;
        double radius = 30;
        double initialSpeed = (aircraft instanceof AbstractEnemyAircraft) ? 4 : 8;

        for (int i = 0; i < shootNum; i++) {
            double angle = Math.toRadians(i * angleStep);
            double offsetX = radius * Math.cos(angle);
            double offsetY = radius * Math.sin(angle);
            double bSpeedX = initialSpeed * Math.cos(angle);
            double bSpeedY = initialSpeed * Math.sin(angle);

            BaseBullet bullet;
            if (aircraft instanceof AbstractEnemyAircraft) {
                bullet = new EnemyBullet(
                        (int)(x + offsetX), (int)(y + offsetY),
                        bSpeedX, bSpeedY, power);
            } else {
                bullet = new HeroBullet(
                        (int)(x + offsetX), (int)(y + offsetY),
                        bSpeedX, bSpeedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}
