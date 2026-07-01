package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.hitsz.R;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.EliteProEnemy;
import edu.hitsz.aircraft.FriendAircraft;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.bullet.TracerBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletPlusProp;
import edu.hitsz.prop.BulletProp;
import edu.hitsz.prop.BulletPlusProp;

import java.util.HashMap;
import java.util.Map;

/**
 * 综合管理图片的加载、访问
 *
 * @author hitsz
 */
public class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND1_IMAGE;
    public static Bitmap BACKGROUND2_IMAGE;
    public static Bitmap BACKGROUND3_IMAGE;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_IMAGE;
    public static Bitmap ELITEPLUS_ENEMY_IMAGE;       // 新增：加强精英
    public static Bitmap ELITEPRO_ENEMY_IMAGE;        // 新增：专业精英
    public static Bitmap BOSS_IMAGE;
    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap TRACER_BULLET_IMAGE;
    public static Bitmap PROP_BULLETPLUS_IMAGE;

    /**
     * 加载图片并缩放到指定倍数
     */
    private static Bitmap loadAndScale(Context context, int resId, float scale) {
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resId);
        if (original == null) return null;
        int newWidth = Math.round(original.getWidth() * scale);
        int newHeight = Math.round(original.getHeight() * scale);
        Bitmap scaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
        if (scaled != original) {
            original.recycle();  // 释放原始图片内存
        }
        return scaled;
    }

    public static void initImage(Context context) {
        // 辅助方法：加载并缩放到 2.5 倍
        BACKGROUND1_IMAGE = loadAndScale(context, R.drawable.bg, 2.5f);
        BACKGROUND2_IMAGE = loadAndScale(context, R.drawable.bg2, 2.5f);
        BACKGROUND3_IMAGE = loadAndScale(context, R.drawable.bg3, 2.5f);
        HERO_IMAGE        = loadAndScale(context, R.drawable.hero, 2.5f);
        MOB_ENEMY_IMAGE   = loadAndScale(context, R.drawable.mob, 2.5f);
        ELITE_IMAGE       = loadAndScale(context, R.drawable.elite, 2.5f);
        BOSS_IMAGE        = loadAndScale(context, R.drawable.boss, 2.5f);
        HERO_BULLET_IMAGE = loadAndScale(context, R.drawable.bullet_hero, 2.5f);
        ENEMY_BULLET_IMAGE= loadAndScale(context, R.drawable.bullet_enemy, 2.5f);
        PROP_BULLET_IMAGE = loadAndScale(context, R.drawable.prop_bullet, 2.5f);
        PROP_BLOOD_IMAGE  = loadAndScale(context, R.drawable.prop_blood, 2.5f);
        PROP_BOMB_IMAGE   = loadAndScale(context, R.drawable.prop_bomb, 2.5f);
        ELITEPLUS_ENEMY_IMAGE = loadAndScale(context, R.drawable.elite_plus, 2.5f);
        ELITEPRO_ENEMY_IMAGE  = loadAndScale(context, R.drawable.elite_pro, 2.5f);
        TRACER_BULLET_IMAGE   = loadAndScale(context, R.drawable.bullet_tracer, 2.5f);
        PROP_BULLETPLUS_IMAGE = loadAndScale(context, R.drawable.prop_bullet_plus, 2.5f);
        // 英雄机
        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(FriendAircraft.class.getName(), HERO_IMAGE);

        // 敌机：新增类复用现有图片
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(),       MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(),     ELITE_IMAGE);
        CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITEPLUS_ENEMY_IMAGE);  // 复用精英图片
        CLASSNAME_IMAGE_MAP.put(EliteProEnemy.class.getName(),  ELITEPRO_ENEMY_IMAGE);  // 复用精英图片
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(),      BOSS_IMAGE);

        // 子弹
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(),   HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(),  ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(TracerBullet.class.getName(), TRACER_BULLET_IMAGE); // 复用敌机子弹图片

        // 道具
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(),     PROP_BLOOD_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(),      PROP_BOMB_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletProp.class.getName(),    PROP_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletPlusProp.class.getName(), PROP_BULLETPLUS_IMAGE); // 复用子弹道具图片
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) return null;
        return get(obj.getClass().getName());
    }
}
