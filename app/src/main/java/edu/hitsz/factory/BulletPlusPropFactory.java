package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BulletPlusProp;

public class BulletPlusPropFactory implements PropFactory {
    @Override
    public AbstractProp createProp(int locationX, int locationY) {
        return new BulletPlusProp(locationX, locationY, 0, 5);
    }
}
