package edu.hitsz.application;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;

public class MusicService {
    private static final float DEFAULT_VOLUME = 1f;
    private static final float DEFAULT_RATE = 1.2f;
    private static final int SOUND_BOMB = 1;
    private static final int SOUND_HIT = 2;
    private static final int SOUND_SUPPLY = 3;
    private static final int SOUND_GAME_OVER = 4;

    private final MediaPlayer bgmMediaPlayer;
    private final MediaPlayer bgmBossMediaPlayer;
    private final SoundPool soundPool;
    private final Map<Integer, Integer> soundPoolMap = new HashMap<>();

    public MusicService(Context context) {
        bgmMediaPlayer = MediaPlayer.create(context, R.raw.bgm);
        bgmBossMediaPlayer = MediaPlayer.create(context, R.raw.bgm_boss);
        if (bgmMediaPlayer != null) {
            bgmMediaPlayer.setLooping(true);
        }
        if (bgmBossMediaPlayer != null) {
            bgmBossMediaPlayer.setLooping(true);
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
        soundPoolMap.put(SOUND_BOMB, soundPool.load(context, R.raw.bomb_explosion, 1));
        soundPoolMap.put(SOUND_HIT, soundPool.load(context, R.raw.bullet_hit, 1));
        soundPoolMap.put(SOUND_SUPPLY, soundPool.load(context, R.raw.get_supply, 1));
        soundPoolMap.put(SOUND_GAME_OVER, soundPool.load(context, R.raw.game_over, 1));
    }

    private void stopAndReset(MediaPlayer player) {
        if (player != null && player.isPlaying()) {
            player.pause();
            player.seekTo(0);
        }
    }

    private void startIfIdle(MediaPlayer player) {
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    private void playEffect(int soundKey) {
        Integer soundId = soundPoolMap.get(soundKey);
        if (soundId != null) {
            soundPool.play(soundId, DEFAULT_VOLUME, DEFAULT_VOLUME, 1, 0, DEFAULT_RATE);
        }
    }

    public void playBgm() {
        stopAndReset(bgmBossMediaPlayer);
        startIfIdle(bgmMediaPlayer);
    }

    public void playBossBgm() {
        stopAndReset(bgmMediaPlayer);
        startIfIdle(bgmBossMediaPlayer);
    }

    public void explosionSP() {
        playEffect(SOUND_BOMB);
    }

    public void hitSP() {
        playEffect(SOUND_HIT);
    }

    public void supplySP() {
        playEffect(SOUND_SUPPLY);
    }

    public void gameOverbgm() {
        playEffect(SOUND_GAME_OVER);
    }

    public void stopBgm() {
        stopAndReset(bgmMediaPlayer);
    }

    public void stopBossBgm() {
        stopAndReset(bgmBossMediaPlayer);
    }
}
