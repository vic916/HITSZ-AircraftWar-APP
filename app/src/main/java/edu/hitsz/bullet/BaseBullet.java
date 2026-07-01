package edu.hitsz.bullet;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.basic.AbstractFlyingObject;

/**
 * 子弹类。
 * 也可以考虑不同类型的子弹
 *
 * @author hitsz
 */
public class BaseBullet extends AbstractFlyingObject {
    private static final int DEFAULT_POWER = 10;

    private final int power;

    public BaseBullet(int locationX, int locationY, double speedX, double speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power > 0 ? power : DEFAULT_POWER;
    }

    @Override
    public void forward() {
        super.forward();
        if (touchesHorizontalEdge() || leavesVerticalBounds()) {
            vanish();
        }
    }

    private boolean leavesVerticalBounds() {
        boolean leaveBottom = speedY > 0 && locationY >= MainActivity.WINDOW_HEIGHT;
        boolean leaveTop = locationY <= 0;
        return leaveBottom || leaveTop;
    }

    public int getPower() {
        return power;
    }
}
