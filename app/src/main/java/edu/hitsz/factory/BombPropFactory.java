package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;

public class BombPropFactory implements PropFactory {
    private static final double PROP_DROP_SPEED_Y = 5;

    @Override
    public AbstractProp createProp(int locationX, int locationY) {
        return new BombProp(locationX, locationY, 0, PROP_DROP_SPEED_Y);
    }
}
