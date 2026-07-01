package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.ShootStrategy;

import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {
    protected int maxHp;
    protected int hp;
    protected int power;
    protected int direction;
    protected int shootNum = 1;

    private ShootStrategy shootStrategy;
    private int timedShootBuffFrames = 0;
    private ShootStrategy timedShootBuffRestoreStrategy;
    private int timedShootBuffRestoreShootNum = 1;

    public AbstractAircraft(int locationX, int locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void setStrategy(ShootStrategy shootStrategy) {
        if (shootStrategy != null) {
            this.shootStrategy = shootStrategy;
        }
    }

    public void activateTimedShootBuff(ShootStrategy buffStrategy, int buffShootNum,
                                       int durationFrames, ShootStrategy restoreStrategy,
                                       int restoreShootNum) {
        this.shootStrategy = buffStrategy;
        this.shootNum = buffShootNum;
        this.timedShootBuffFrames = Math.max(0, durationFrames);
        this.timedShootBuffRestoreStrategy = restoreStrategy;
        this.timedShootBuffRestoreShootNum = Math.max(1, restoreShootNum);
    }

    public void tickTimedShootBuff() {
        if (timedShootBuffFrames <= 0) {
            return;
        }
        timedShootBuffFrames--;
        if (timedShootBuffFrames == 0 && timedShootBuffRestoreStrategy != null) {
            this.shootStrategy = timedShootBuffRestoreStrategy;
            this.shootNum = timedShootBuffRestoreShootNum;
        }
    }

    public void clearTimedShootBuff() {
        timedShootBuffFrames = 0;
        timedShootBuffRestoreStrategy = null;
        timedShootBuffRestoreShootNum = 1;
    }

    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(this);
    }

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
            return;
        }
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getpower() {
        return power;
    }

    public int getDirection() {
        return direction;
    }

    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
    }

    public int getShootNum() {
        return shootNum;
    }

    public abstract List<AbstractProp> dropProps();
}
