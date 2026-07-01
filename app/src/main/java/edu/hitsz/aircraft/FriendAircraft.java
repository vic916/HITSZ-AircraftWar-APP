package edu.hitsz.aircraft;

import android.graphics.Bitmap;
import edu.hitsz.application.ImageManager;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 友机（对手飞机），由网络同步位置，自动射击
 * 行为类似英雄机，但不受触摸控制
 */
public class FriendAircraft extends AbstractAircraft {


    public FriendAircraft(int locationX, int locationY, int hp) {
        super(locationX, locationY, 0, 0, hp);
        setStrategy(new StraightShootStrategy());
        this.maxHp = hp;
        this.hp = hp;
        this.direction = -1;
        this.power = 30;
        this.shootNum = 1;
    }

    /**
     * 更新位置（由网络调用）
     */
    public void setPosition(float x, float y) {
        this.locationX = (int)x;
        this.locationY = (int)y;
    }

    @Override
    public void forward() {
        // 友机位置由网络控制，不自动移动
    }
/*
    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        // 与英雄机类似的射击逻辑
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        double speedX = 0;
        double speedY = this.getSpeedY() + direction * 5;
        for (int i = 0; i < shootNum; i++) {
            BaseBullet bullet = new HeroBullet(
                    x + (i * 2 - shootNum + 1) * 10,
                    y, speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }
*/

    @Override
    public List<AbstractProp> dropProps() {
        // 友机不掉落道具
        return new LinkedList<>();
    }

    @Override
    public Bitmap getImage() {
        return ImageManager.HERO_IMAGE; // 复用英雄机图片
    }

    public void addShootNum(int delta) {
        shootNum += delta;
        if (shootNum <= 0) shootNum = 1;
    }

    public void setShootNum(int num) {
        shootNum = Math.max(1, num);
    }

    public void setPower(int power) {
        this.power = power;
    }
}