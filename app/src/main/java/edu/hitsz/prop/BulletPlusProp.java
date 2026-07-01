package edu.hitsz.prop;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.strategy.CircularShootStrategy;
import edu.hitsz.strategy.StraightShootStrategy;

/**
 * 子弹Plus道具（对应 base 的 PropBulletPlus）
 * 生效后切换为圆形射击，固定生效若干帧后恢复直线射击
 */
public class BulletPlusProp extends AbstractProp {

    private static final int EFFECT_DURATION_FRAMES = 75;
    private boolean effectCalled = false;

    public BulletPlusProp(int locationX, int locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public synchronized void effect(AbstractAircraft aircraft) {
        if (!effectCalled) {
            effectCalled = true;
            aircraft.activateTimedShootBuff(
                    new CircularShootStrategy(),
                    32,
                    EFFECT_DURATION_FRAMES,
                    new StraightShootStrategy(),
                    1
            );
        }
    }
}
