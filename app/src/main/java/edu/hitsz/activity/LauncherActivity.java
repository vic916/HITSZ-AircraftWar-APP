package edu.hitsz.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;

public class LauncherActivity extends AppCompatActivity {

    private Button offlineGame;
    private Button onlineGame;

    public static boolean isOnline = false;

    private void openTargetPage(Class<?> targetActivity, boolean onlineMode) {
        isOnline = onlineMode;
        startActivity(new Intent(this, targetActivity));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        offlineGame = findViewById(R.id.offline_btn);
        onlineGame = findViewById(R.id.online_btn);


        offlineGame.setOnClickListener(v -> openTargetPage(OffLineActivity.class, false));
        onlineGame.setOnClickListener(v -> openTargetPage(LogInActivity.class, true));
    }
}
