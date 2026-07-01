package edu.hitsz.aircraft;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.NoneShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 普通敌机
 * 不可射击，不掉落道具
 *
 * @author hitsz
 */
public class MobEnemy extends AbstractEnemyAircraft {

    public MobEnemy(int locationX, int locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.direction = 1;
        this.power = 0;
        this.shootNum = 0;
        setStrategy(new NoneShootStrategy());
    }

    @Override
    public List<AbstractProp> dropProps() {
        return new LinkedList<>();
    }
}
