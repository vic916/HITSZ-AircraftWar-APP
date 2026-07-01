package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.FriendAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直线射击策略
 */
public class StraightShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        if (aircraft instanceof HeroAircraft || aircraft instanceof FriendAircraft) {
            aircraft.setShootNum(1);
        }
        int shootNum = aircraft.getShootNum();
        int power = aircraft.getpower();
        int direction = aircraft.getDirection();

        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        double speedX = 0;
        double speedY = aircraft.getSpeedY() + direction * 10;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
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
