package edu.hitsz.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.hitsz.activity.MainActivity;
import edu.hitsz.activity.OnlineActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.FriendAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.basic.Observer;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletProp;
import edu.hitsz.prop.BulletPlusProp;

/**
 * 双人实时模式：本地英雄机 + 远程友机（对手）
 * 采用帧同步：每帧发送 (x, y, frame)，等待对方相同帧数据到达后才执行本帧逻辑
 */
public class CoopGame extends Game {

    private static FriendAircraft friendInstance = null;
    private static final String TAG = "CoopGame";
    private static final long FRAME_WAIT_TIMEOUT_MS = 250;
    private PrintWriter writer;
    private FriendAircraft friendAircraft;   // 对手的飞机

    private final Map<Integer, float[]> futureFrames = new ConcurrentHashMap<>();

    // 帧同步相关
    private int currentFrame = 0;                // 当前帧序号
    private final Object frameLock = new Object(); // 等待锁
    private volatile boolean frameReceived = false; // 是否收到当前帧数据
    private volatile float receivedX, receivedY;   // 收到的对方坐标
    private volatile int receivedFrame = -1;       // 收到的帧号
    private volatile boolean stopWaiting = false;  // 游戏结束标志
    private volatile boolean opponentEnded = false;
    private volatile boolean localEndReported = false;
    private volatile boolean finalShutdownStarted = false;
    float snapshotHeroX,snapshotHeroY;
    private Handler uiHandler;  // 用于通知 OnlineActivity

    public CoopGame(Context context, Handler handler, PrintWriter writer) throws IOException, ClassNotFoundException {
        super(context, handler);
        this.writer = writer;
        this.backGround = ImageManager.BACKGROUND2_IMAGE;
        this.uiHandler = handler;
        // 初始化友机（初始位置放在屏幕外，等收到网络位置后再显示）
        friendAircraft = new FriendAircraft(-100, -100, 10000);
        friendInstance = friendAircraft;
    }

    /**
     * 接收对方的位置数据（由网络线程调用）
     * @param x 对方英雄机x坐标
     * @param y 对方英雄机y坐标
     * @param frame 对应的帧序号
     */
    public void receiveOpponentPosition(float x, float y, int frame) {
        synchronized (frameLock) {
            if (frame == currentFrame) {
                // 正好是当前帧
                receivedX = x;
                receivedY = y;
                receivedFrame = frame;
                frameReceived = true;
                if (friendAircraft != null) {
                    friendAircraft.setPosition(x, y);
                }
                frameLock.notifyAll();
                Log.d(TAG, "Received frame " + frame + " position: (" + x + "," + y + ")");
            } else if (frame > currentFrame) {
                // 未来帧，缓存起来
                futureFrames.put(frame, new float[]{x, y});
                Log.d(TAG, "Cached future frame " + frame + " position: (" + x + "," + y + ")");
            } else {
                // 过去帧，忽略
                Log.d(TAG, "Discarded old frame " + frame);
            }
        }
    }

    /**
     * 发送本机位置和当前帧号到服务器
     */
    private void sendFrameData(int frame, float x, float y) {
        if (writer != null) {
            writer.println(x + "," + y + "," + frame);
            writer.flush();
            Log.d(TAG, "Sent frame " + frame + " position: (" + x + "," + y + ")");
        }
    }

    /**
     * 等待对方相同帧的数据到达（阻塞当前线程，直到收到数据或游戏结束）
     * @param frame 期望的帧号
     * @return true表示收到数据，false表示游戏结束或中断
     */
    private boolean waitForFrameData(int frame) {
        synchronized (frameLock) {
            // 先检查缓存中是否有该帧
            float[] cached = futureFrames.remove(frame);
            if (cached != null) {
                receivedX = cached[0];
                receivedY = cached[1];
                receivedFrame = frame;
                frameReceived = true;
                if (friendAircraft != null) {
                    friendAircraft.setPosition(receivedX, receivedY);
                }
                Log.d(TAG, "Using cached frame " + frame);
                return true;
            }
            if (frameReceived && receivedFrame == frame) {
                return true;
            }
            // Wait with timeout to avoid permanent freeze when peer packets are delayed/lost.
            long deadline = System.currentTimeMillis() + FRAME_WAIT_TIMEOUT_MS;
            while (!frameReceived && !stopWaiting && !gameOverFlag && !opponentEnded) {
                long remain = deadline - System.currentTimeMillis();
                if (remain <= 0) {
                    Log.w(TAG, "Frame " + frame + " wait timeout, continue with last peer state");
                    return false;
                }
                try {
                    frameLock.wait(Math.min(remain, FRAME_WAIT_TIMEOUT_MS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return frameReceived && receivedFrame == frame;
        }
    }
    /**
     * 重置等待状态，准备下一帧
     */
    private void resetWaitState() {
        synchronized (frameLock) {
            frameReceived = false;
            receivedFrame = -1;
        }
    }

    // ===================== 重写游戏主循环action，加入帧同步 =====================

    @Override
    public void action() {
        if (gameOverFlag) {
            return;
        }

        // 帧序号递增
        currentFrame++;

        boolean heroAlive = !heroAircraft.notValid();
        boolean friendAlive = friendAircraft != null && !friendAircraft.notValid() && !opponentEnded;

        // 保存当前帧的英雄机坐标快照
        snapshotHeroX = (float) heroAircraft.getLocationX();
        snapshotHeroY = (float)heroAircraft.getLocationY();
        HeroAircraft.setCoopSnapshot(snapshotHeroX, snapshotHeroY);
        if (!localEndReported) {
            if (heroAlive) {
                sendFrameData(currentFrame, snapshotHeroX, snapshotHeroY);
            } else {
                // Keep compatibility with server parser before end packet is flushed.
                sendFrameData(currentFrame, 1.0f, 1.0f);
            }
        }

        // Frame sync is only needed while both sides are still alive.
        if (heroAlive && friendAlive && currentFrame >= 0) {
            waitForFrameData(currentFrame);
        }

        // 重置等待状态，准备下一帧
        resetWaitState();

        // 执行父类游戏逻辑（敌机生成、移动、碰撞等）
        super.action();
        HeroAircraft.clearCoopSnapshot();
    }

    // ===================== 游戏结束处理 =====================


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);

        boolean heroDead = heroAircraft.getHp() <= 0 || heroAircraft.notValid();
        boolean friendDead = opponentEnded || friendAircraft == null || friendAircraft.getHp() <= 0 || friendAircraft.notValid();

        if (heroDead && !localEndReported) {
            reportLocalEnd();
        }

        if (heroDead && friendDead && !finalShutdownStarted) {
            finalShutdownStarted = true;
            gameOverFlag = true;
            stopWaiting = true;
            mbLoop = false;
            System.out.println("Coop Game Over!");

            try {
                if (needSound) {
                    music.gameOverbgm();
                }
                if (needBgm) {
                    music.stopBgm();
                    music.stopBossBgm();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed during coop shutdown", e);
            }
        }

        if (gameOverFlag) {
            stopWaiting = true;
            synchronized (frameLock) {
                frameLock.notifyAll();
            }
        }
    }


    private void reportLocalEnd() {
        localEndReported = true;
        if (writer != null) {
            writer.println("end:" + score);
            writer.flush();
        }
        OnlineActivity.markLocalGameEnded();
        synchronized (frameLock) {
            frameLock.notifyAll();
        }
    }

    // ===================== 难度设置 =====================

    @Override
    protected void setDifficulty() {
        difficulty = "Coop";
        cycleDuration = 600;
        enemyMaxNumber = 4;
        enemyCreateNumber = 2;
        mobProbability = 0.5;
        elitePlusProbability = 0.2;
        eliteProbability = 0.25;
        BossEnemyHP = 1200;
        enemyHP = 2.0;
        BossEnemyScoreThreshold = 400;
        bossEnabled = true;
    }

    @Override
    protected void addEnemy() {
        if (enemyAircrafts.size() >= enemyMaxNumber) return;
        double num = sharedRandom.nextDouble();
        if (num < mobProbability) {
            factory = new edu.hitsz.factory.MobEnemyFactory();
            enemyAircrafts.add(factory.createAircraft(0, 7, (int)(30 * enemyHP)));
        } else if (num < mobProbability + elitePlusProbability) {
            factory = new edu.hitsz.factory.ElitePlusEnemyFactory();
            enemyAircrafts.add(factory.createAircraft(3, 7, (int)(90 * enemyHP)));
        } else if (num < mobProbability + elitePlusProbability + eliteProbability) {
            factory = new edu.hitsz.factory.EliteEnemyFactory();
            enemyAircrafts.add(factory.createAircraft(3, 7, (int)(90 * enemyHP)));
        } else {
            factory = new edu.hitsz.factory.EliteProEnemyFactory();
            enemyAircrafts.add(factory.createAircraft(1, 3, (int)(180 * enemyHP)));
        }
    }

    @Override
    protected void generateBoss() {
        super.generateBoss();
    }

    @Override
    protected void difficultyUpdate() {
        enemyCreateNumber += 1;
        mobProbability = Math.max(0, mobProbability - 0.003);
        elitePlusProbability = Math.min(0.7, elitePlusProbability + 0.001);
        eliteProbability = Math.min(0.5, eliteProbability + 0.003);
        enemyMaxNumber += 1;
        enemyHP += 0.05;
        Log.i(TAG, "难度增加: 每轮敌机=" + enemyCreateNumber);
    }

    @Override
    protected void updateTimedShootEffects() {
        super.updateTimedShootEffects();
        if (friendAircraft != null && !friendAircraft.notValid() && !opponentEnded) {
            friendAircraft.tickTimedShootBuff();
        }
    }

    // ===================== 重写射击逻辑，让友机也射击 =====================

    @Override
    protected void shootAction() {
        // 敌人射击
        for (AbstractAircraft enemy : enemyAircrafts) {
            enemyBullets.addAll(enemy.shoot());
        }
        // 英雄射击
        if (!heroAircraft.notValid()) {
            heroBullets.addAll(heroAircraft.shoot());
        }
        // 友机射击
        if (friendAircraft != null && !friendAircraft.notValid() && !opponentEnded) {
            heroBullets.addAll(friendAircraft.shoot());
        }
    }

    // ===================== 重写碰撞检测，包含友机 =====================

    @Override
    protected void crashCheckAction() {
        // 1. 敌机子弹攻击英雄和友机
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (!heroAircraft.notValid() && heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
            if (bullet.notValid()) continue;
            if (!friendAircraft.notValid() && friendAircraft.crash(bullet)) {
                friendAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 2. 英雄子弹和友机子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) continue;
            for (int i = 0; i < enemyAircrafts.size(); i++) {
                AbstractAircraft enemy = enemyAircrafts.get(i);
                if (enemy.notValid()) continue;
                if (enemy.crash(bullet)) {
                    enemy.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemy.notValid()) {
                        props.addAll(enemy.dropProps());
                        // 得分
                        if (enemy instanceof BossEnemy) {
                            addScore(100);
                            if (needBgm) music.playBgm();
                        } else if (enemy instanceof EliteProEnemy) {
                            addScore(30);
                        } else if (enemy instanceof ElitePlusEnemy) {
                            addScore(20);
                        } else if (enemy instanceof EliteEnemy) {
                            addScore(20);
                        } else if (enemy instanceof MobEnemy) {
                            addScore(10);
                        }
                    }
                }
                // 敌机与英雄或友机相撞
                if (!heroAircraft.notValid() && (enemy.crash(heroAircraft) || heroAircraft.crash(enemy))) {
                    enemy.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
                if (!friendAircraft.notValid() && (enemy.crash(friendAircraft) || friendAircraft.crash(enemy))) {
                    enemy.vanish();
                    friendAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // 3. 道具获取（英雄和友机均可拾取）
        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            boolean picked = false;
            if (!heroAircraft.notValid() && (heroAircraft.crash(prop) || prop.crash(heroAircraft))) {
                applyProp(prop, heroAircraft);
                picked = true;
            }
            if (!picked && !friendAircraft.notValid() && (friendAircraft.crash(prop) || prop.crash(friendAircraft))) {
                applyProp(prop, friendAircraft);
                picked = true;
            }
            if (picked) {
                prop.vanish();
            }
        }
    }

    private void applyProp(AbstractProp prop, AbstractAircraft target) {
        if (prop instanceof BombProp) {
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy instanceof Observer) {
                    ((BombProp) prop).addObserver((Observer) enemy);
                }
            }
            for (BaseBullet bullet : enemyBullets) {
                if (bullet instanceof Observer) {
                    ((BombProp) prop).addObserver((Observer) bullet);
                }
            }
        }
        if (needSound) {
            if (prop instanceof BombProp) music.explosionSP();
            else if (prop instanceof BloodProp) music.supplySP();
            else if (prop instanceof BulletProp || prop instanceof BulletPlusProp) music.hitSP();
        }
        prop.effect(target);
    }


    // ===================== 绘制 =====================

    @Override
    public void draw() {
        canvas = surfaceHolder.lockCanvas();
        if (canvas == null) return;

        // 背景
        canvas.drawBitmap(backGround, 0, backGroundTop - backGround.getHeight(), mPaint);
        canvas.drawBitmap(backGround, 0, backGroundTop, mPaint);
        backGroundTop += 1;
        if (backGroundTop == MainActivity.WINDOW_HEIGHT) backGroundTop = 0;

        // 道具、子弹、敌机
        paintImageWithPositionRevised(props);
        paintImageWithPositionRevised(enemyBullets);
        paintImageWithPositionRevised(heroBullets);
        paintImageWithPositionRevised(enemyAircrafts);

        // 绘制友机
        if (!friendAircraft.notValid() && !opponentEnded) {
            Bitmap friendImg = friendAircraft.getImage();
            canvas.drawBitmap(friendImg,
                    friendAircraft.getLocationX() - friendImg.getWidth() / 2f,
                    friendAircraft.getLocationY() - friendImg.getHeight() / 2f,
                    mPaint);
        }

        // 绘制英雄机
        if (!heroAircraft.notValid()) {
            canvas.drawBitmap(ImageManager.HERO_IMAGE,
                    snapshotHeroX - ImageManager.HERO_IMAGE.getWidth() / 2,
                    snapshotHeroY - ImageManager.HERO_IMAGE.getHeight() / 2,
                    mPaint);
        }

        // 分数和生命值（自定义，显示双方）
        paintCoopScoreAndLife();

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void paintCoopScoreAndLife() {
        int x = 30;
        int y = 60;
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(55);
        canvas.drawText("SCORE:" + score, x, y, mPaint);
        canvas.drawText("LIFE:" + heroAircraft.getHp(), x, y + 60, mPaint);
        canvas.drawText("FRIEND LIFE:" + friendAircraft.getHp(), x, y + 120, mPaint);

        // 对手分数和名字（从OnlineActivity获取）
        canvas.drawText("OPPONENT:" + OnlineActivity.getOpponentName(), x + 450, y, mPaint);
        canvas.drawText("OPP SCORE:" + OnlineActivity.getOpponentScore(), x + 450, y + 60, mPaint);
    }

    public static FriendAircraft getFriendAircraft() {
        return friendInstance;
    }

    public void markOpponentEnded() {
        opponentEnded = true;
        if (friendAircraft != null && !friendAircraft.notValid()) {
            friendAircraft.vanish();
        }
        synchronized (frameLock) {
            frameLock.notifyAll();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        friendInstance = null;   // 避免内存泄漏
        stopWaiting = true;
        synchronized (frameLock) {
            frameLock.notifyAll();
        }
    }

    public void stopGameLoop() {
        gameOverFlag = true;
        stopWaiting = true;
        mbLoop = false;
        synchronized (frameLock) {
            frameLock.notifyAll();  // 唤醒可能阻塞的等待线程
        }
    }
}
