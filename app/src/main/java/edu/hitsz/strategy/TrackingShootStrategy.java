package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.TracerBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 追踪射击策略（对应 base 的 TrackingShoot）
 * 每隔 shootInterval 次调用发射一次追踪导弹
 */
public class TrackingShootStrategy implements ShootStrategy {

    private int shootCounter = 3;
    private final int shootInterval = 5;
    private final int missileCount = 2;

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        shootCounter++;
        if (shootCounter % shootInterval != 0) {
            return res;
        }

        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY();
        double baseSpeed = 8;

        for (int i = 0; i < missileCount; i++) {
            double angleOffset = (i - (missileCount - 1) / 2.0) * 15;
            double bSpeedX = baseSpeed * Math.sin(Math.toRadians(angleOffset));
            double bSpeedY = baseSpeed * Math.cos(Math.toRadians(angleOffset));
            int offsetX = (int)(150 * Math.sin(Math.toRadians(angleOffset)));
            res.add(new TracerBullet(x + offsetX, y, bSpeedX, bSpeedY, 200));
        }
        return res;
    }
}
