package edu.hitsz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

import edu.hitsz.DAO.User;
import edu.hitsz.application.GameConfig;
import edu.hitsz.game.Game;
import edu.hitsz.game.EasyGame;
import edu.hitsz.game.HardGame;
import edu.hitsz.game.MediumGame;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";

    private int gameType = 0;

    private Handler buildGameOverHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d(TAG, "handleMessage");
                if (msg.what != 1 || LauncherActivity.isOnline) {
                    return;
                }
                Intent intent = new Intent(GameActivity.this, RankingActivity.class);
                User user = (User) msg.obj;
                intent.putExtra("user_score", user.getScore());
                intent.putExtra("user_time", user.getTime());
                intent.putExtra("should_prompt_name", true);
                intent.putExtra("difficulty", getDifficultyLabel(gameType));
                Toast.makeText(GameActivity.this, "GameOver", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        };
    }

    private Game createGameView(Handler handler) throws IOException, ClassNotFoundException {
        switch (gameType) {
            case 1:
                return new MediumGame(this, handler);
            case 3:
                return new HardGame(this, handler);
            case 2:
            default:
                return new EasyGame(this, handler);
        }
    }

    private FrameLayout buildGameContainer(Game gameView) {
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.BLACK);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams gameParams = new FrameLayout.LayoutParams(
                GameConfig.DESIGN_WIDTH,
                GameConfig.DESIGN_HEIGHT);
        gameParams.gravity = Gravity.CENTER;
        rootLayout.addView(gameView, gameParams);
        return rootLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.WINDOW_WIDTH = GameConfig.DESIGN_WIDTH;
        MainActivity.WINDOW_HEIGHT = GameConfig.DESIGN_HEIGHT;

        if (getIntent() != null) {
            gameType = getIntent().getIntExtra("gameType", 1);
        }

        Handler handler = buildGameOverHandler();
        Game basGameView;
        try {
            basGameView = createGameView(handler);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        setContentView(buildGameContainer(basGameView));
    }

    private String getDifficultyLabel(int gameType) {
        if (gameType == 1) {
            return "普通";
        }
        if (gameType == 3) {
            return "困难";
        }
        return "简单";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
