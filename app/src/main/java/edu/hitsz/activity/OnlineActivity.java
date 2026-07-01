package edu.hitsz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import edu.hitsz.R;
import edu.hitsz.application.GameConfig;
import edu.hitsz.game.CoopGame;
import edu.hitsz.game.Game;
import edu.hitsz.game.MediumGame;

public class OnlineActivity extends AppCompatActivity {

    private static final String TAG = "OnlineActivity";
    private boolean readySent = false; // 成员变量
    private static CoopGame coopGameInstance = null;  // 静态引用

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Handler handler;
    private Thread receiveThread;
    private boolean isActive = true;

    private TextView waitingText;
    private Button btnClassicMode;
    private Button btnCoopMode;
    private String selectedMode = null;
    private boolean gameStarted = false;

    public static volatile float opponentX = 0f;
    public static volatile float opponentY = 0f;
    private static int opponentScore = 0;
    private static int finalMyScore = 0;
    private static int finalOpponentScore = 0;
    private static boolean gameOverFlag = false;
    private static String opName;
    private static String myName;
    private static volatile boolean localGameEnded = false;
    private volatile boolean overActivityStarted = false;

    private int positionUpdateCount = 0;
    private static final int TOAST_INTERVAL = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        Log.i(TAG, "onCreate");

        gameOverFlag = false;
        opponentScore = 0;
        opponentX = 0f;
        opponentY = 0f;
        opName = null;
        myName = null;
        localGameEnded = false;
        finalMyScore = 0;
        finalOpponentScore = 0;

        waitingText = findViewById(R.id.textView);
        btnClassicMode = findViewById(R.id.btn_classic_mode);
        btnCoopMode = findViewById(R.id.btn_coop_mode);

        btnClassicMode.setEnabled(false);
        btnCoopMode.setEnabled(false);
        waitingText.setText("连接服务器中...");

        btnClassicMode.setOnClickListener(v -> {
            Log.i(TAG, "Classic mode button clicked");
            try {
                selectMode("classic");
            } catch (Exception e) {
                Log.e(TAG, "Error in classic mode click: " + e.getMessage(), e);
                Toast.makeText(OnlineActivity.this, "选择失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCoopMode.setOnClickListener(v -> {
            Log.i(TAG, "Coop mode button clicked");
            try {
                selectMode("coop");
            } catch (Exception e) {
                Log.e(TAG, "Error in coop mode click: " + e.getMessage(), e);
                Toast.makeText(OnlineActivity.this, "选择失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //if (!isActive || gameStarted) return;
                Log.i(TAG, "handleMessage: what=" + msg.what);
                try {
                    switch (msg.what) {
                        case 0x888:
                            if (!gameStarted) { // 只在游戏未开始时启用按钮
                                btnClassicMode.setEnabled(true);
                                btnCoopMode.setEnabled(true);
                                waitingText.setText("请选择游戏模式");
                                Log.i(TAG, "Network ready, buttons enabled");
                            }
                            break;
                        case 0x123:
                            String command = (String) msg.obj;
                            if (command != null && command.startsWith("start:") && !gameStarted) {
                                String mode = command.substring(6);
                                Log.i(TAG, "Starting game with mode: " + mode);
                                startGameByMode(mode);
                                gameStarted = true;
                            }
                            break;
                        case 0x124:
                            String result = (String) msg.obj;
                            if (result != null && result.startsWith("result:")) {
                                String[] scores = result.substring(7).split(",");
                                if (scores.length == 2) {
                                    finalMyScore = safeParseScore(scores[0]);
                                    finalOpponentScore = safeParseScore(scores[1]);
                                    opponentScore = finalOpponentScore;
                                }
                                Log.i(TAG, "Final result received: myScore=" + finalMyScore + ", opponentScore=" + finalOpponentScore);
                                openOverActivity();
                            }
                            break;
                        case 0x125:
                            String peerEnd = (String) msg.obj;
                            if (peerEnd != null && peerEnd.startsWith("peer_end:")) {
                                opponentScore = safeParseScore(peerEnd.substring(9));
                                if (coopGameInstance != null) {
                                    coopGameInstance.markOpponentEnded();
                                }
                                Log.i(TAG, "Opponent ended, final opponent score=" + opponentScore);
                            }
                            break;
                        case 0x456:
                            myName = (String) msg.obj;
                            Log.i(TAG, "myName: " + myName);
                            break;
                        case 0x789:
                            opName = (String) msg.obj;
                            Log.i(TAG, "opName: " + opName);
                            if (myName != null && opName != null) {
                                String name1 = myName, name2 = opName;
                                if (name1.compareTo(name2) > 0) {
                                    String tmp = name1;
                                    name1 = name2;
                                    name2 = tmp;
                                }
                                int seed = (name1 + name2).hashCode();
                                Game.setRandomSeed(seed);
                                boolean firstTargetIsHero = Game.sharedRandom.nextBoolean();
                                // 3. 根据当前玩家名字决定本端的追踪目标
                                boolean meTargetIsHero;
                                if (myName.equals(name1)) {
                                    meTargetIsHero = firstTargetIsHero;      // 名字较小的玩家按随机结果
                                } else {
                                    meTargetIsHero = !firstTargetIsHero;     // 名字较大的玩家取反
                                }
                                Game.setTracerTargetIsHero(meTargetIsHero);
                                Log.i(TAG, "Random seed set to " + seed);
                                Toast.makeText(OnlineActivity.this, "Random seed set to " + seed, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "myName or opName is null, cannot set seed");
                            }

                            //boolean trackHero = Game.sharedRandom.nextBoolean();
                            //Game.setTracerTargetIsHero(trackHero);
                            //Log.i(TAG, "Tracer target decision: " + (trackHero ? "Hero" : "Friend"));
                            break;
                        case 0x999:
                            String[] parts = ((String) msg.obj).split(",");
                            if (parts.length == 3) {
                                try {
                                    float x = Float.parseFloat(parts[0]);
                                    float y = Float.parseFloat(parts[1]);
                                    int frame = Integer.parseInt(parts[2]);
                                    if (coopGameInstance != null) {
                                        coopGameInstance.receiveOpponentPosition(x, y, frame);
                                    }
                                    // 更新静态变量（可选，用于其他显示）
                                    opponentX = x;
                                    opponentY = y;
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Failed to parse position data: " + msg.obj);
                                }
                            } else {
                                Log.e(TAG, "Invalid position format (expected 3 parts): " + msg.obj);
                            }
                            break;
                        case 0x1000:
                            try {
                                opponentScore = Integer.parseInt((String) msg.obj);
                                Log.d(TAG, "Opponent score: " + opponentScore);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Failed to parse opponent score: " + msg.obj);
                            }
                            break;
                        case 0x666:
                            Log.e(TAG, "Mode mismatch received");
                            Toast.makeText(OnlineActivity.this, "对手选择了不同的模式，请重新匹配", Toast.LENGTH_LONG).show();
                            finish();
                            break;
                        case 0x667:
                            Log.e(TAG, "Opponent disconnected before game over");
                            Toast.makeText(OnlineActivity.this, "对手提前断开连接，比赛结束", Toast.LENGTH_LONG).show();
                            finish();
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling message: " + e.getMessage(), e);
                }
            }
        };

        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Log.e("GAME_CONNECT", "连接游戏服务器 " + MainActivity.SERVER_HOST + ":" + MainActivity.GAME_SERVER_PORT);
                socket = MainActivity.connectToServerWithFallback(MainActivity.GAME_SERVER_PORT, 5000);
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                // 关键：只有连接成功才通知UI启用按钮
                handler.sendEmptyMessage(0x888);

                // 启动接收线程（保持原有逻辑）
                new Thread(() -> {
                    String msg;
                    int nameCount = 0;
                    try {
                        while (isActive && (msg = reader.readLine()) != null) {
                            Log.i(TAG, "recv: " + msg);
                            if (nameCount == 0) {
                                handler.obtainMessage(0x456, msg).sendToTarget();
                                nameCount++;
                            } else if (nameCount == 1) {
                                handler.obtainMessage(0x789, msg).sendToTarget();
                                nameCount++;
                            } else if (msg.startsWith("start:")) {
                                handler.obtainMessage(0x123, msg).sendToTarget();
                            } else if (msg.startsWith("pos:")) {
                                handler.obtainMessage(0x999, msg.substring(4)).sendToTarget();
                            } else if (msg.matches("\\d+")) {
                                handler.obtainMessage(0x1000, msg).sendToTarget();
                            } else if (msg.startsWith("result:")) {
                                handler.obtainMessage(0x124, msg).sendToTarget();
                            } else if (msg.startsWith("peer_end:")) {
                                handler.obtainMessage(0x125, msg).sendToTarget();
                            } else if ("error:mode_mismatch".equals(msg)) {
                                handler.sendEmptyMessage(0x666);
                            } else if ("error:opponent_disconnected".equals(msg)) {
                                handler.sendEmptyMessage(0x667);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "接收线程异常", e);
                    }
                }).start();

            } catch (IOException e) {
                Log.e("GAME_CONNECT", "连接失败: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(OnlineActivity.this, "游戏服务器连接失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void selectMode(String mode) {
        if (selectedMode != null || gameStarted) return;
        selectedMode = mode;
        btnClassicMode.setEnabled(false);
        btnCoopMode.setEnabled(false);
        waitingText.setText("已选择 " + ("classic".equals(mode) ? "经典对战模式" : "双人实时模式") + "，等待对手...");

        // 将网络发送操作放入子线程
        new Thread(() -> {
            if (writer != null) {
                try {
                    Log.i(TAG, "Sending mode to server: " + mode);
                    writer.println("mode:" + mode);
                    writer.flush();
                    Log.i(TAG, "Mode sent successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to send mode: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } else {
                Log.e(TAG, "writer is null");
                runOnUiThread(() -> {
                    Toast.makeText(this, "网络未就绪", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void startGameByMode(String mode) {
        Log.i(TAG, "startGameByMode: " + mode);

        // 确保设计尺寸常量已赋值
        MainActivity.WINDOW_WIDTH = GameConfig.DESIGN_WIDTH;
        MainActivity.WINDOW_HEIGHT = GameConfig.DESIGN_HEIGHT;

        if ("classic".equals(mode)) {
            try {
                Game classicGame = new MediumGame(OnlineActivity.this, handler);

                // === 固定分辨率包裹逻辑 ===
                FrameLayout rootLayout = new FrameLayout(this);
                rootLayout.setBackgroundColor(Color.BLACK);
                rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                FrameLayout.LayoutParams gameParams = new FrameLayout.LayoutParams(
                        GameConfig.DESIGN_WIDTH,
                        GameConfig.DESIGN_HEIGHT);
                gameParams.gravity = Gravity.CENTER;
                rootLayout.addView(classicGame, gameParams);
                setContentView(rootLayout);
                // ========================

                // 启动分数上报线程
                new Thread(() -> {
                    while (!classicGame.isGameOverFlag() && isActive) {
                        if (writer != null) {
                            try {
                                writer.println(Game.score);
                                writer.flush();
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to send score: " + e.getMessage());
                                break;
                            }
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (writer != null && isActive) {
                        markLocalGameEnded();
                        writer.println("end:" + Game.score);
                        writer.flush();
                    }
                    runOnUiThread(() -> setContentView(R.layout.activity_end));
                }).start();
            } catch (Exception e) {
                Log.e(TAG, "Error starting classic game: " + e.getMessage(), e);
                Toast.makeText(this, "启动游戏失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if ("coop".equals(mode)) {
            try {
                CoopGame coopGame = new CoopGame(OnlineActivity.this, handler, writer);
                coopGameInstance = coopGame;

                // === 固定分辨率包裹逻辑 ===
                FrameLayout rootLayout = new FrameLayout(this);
                rootLayout.setBackgroundColor(Color.BLACK);
                rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                FrameLayout.LayoutParams gameParams = new FrameLayout.LayoutParams(
                        GameConfig.DESIGN_WIDTH,
                        GameConfig.DESIGN_HEIGHT);
                gameParams.gravity = Gravity.CENTER;
                rootLayout.addView(coopGame, gameParams);
                setContentView(rootLayout);
                // ========================

                if (writer != null) {
                    new Thread(() -> {
                        writer.println("ready");
                        writer.flush();
                        Log.i(TAG, "Sent ready to server");
                    }).start();
                }
            } catch (Exception e) {
                Log.e(TAG, "创建 CoopGame 失败", e);
                Toast.makeText(this, "启动双人模式失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void closeConnections() {
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (writer != null) {
                writer.close();
                writer = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            Log.i(TAG, "Connections closed");
        } catch (IOException e) {
            Log.e(TAG, "Error closing connections: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        isActive = false;
        closeConnections();
    }

    private void openOverActivity() {
        if (overActivityStarted || isFinishing()) {
            return;
        }
        // 停止所有后台网络活动，避免在 Activity 销毁后收到消息
        isActive = false;
        closeConnections();  // 关闭 socket 和 reader/writer

        // 清除 CoopGame 静态引用，防止网络线程继续调用
        if (coopGameInstance != null) {
            coopGameInstance.stopGameLoop();  // 需在 CoopGame 中添加此方法
            coopGameInstance = null;
        }
        overActivityStarted = true;
        setGameOverFlag(true);
        Intent intent = new Intent(OnlineActivity.this, OverActivity.class);
        intent.putExtra("myScore", finalMyScore);
        intent.putExtra("otherScore", finalOpponentScore);
        intent.putExtra("opName", opName);
        intent.putExtra("myName", myName);
        intent.putExtra("mode", selectedMode);
        startActivity(intent);
        finish();
    }

    public static void markLocalGameEnded() {
        localGameEnded = true;
    }

    private int safeParseScore(String scoreText) {
        try {
            return Integer.parseInt(scoreText);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse score: " + scoreText, e);
            return 0;
        }
    }

    private void setGameOverFlag(boolean flag) { gameOverFlag = flag; }
    public static boolean isGameOverFlag() { return gameOverFlag; }
    public static int getOpponentScore() { return opponentScore; }
    public static String getOpponentName() { return opName; }
}
