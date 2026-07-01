package edu.hitsz.aircraft;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.basic.Observer;
import edu.hitsz.prop.AbstractProp;

import java.util.List;

/**
 * 所有敌机的抽象父类
 */
public abstract class AbstractEnemyAircraft extends AbstractAircraft implements Observer {

    public AbstractEnemyAircraft(int locationX, int locationY, double speedX, double speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        locationX += speedX;
        locationY += speedY;

        // 横向超出边界后反向
        if (locationX <= -speedX || locationX >= MainActivity.WINDOW_WIDTH) {
            speedX = -speedX;
        }

        // 判定 y 轴向下飞行出界
        if (locationY >= MainActivity.WINDOW_HEIGHT) {
            vanish();
        }
    }

    /**
     * 被炸弹道具命中时消失
     */
    @Override
    public void update() {
        this.vanish();
    }

    public abstract List<AbstractProp> dropProps();
}
