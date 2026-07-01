package edu.hitsz.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.hitsz.R;
import edu.hitsz.score.Score;
import edu.hitsz.score.ScoreDaoImpl;

public class OverActivity extends AppCompatActivity {

    private TextView text_1, text_2, text_3;
    private TextView titleText;
    private TextView winnerName;
    private Button returnBtn;

    private void bindViews() {
        titleText = findViewById(R.id.title_text);
        text_1 = findViewById(R.id.text1);
        text_2 = findViewById(R.id.text2);
        text_3 = findViewById(R.id.text3);
        winnerName = findViewById(R.id.winner_name);
        returnBtn = findViewById(R.id.return_button);
    }

    private void renderCoopResult(int myScore, int otherScore) {
        titleText.setText("Game Over");
        winnerName.setVisibility(View.GONE);
        text_1.setVisibility(View.VISIBLE);
        text_2.setVisibility(View.GONE);
        text_3.setVisibility(View.GONE);
        text_1.setText("你们的总分数是： " + (myScore + otherScore));
    }

    private void renderVersusResult(int myScore, int otherScore, String myName, String otherName) {
        titleText.setText("THE WINNER IS");
        winnerName.setVisibility(View.VISIBLE);
        text_1.setVisibility(View.VISIBLE);
        text_2.setVisibility(View.VISIBLE);
        text_3.setVisibility(View.VISIBLE);
        text_1.setText("你的分数是： " + myScore);
        text_2.setText("对手分数是： " + otherScore);

        if (myScore > otherScore) {
            winnerName.setText(myName);
            text_3.setText("恭喜你，战胜了对手！");
        } else if (myScore < otherScore) {
            winnerName.setText(otherName);
            text_3.setText("很遗憾，你输了！");
        } else {
            winnerName.setText(otherName);
            text_3.setText("好巧呀！平局!");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over);
        bindViews();

        int myScore = getIntent().getIntExtra("myScore",0);
        int otherScore = getIntent().getIntExtra("otherScore",0);
        String myName = getIntent().getStringExtra("myName");
        String otherName = getIntent().getStringExtra("opName");
        String mode = getIntent().getStringExtra("mode");

        if ("coop".equals(mode) && !getIntent().getBooleanExtra("coop_saved", false)) {
            saveCoopScoreIfNeeded(myName, myScore, otherName, otherScore);
            getIntent().putExtra("coop_saved", true);
        }

        if ("coop".equals(mode)) {
            renderCoopResult(myScore, otherScore);
        } else {
            renderVersusResult(myScore, otherScore, myName, otherName);
        }

        returnBtn.setOnClickListener(v -> startActivity(new Intent(OverActivity.this, MainActivity.class)));
    }

    private void saveCoopScoreIfNeeded(String myName, int myScore, String otherName, int otherScore) {
        if (myName == null || myName.isEmpty() || otherName == null || otherName.isEmpty()) {
            return;
        }
        ScoreDaoImpl scoreDao = new ScoreDaoImpl(this);
        String time = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date());
        Score coopScore = new Score(
                scoreDao.nextId(),
                myName,
                myScore,
                "合作",
                time,
                otherName,
                otherScore,
                "coop"
        );
        scoreDao.add(coopScore);
    }
}
