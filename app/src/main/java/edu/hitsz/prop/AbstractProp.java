package edu.hitsz.prop;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.basic.AbstractFlyingObject;

/**
 * 所有道具的抽象父类
 *
 * @author hitsz
 */
public abstract class AbstractProp extends AbstractFlyingObject {

    public AbstractProp(int locationX, int locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void forward() {
        super.forward();
        if (leavesVerticalBounds()) {
            vanish();
        }
    }

    private boolean leavesVerticalBounds() {
        return (speedY > 0 && locationY >= MainActivity.WINDOW_HEIGHT) || locationY <= 0;
    }

    public abstract void effect(AbstractAircraft aircraft);
}
