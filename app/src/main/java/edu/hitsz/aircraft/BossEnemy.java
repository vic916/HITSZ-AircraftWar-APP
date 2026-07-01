package edu.hitsz.aircraft;

import edu.hitsz.factory.BloodPropFactory;
import edu.hitsz.factory.BombPropFactory;
import edu.hitsz.factory.BulletPropFactory;
import edu.hitsz.factory.BulletPlusPropFactory;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.game.Game;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.ScatterShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * BOSS敌机
 * 散射攻击，掉落多个道具
 */
public class BossEnemy extends AbstractEnemyAircraft {

    /** 掉落道具数量 */
    private final int propNum = 3;

    public BossEnemy(int locationX, int locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.direction = 1;
        this.power = 10;
        this.shootNum = 3;
        setStrategy(new ScatterShootStrategy());
    }

    /**
     * 被炸弹命中时扣血而非直接消失（与 base 的 BossEnemy.response() 一致）
     */
    @Override
    public void update() {
        decreaseHp(300);
    }

    @Override
    public List<AbstractProp> dropProps() {
        List<AbstractProp> res = new LinkedList<>();
        for (int i = 0; i < propNum; i++) {
            PropFactory propFactory;
            double rand = Game.sharedRandom.nextDouble();
            if (rand < 0.25) {
                propFactory = new BloodPropFactory();
            } else if (rand < 0.5) {
                propFactory = new BombPropFactory();
            } else if (rand < 0.75) {
                propFactory = new BulletPropFactory();
            } else {
                propFactory = new BulletPlusPropFactory();
            }
            res.add(propFactory.createProp(this.locationX + (i - 1) * 50, this.locationY));
        }
        return res;
    }
}
