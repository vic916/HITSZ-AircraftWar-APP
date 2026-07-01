package edu.hitsz.basic;

import android.graphics.Bitmap;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.ImageManager;

/**
 * 可飞行对象的父类
 *
 * @author hitsz
 */
public abstract class AbstractFlyingObject {
    private static final int AIRCRAFT_COLLISION_SCALE = 2;

    protected double exactLocationX;
    protected double exactLocationY;
    protected int locationX;
    protected int locationY;
    protected double speedX;
    protected double speedY;
    protected Bitmap image = null;
    protected int width = -1;
    protected int height = -1;
    protected boolean isValid = true;

    public AbstractFlyingObject() {
    }

    public AbstractFlyingObject(int locationX, int locationY, double speedX, double speedY) {
        this.exactLocationX = locationX;
        this.exactLocationY = locationY;
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void forward() {
        exactLocationX += speedX;
        exactLocationY += speedY;
        locationX = (int) exactLocationX;
        locationY = (int) exactLocationY;
        if (touchesHorizontalEdge()) {
            speedX = -speedX;
        }
    }

    protected boolean touchesHorizontalEdge() {
        return locationX <= 0 || locationX >= MainActivity.WINDOW_WIDTH;
    }

    private int collisionHeightFactor() {
        return this instanceof AbstractAircraft ? AIRCRAFT_COLLISION_SCALE : 1;
    }

    public boolean crash(AbstractFlyingObject flyingObject) {
        int horizontalLimit = (flyingObject.getWidth() + getWidth()) / 2;
        int verticalLimit = (flyingObject.getHeight() / flyingObject.collisionHeightFactor()
                + getHeight() / collisionHeightFactor()) / 2;
        int deltaX = Math.abs(flyingObject.getLocationX() - locationX);
        int deltaY = Math.abs(flyingObject.getLocationY() - locationY);
        return deltaX < horizontalLimit && deltaY < verticalLimit;
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public void setLocation(double locationX, double locationY) {
        this.exactLocationX = locationX;
        this.exactLocationY = locationY;
        this.locationX = (int) locationX;
        this.locationY = (int) locationY;
    }

    public double getSpeedY() {
        return speedY;
    }

    public double getSpeedX() {
        return speedX;
    }

    public Bitmap getImage() {
        if (image == null) {
            image = ImageManager.get(this);
        }
        return image;
    }

    private void cacheImageSizeIfNeeded() {
        if (width != -1 && height != -1) {
            return;
        }
        Bitmap bitmap = ImageManager.get(this);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
    }

    public int getWidth() {
        cacheImageSizeIfNeeded();
        return width;
    }

    public int getHeight() {
        cacheImageSizeIfNeeded();
        return height;
    }

    public boolean notValid() {
        return !isValid;
    }

    public void vanish() {
        isValid = false;
    }
}
