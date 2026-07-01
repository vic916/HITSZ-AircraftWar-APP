package edu.hitsz.bullet;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.FriendAircraft;
import edu.hitsz.basic.Observer;
import edu.hitsz.game.CoopGame;
import edu.hitsz.game.Game;

/**
 * 追踪子弹（对应 base 的 TracerBullet）
 * 向英雄机或友机方向追踪，当目标死亡时自动切换到另一个存活目标
 */
public class TracerBullet extends BaseBullet implements Observer {

    private final double BASE_SPEED;
    private static final double TRACKING_STRENGTH = 0.1;
    private static final double MAX_TURN_RATE = 0.3;

    public TracerBullet(int locationX, int locationY, double speedX, double speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        BASE_SPEED = Math.sqrt(speedX * speedX + speedY * speedY);
    }

    @Override
    public void forward() {
        AbstractAircraft target = getValidTarget();
        if (target == null || target.notValid()) {
            super.forward();
            return;
        }

        double targetX = target.getLocationX();
        double targetY = target.getLocationY();
        double dx = targetX - locationX;
        double dy = targetY - locationY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            double dirX = dx / distance;
            double dirY = dy / distance;
            double currentSpeed = Math.sqrt(speedX * speedX + speedY * speedY);

            if (currentSpeed > 0) {
                double currentDirX = speedX / currentSpeed;
                double currentDirY = speedY / currentSpeed;
                double dotProduct = Math.max(-1.0, Math.min(1.0, currentDirX * dirX + currentDirY * dirY));
                double turnAngle = Math.acos(dotProduct);
                double actualTurnRate = Math.min(MAX_TURN_RATE, turnAngle);

                if (turnAngle > 0.01) {
                    double crossProduct = currentDirX * dirY - currentDirY * dirX;
                    double turnDirection = crossProduct > 0 ? 1 : -1;
                    double t = Math.min(actualTurnRate / turnAngle, 1.0);

                    double newDirX = currentDirX * Math.cos(t * turnDirection) - currentDirY * Math.sin(t * turnDirection);
                    double newDirY = currentDirX * Math.sin(t * turnDirection) + currentDirY * Math.cos(t * turnDirection);

                    double finalDirX = currentDirX * (1 - TRACKING_STRENGTH) + newDirX * TRACKING_STRENGTH;
                    double finalDirY = currentDirY * (1 - TRACKING_STRENGTH) + newDirY * TRACKING_STRENGTH;
                    double len = Math.sqrt(finalDirX * finalDirX + finalDirY * finalDirY);

                    if (len > 0) {
                        speedX = (finalDirX / len) * BASE_SPEED;
                        speedY = (finalDirY / len) * BASE_SPEED;
                    }
                }
            }
        }

        locationX += (int) speedX;
        locationY += (int) speedY;

        if (locationX <= 0 || locationX >= MainActivity.WINDOW_WIDTH) vanish();
        if (speedY > 0 && locationY >= MainActivity.WINDOW_HEIGHT) vanish();
        else if (locationY <= 0) vanish();
    }

    /**
     * 获取当前有效的追踪目标。
     * 如果配置的目标无效，则自动切换到另一个存活的飞机。
     */
    private AbstractAircraft getValidTarget() {
        AbstractAircraft configuredTarget = Game.getCurrentTracerTarget();
        boolean configuredValid = (configuredTarget != null && !configuredTarget.notValid());
        if (configuredValid) {
            return configuredTarget;
        }

        // 配置的目标无效，尝试切换到另一个可用的飞机
        HeroAircraft hero = HeroAircraft.getHeroAircraft();
        FriendAircraft friend = CoopGame.getFriendAircraft(); // 仅在 CoopGame 中非空
        boolean heroAlive = (hero != null && !hero.notValid());
        boolean friendAlive = (friend != null && !friend.notValid());

        boolean preferHero = Game.isTracerTargetIsHero(); // 从 Game 获取偏好设置
        if (preferHero) {
            if (heroAlive) return hero;
            if (friendAlive) return friend;
        } else {
            if (friendAlive) return friend;
            if (heroAlive) return hero;
        }
        return null;
    }

    @Override
    public void update() {
        this.vanish();
    }
}