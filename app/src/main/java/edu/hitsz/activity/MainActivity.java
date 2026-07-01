package edu.hitsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import edu.hitsz.BuildConfig;
import edu.hitsz.R;
import edu.hitsz.application.GameConfig;
import edu.hitsz.game.Game;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static int WINDOW_WIDTH;
    public static int WINDOW_HEIGHT;
    // Android emulator should use 10.0.2.2 to reach the host machine.
    public static final String SERVER_HOST = BuildConfig.SERVER_HOST;
    public static final int AUTH_SERVER_PORT = BuildConfig.AUTH_SERVER_PORT;
    public static final int GAME_SERVER_PORT = BuildConfig.GAME_SERVER_PORT;
    private Button easyButton;
    private Button normalButton;
    private Button hardButton;
    private Button onlineButton;
    private Button scoreButton;
    private Button bgmButton;
    private Button soundButton;

    private void bindViews() {
        easyButton = findViewById(R.id.easy_btn);
        normalButton = findViewById(R.id.normal_btn);
        hardButton = findViewById(R.id.hard_btn);
        onlineButton = findViewById(R.id.online_btn);
        scoreButton = findViewById(R.id.score_btn);
        bgmButton = findViewById(R.id.bgm_btn);
        soundButton = findViewById(R.id.sound_btn);
    }

    private void installModeButtons() {
        easyButton.setOnClickListener(view -> startOfflineGame(2));
        normalButton.setOnClickListener(view -> startOfflineGame(1));
        hardButton.setOnClickListener(view -> startOfflineGame(3));
        onlineButton.setOnClickListener(view -> {
            LauncherActivity.isOnline = true;
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
        });
        scoreButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RankingActivity.class);
            intent.putExtra("should_prompt_name", false);
            startActivity(intent);
        });
    }

    private void installAudioButtons() {
        bgmButton.setOnClickListener(view -> {
            Game.needBgm = !Game.needBgm;
            refreshAudioButtons();
        });

        soundButton.setOnClickListener(view -> {
            Game.needSound = !Game.needSound;
            refreshAudioButtons();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        bindViews();
        getScreenHW();
        refreshAudioButtons();
        installAudioButtons();
        installModeButtons();
    }

    private void startOfflineGame(int gameType) {
        LauncherActivity.isOnline = false;
        OffLineActivity.gameType = gameType;
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("gameType", gameType);
        startActivity(intent);
    }

    private void refreshAudioButtons() {
        bgmButton.setText(Game.needBgm ? "背景音乐：开" : "背景音乐：关");
        soundButton.setText(Game.needSound ? "游戏音效：开" : "游戏音效：关");
    }

    public void getScreenHW() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WINDOW_WIDTH = GameConfig.DESIGN_WIDTH;
        WINDOW_HEIGHT = GameConfig.DESIGN_HEIGHT;
        Log.i(TAG, "screenWidth : " + WINDOW_WIDTH + " screenHeight : " + WINDOW_HEIGHT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static Socket connectToServerWithFallback(int port, int timeoutMs) throws IOException {
        String[] hosts = new String[]{SERVER_HOST, "10.0.2.2", "127.0.0.1", "localhost"};
        IOException lastError = null;
        for (String host : hosts) {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(host, port), timeoutMs);
                return socket;
            } catch (IOException e) {
                lastError = e;
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new IOException("No server host candidates available");
    }

}
