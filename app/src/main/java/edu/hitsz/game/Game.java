package edu.hitsz.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import edu.hitsz.DAO.User;
import edu.hitsz.DAO.UserDao;
import edu.hitsz.activity.LauncherActivity;
import edu.hitsz.activity.MainActivity;
import edu.hitsz.activity.OnlineActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.AbstractEnemyAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.GameConfig;
import edu.hitsz.application.HeroController;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.MusicService;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.basic.Observer;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.bullet.TracerBullet;
import edu.hitsz.factory.AircraftFactory;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletProp;
import edu.hitsz.prop.BulletPlusProp;

/**
 * 游戏逻辑抽象基类，遵循模板模式
 *
 */
public abstract class Game extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public static Random sharedRandom = new Random(); // 默认种子，后续会被覆盖
    static {
        Log.d("RandomTest","Random seed first value: "+sharedRandom.nextInt(100));
    }

    private static boolean tracerTargetIsHero = true;

    public static void setTracerTargetIsHero(boolean isHero) {
        tracerTargetIsHero = isHero;
        Log.d("Game", "Tracer target is hero: " + isHero);
    }

    public static boolean isTracerTargetIsHero() {
        return tracerTargetIsHero;
    }

    public static AbstractAircraft getCurrentTracerTarget() {
        if (tracerTargetIsHero) {
            return HeroAircraft.getHeroAircraft();
        } else {
            // 从 CoopGame 获取友机实例
            return CoopGame.getFriendAircraft();
        }
    }

    public static final String TAG = "BaseGame";
    boolean mbLoop = false;
    protected SurfaceHolder surfaceHolder;
    protected Canvas canvas;
    protected Paint mPaint;
    private Handler handler;
    private Context context;
    MusicService music;
    UserDao data;
    User user;

    /** 背景图片 */
    protected Bitmap backGround;

    protected int backGroundTop = 0;

    protected final ScheduledExecutorService executorService;
    protected boolean gameOverFlag = false;
    public static int score = 0;
    public static boolean needBgm = true;
    public static boolean needSound = true;

    protected AircraftFactory factory;

    protected final HeroAircraft heroAircraft;
    protected final List<AbstractAircraft> enemyAircrafts;
    protected final List<BaseBullet> heroBullets;
    protected final List<BaseBullet> enemyBullets;
    protected final List<AbstractProp> props;

    /** 时间相关参数 */
    protected int timeInterval = 40;
    protected int cycleDuration = 600;
    private int cycleTime = 0;
    private int time = 0;

    // ===== 难度相关参数（与 base 对齐） =====

    /** 屏幕中出现的敌机最大数量 */
    protected int enemyMaxNumber = 4;

    /** 每轮生成敌机数量 */
    protected int enemyCreateNumber = 1;

    /** 普通敌机出现概率 */
    protected double mobProbability = 0.6;

    /** 精英敌机出现概率 */
    protected double eliteProbability = 0.2;

    /** 超级精英敌机出现概率 */
    protected double elitePlusProbability = 0.15;

    /** ElitePro敌机出现概率（剩余概率） */
    // eliteProProbability = 1 - mob - elite - elitePlus

    /** BOSS敌机血量 */
    protected int BossEnemyHP = 300;

    /** BOSS出现分数阈值 */
    protected int BossEnemyScoreThreshold = 600;

    /** 敌机血量加成倍率 */
    protected double enemyHP = 1.0;

    /** 难度是否允许出现Boss */
    protected boolean bossEnabled = true;

    /** 难度标识 */
    protected String difficulty = "";

    /** 当前boss计数，用于下一次Boss出现的阈值 */
    private int bosscnt = 1;

    /** 难度增加触发标志 */
    private boolean difficultyUpdateFlag = true;

    private void awardScore(AbstractAircraft enemy) {
        if (enemy instanceof BossEnemy) {
            addScore(100);
            if (needBgm) {
                music.playBgm();
            }
            return;
        }
        if (enemy instanceof EliteProEnemy) {
            addScore(30);
            return;
        }
        if (enemy instanceof ElitePlusEnemy || enemy instanceof EliteEnemy) {
            addScore(20);
            return;
        }
        if (enemy instanceof MobEnemy) {
            addScore(10);
        }
    }

    private void attachBombTargets(BombProp bombProp) {
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (enemy instanceof Observer) {
                bombProp.addObserver((Observer) enemy);
            }
        }
        for (BaseBullet bullet : enemyBullets) {
            if (bullet instanceof Observer) {
                bombProp.addObserver((Observer) bullet);
            }
        }
    }

    private void playPropSound(AbstractProp prop) {
        if (!needSound) {
            return;
        }
        if (prop instanceof BombProp) {
            music.explosionSP();
        } else if (prop instanceof BloodProp) {
            music.supplySP();
        } else if (prop instanceof BulletProp || prop instanceof BulletPlusProp) {
            music.hitSP();
        }
    }

    private void finalizeOfflineResult() {
        user = new User();
        user.setScore(score);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
        user.setOverTime(formatter.format(date));
    }

    private void awaitOnlineEnding() {
        OnlineActivity.markLocalGameEnded();
        while (!OnlineActivity.isGameOverFlag()) {
            repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void stopAudioAndThreads() {
        executorService.shutdown();
        gameOverFlag = true;
        if (needSound) {
            music.gameOverbgm();
        }
        if (needBgm) {
            music.stopBgm();
            music.stopBossBgm();
        }
    }

    /**
     * 难度递增（每500分触发一次），Easy模式可留空实现
     */
    protected abstract void difficultyUpdate();

    /**
     * 设置难度初始参数，由子类实现
     */
    protected abstract void setDifficulty();

    //public static Random sharedRandom = new Random(); // 默认种子，后续会被覆盖

    public static void setRandomSeed(long seed) {
        Log.i("Game", "setRandomSeed called with seed=" + seed);
        sharedRandom.setSeed(seed);
        Log.i("Game", "After setSeed, first random=" + sharedRandom.nextInt(100));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(GameConfig.DESIGN_WIDTH, GameConfig.DESIGN_HEIGHT);
    }

    public Game(Context context, Handler handler) throws IOException, ClassNotFoundException {
        super(context);
        this.context = context;
        this.handler = handler;

        mbLoop = true;
        mPaint = new Paint();
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        this.setFocusable(true);
        ImageManager.initImage(context);

        // 初始化英雄机
        HeroAircraft.refreshHero();
        heroAircraft = HeroAircraft.getHeroAircraft();

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        ThreadFactory gameThread = r -> {
            Thread t = new Thread(r);
            t.setName("game thread");
            return t;
        };
        executorService = new ScheduledThreadPoolExecutor(1, gameThread);

        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        this.setFocusable(true);

        this.setOnTouchListener(new HeroController(this, heroAircraft));

        music = new MusicService(context);
    }

    /**
     * 游戏启动入口（模板方法）
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void action() {

        Runnable task = () -> {

            time += timeInterval;
            updateTimedShootEffects();

            // 周期性执行
            if (timeCountAndNewCycleJudge()) {
                // 新敌机产生
                for (int j = 0; j < enemyCreateNumber; j++) {
                    addEnemy();
                }
                // 飞机射出子弹
                shootAction();
            }

            // Boss机生成
            generateBoss();

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            // 道具移动
            propsMoveAction();

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            // 难度递增检测（每500分触发一次）
            if (score % 500 == 0 && score != 0) {
                if (difficultyUpdateFlag) {
                    difficultyUpdateFlag = false;
                    difficultyUpdate();
                }
            } else {
                difficultyUpdateFlag = true;
            }
        };
        // Keep update and draw on the same game thread to avoid concurrent
        // modification of the LinkedList-backed game object collections.
        task.run();
    }

    protected void updateTimedShootEffects() {
        heroAircraft.tickTimedShootBuff();
    }

    /**
     * 控制Boss敌机是否出现（按分数阈值，与 base 对齐）
     */
    protected void generateBoss() {
        if (!bossEnabled) return;
        if (score / BossEnemyScoreThreshold >= bosscnt && !isBossExist() && score != 0) {
            bosscnt = score / BossEnemyScoreThreshold + 1;
            factory = new edu.hitsz.factory.BossEnemyFactory();
            enemyAircrafts.add(factory.createAircraft(3, 0, BossEnemyHP));
            if (needBgm) {
                music.playBossBgm();
            }
        }
    }

    protected boolean isBossExist() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof BossEnemy) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按概率添加敌机（Mob / Elite / ElitePlus / ElitePro）
     * 子类可重写此方法完全自定义，或利用父类概率参数
     */
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

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        }
        return false;
    }

    protected void shootAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyBullets.addAll(enemyAircraft.shoot());
        }
        heroBullets.addAll(heroAircraft.shoot());
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) bullet.forward();
        for (BaseBullet bullet : enemyBullets) bullet.forward();
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) enemyAircraft.forward();
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) prop.forward();
    }

    public static void addScore(int num) {
        score += num;
    }

    public boolean isGameOverFlag() {
        return gameOverFlag;
    }

    /**
     * 碰撞检测：
     * 1. 敌机子弹攻击英雄
     * 2. 英雄子弹攻击/英雄撞击敌机
     * 3. 英雄获得道具
     */
    protected void crashCheckAction() {
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) continue;
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

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
                        awardScore(enemy);
                    }
                }
                if (enemy.crash(heroAircraft) || heroAircraft.crash(enemy)) {
                    enemy.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop) || prop.crash(heroAircraft)) {
                if (prop instanceof BombProp) {
                    attachBombTargets((BombProp) prop);
                }
                playPropSound(prop);
                prop.effect(heroAircraft);
                prop.vanish();
            }
        }
    }

    /**
     * 后处理：删除无效对象，检查游戏结束
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);

        if (heroAircraft.getHp() <= 0) {
            System.out.println("Game Over!");
            stopAudioAndThreads();

            if (LauncherActivity.isOnline) {
                awaitOnlineEnding();
            } else {
                finalizeOfflineResult();
            }
            mbLoop = false;
        }
    }

    //***********************
    //      Paint 各部分
    //***********************

    private void repaint() {
        synchronized (surfaceHolder) {
            draw();
        }
    }

    public void draw() {
        canvas = surfaceHolder.lockCanvas();
        if (surfaceHolder == null || canvas == null) return;

        canvas.drawBitmap(backGround, 0, this.backGroundTop - backGround.getHeight(), mPaint);
        canvas.drawBitmap(backGround, 0, this.backGroundTop, mPaint);
        backGroundTop += 1;
        if (backGroundTop == MainActivity.WINDOW_HEIGHT) backGroundTop = 0;

        paintImageWithPositionRevised(props);
        paintImageWithPositionRevised(enemyBullets);
        paintImageWithPositionRevised(heroBullets);
        paintImageWithPositionRevised(enemyAircrafts);

        canvas.drawBitmap(ImageManager.HERO_IMAGE,
                heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2,
                mPaint);

        paintScoreAndLife();
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    protected void paintImageWithPositionRevised(List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) return;
        for (AbstractFlyingObject obj : objects) {
            Bitmap image = obj.getImage();
            if (image == null) continue;

            // 子弹需要根据速度方向旋转
            if (obj instanceof HeroBullet || obj instanceof EnemyBullet || obj instanceof TracerBullet) {
                double angleRad = Math.atan2(obj.getSpeedY(), obj.getSpeedX());   // 弧度制方向角
                float angleDeg = (float) (angleRad * 180 / Math.PI);             // 转为度数
                angleDeg += 90;   // 与桌面版对齐，因为图片默认朝上，需要 +90° 使子弹指向速度方向
                if (obj instanceof EnemyBullet) {
                    // 敌机子弹贴图方向与英雄子弹相反，额外旋转 180°
                    angleDeg += 180;
                }

                canvas.save();                         // 保存当前画布状态
                canvas.translate(obj.getLocationX(), obj.getLocationY()); // 将画布原点移动到对象中心
                canvas.rotate(angleDeg);               // 旋转画布
                canvas.drawBitmap(image,
                        -image.getWidth() / 2f,
                        -image.getHeight() / 2f,
                        mPaint);                       // 绘制图片（左上角位于 (-w/2, -h/2)）
                canvas.restore();                      // 恢复画布状态
            } else {
                // 普通对象：直接绘制，不旋转
                canvas.drawBitmap(image,
                        obj.getLocationX() - image.getWidth() / 2,
                        obj.getLocationY() - image.getHeight() / 2,
                        mPaint);
            }
        }
    }

    private void paintScoreAndLife() {
        int x = 30;
        int y = 60;
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(55);
        canvas.drawText("SCORE:" + score, x, y, mPaint);
        canvas.drawText("LIFE:" + heroAircraft.getHp(), x, y + 60, mPaint);
        if (LauncherActivity.isOnline) {
            canvas.drawText("OPPONENT NAME:" + OnlineActivity.getOpponentName(), x + 450, y, mPaint);
            canvas.drawText("OPPONENT SCORE:" + OnlineActivity.getOpponentScore(), x + 450, y + 60, mPaint);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        new Thread(this).start();
        Log.i(TAG, "start surface view thread");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        MainActivity.WINDOW_WIDTH = GameConfig.DESIGN_WIDTH;
        MainActivity.WINDOW_HEIGHT = GameConfig.DESIGN_HEIGHT;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        mbLoop = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        setDifficulty();
        if (needBgm) music.playBgm();

        while (mbLoop) {
            synchronized (surfaceHolder) {
                action();
                draw();
            }
        }

        Message message = Message.obtain();
        message.what = 1;
        message.obj = user;
        handler.sendMessage(message);
    }
}
