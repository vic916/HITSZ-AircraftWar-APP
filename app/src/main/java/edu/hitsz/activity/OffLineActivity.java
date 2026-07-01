package edu.hitsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;
import edu.hitsz.game.Game;

public class OffLineActivity extends AppCompatActivity {

    public static int WINDOW_WIDTH;
    public static int WINDOW_HEIGHT;
    public static int gameType=0;
    private Switch bgm;

    private void launchSelectedMode(int selectedType) {
        gameType = selectedType;
        Intent gameIntent = new Intent(this, GameActivity.class);
        gameIntent.putExtra("gameType", selectedType);
        startActivity(gameIntent);
    }

    private void syncAudioSettings(boolean enabled) {
        Game.needBgm = enabled;
        Game.needSound = enabled;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        Button medium_btn = findViewById(R.id.medium_btn);
        Button easy_btn = findViewById(R.id.easy_btn);
        Button hard_btn = findViewById(R.id.hard_btn);
        bgm = findViewById(R.id.bgm);

        getScreenHW();

        bgm.setChecked(Game.needBgm && Game.needSound);
        medium_btn.setOnClickListener(view -> launchSelectedMode(1));
        easy_btn.setOnClickListener(view -> launchSelectedMode(2));
        hard_btn.setOnClickListener(view -> launchSelectedMode(3));
        bgm.setOnCheckedChangeListener((compoundButton, isChecked) -> syncAudioSettings(isChecked));
    }

    public void getScreenHW() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WINDOW_WIDTH = dm.widthPixels;
        WINDOW_HEIGHT = dm.heightPixels;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
