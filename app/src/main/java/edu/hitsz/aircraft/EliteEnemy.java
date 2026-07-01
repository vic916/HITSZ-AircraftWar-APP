package edu.hitsz.aircraft;

import edu.hitsz.factory.BloodPropFactory;
import edu.hitsz.factory.BombPropFactory;
import edu.hitsz.factory.BulletPropFactory;
import edu.hitsz.factory.BulletPlusPropFactory;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.game.Game;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 精英敌机
 * 可射击，可掉落道具
 *
 * @author hitsz
 */
public class EliteEnemy extends AbstractEnemyAircraft {

    public EliteEnemy(int locationX, int locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.direction = 1;
        this.power = 10;
        this.shootNum = 1;
        setStrategy(new StraightShootStrategy());
    }

    @Override
    public List<AbstractProp> dropProps() {
        List<AbstractProp> res = new LinkedList<>();
        PropFactory propFactory;
        int flag = (int) (Game.sharedRandom.nextDouble() * 5);
        switch (flag) {
            case 0:
                propFactory = new BloodPropFactory();
                break;
            case 1:
                propFactory = new BombPropFactory();
                break;
            case 2:
                propFactory = new BulletPropFactory();
                break;
            case 3:
                propFactory = new BulletPlusPropFactory();
                break;
            default:
                return res; // 20% 概率不掉落
        }
        res.add(propFactory.createProp(this.locationX, this.locationY));
        return res;
    }
}
