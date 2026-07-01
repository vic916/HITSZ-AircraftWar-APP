package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.strategy.DivergentShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;

/**
 * 子弹强化道具（对应 base 的 PropBullet）
 * 生效后切换为扩散射击，固定生效若干帧后恢复直线射击
 */
public class BulletProp extends AbstractProp {

    private static final int EFFECT_DURATION_FRAMES = 75;
    private boolean effectCalled = false;

    public BulletProp(int locationX, int locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public synchronized void effect(AbstractAircraft aircraft) {
        if (!effectCalled) {
            effectCalled = true;
            aircraft.activateTimedShootBuff(
                    new DivergentShootStrategy(),
                    8,
                    EFFECT_DURATION_FRAMES,
                    new StraightShootStrategy(),
                    1
            );
        }
    }
}
