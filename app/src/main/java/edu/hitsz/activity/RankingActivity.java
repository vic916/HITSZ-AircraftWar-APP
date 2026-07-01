package edu.hitsz.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.List;

import edu.hitsz.R;
import edu.hitsz.score.Score;
import edu.hitsz.score.ScoreDaoImpl;

public class RankingActivity extends AppCompatActivity {

    ScoreDaoImpl scoreDao;
    List<Score> scoreList = null;
    String name;
    int score;
    String time;
    String difficulty;
    boolean shouldPromptName;
    String selectedDifficulty;

    Button returnButton;
    Button allFilterButton;
    Button easyFilterButton;
    Button normalFilterButton;
    Button hardFilterButton;
    Button coopFilterButton;
    TextView titleView;
    TextView hintView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankinglist);
        returnButton = findViewById(R.id.return_btn);
        allFilterButton = findViewById(R.id.filter_all_btn);
        easyFilterButton = findViewById(R.id.filter_easy_btn);
        normalFilterButton = findViewById(R.id.filter_normal_btn);
        hardFilterButton = findViewById(R.id.filter_hard_btn);
        coopFilterButton = findViewById(R.id.filter_coop_btn);
        titleView = findViewById(R.id.textView2);
        hintView = findViewById(R.id.ranking_hint);
        scoreDao = new ScoreDaoImpl(this);

        score = getIntent().getIntExtra("user_score",0);
        time = getIntent().getStringExtra("user_time");
        difficulty = getIntent().getStringExtra("difficulty");
        shouldPromptName = getIntent().getBooleanExtra("should_prompt_name", false);

        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = getDifficultyLabel(OffLineActivity.gameType);
        }

        selectedDifficulty = shouldPromptName ? difficulty : "全部";
        updateTitle();
        setupFilterButtons();

        if (shouldPromptName) {
            inputName();
        } else {
            showList();
        }

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

    }

    public void showList(){
        List<Score> allScores = scoreDao.getAllScores();
        scoreList = new LinkedList<>();
        for (Score item : allScores) {
            if ("全部".equals(selectedDifficulty) || selectedDifficulty.equals(item.getDifficulty())) {
                scoreList.add(item);
            }
        }

        ListView listView = findViewById(R.id.list);
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return scoreList.size();
            }

            @Override
            public Object getItem(int position) {
                return scoreList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return scoreList.get(position).getScoreId();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(RankingActivity.this)
                            .inflate(R.layout.ranking_item, parent, false);
                    holder = new ViewHolder();
                    holder.titleView = convertView.findViewById(R.id.entry_title);
                    holder.metaView = convertView.findViewById(R.id.entry_meta);
                    holder.detailOneView = convertView.findViewById(R.id.entry_detail_one);
                    holder.detailTwoView = convertView.findViewById(R.id.entry_detail_two);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Score curScore = scoreList.get(position);
                if (curScore.isCoop()) {
                    holder.titleView.setText("总分：" + curScore.getTotalScore());
                    holder.metaView.setText(curScore.getTime());
                    holder.detailOneView.setText("玩家1：" + curScore.getUsername() + "  分数：" + curScore.getScore());
                    holder.detailTwoView.setVisibility(View.VISIBLE);
                    holder.detailTwoView.setText("玩家2：" + curScore.getTeammateName() + "  分数：" + curScore.getTeammateScore());
                } else {
                    holder.titleView.setText(curScore.getUsername() + "  " + curScore.getScore());
                    holder.metaView.setText(curScore.getDifficulty());
                    holder.detailOneView.setText(curScore.getTime());
                    holder.detailTwoView.setVisibility(View.GONE);
                }
                return convertView;
            }
        };

        listView.setAdapter(adapter);
        updateHintText();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {

                AlertDialog alertDialog = new AlertDialog.Builder(RankingActivity.this)
                        .setTitle("提示")
                        .setMessage("确认删除该条记录吗")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
                                scoreDao.delete(scoreList.get(position).getScoreId());
                                scoreList.remove(position);

                                adapter.notifyDataSetChanged();
                                updateHintText();
                            }
                        })

                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create();
                alertDialog.show();
            }
        });
    }

    private void inputName() {
        EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("请输入名字以记录得分")
                .setView(input);
        //.setNegativeButton("取消", null)
        builder.setPositiveButton("确定", (dialogInterface, i) -> {
            name = input.getText().toString();
            if (name.isEmpty()){
                Toast.makeText(this, "输入为空", Toast.LENGTH_SHORT).show();
            }else{
                Score newScore = new Score(scoreDao.nextId(), name, score, difficulty, time);
                scoreDao.add(newScore);
                selectedDifficulty = difficulty;
                updateTitle();
                updateFilterButtonState();
                showList();
            }
        });

        builder.setNegativeButton("取消",(dialogInterface, i) -> {
            Toast.makeText(this, "您还未输入姓名", Toast.LENGTH_SHORT).show();
            showList();
        });

        builder.show();
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

    private void setupFilterButtons() {
        allFilterButton.setOnClickListener(v -> applyFilter("全部"));
        easyFilterButton.setOnClickListener(v -> applyFilter("简单"));
        normalFilterButton.setOnClickListener(v -> applyFilter("普通"));
        hardFilterButton.setOnClickListener(v -> applyFilter("困难"));
        coopFilterButton.setOnClickListener(v -> applyFilter("合作"));

        if (shouldPromptName) {
            allFilterButton.setEnabled(false);
        }
        updateFilterButtonState();
    }

    private void applyFilter(String difficultyFilter) {
        selectedDifficulty = difficultyFilter;
        updateTitle();
        updateFilterButtonState();
        showList();
    }

    private void updateTitle() {
        if ("全部".equals(selectedDifficulty)) {
            titleView.setText("总排行榜");
        } else {
            titleView.setText(selectedDifficulty + "模式排行榜");
        }
    }

    private void updateFilterButtonState() {
        allFilterButton.setSelected("全部".equals(selectedDifficulty));
        easyFilterButton.setSelected("简单".equals(selectedDifficulty));
        normalFilterButton.setSelected("普通".equals(selectedDifficulty));
        hardFilterButton.setSelected("困难".equals(selectedDifficulty));
        coopFilterButton.setSelected("合作".equals(selectedDifficulty));
    }

    private void updateHintText() {
        if ("合作".equals(selectedDifficulty)) {
            hintView.setText("按两人总分降序排列，展示双方 ID 与个人分数");
        } else if ("全部".equals(selectedDifficulty)) {
            hintView.setText("单机榜按难度保存，合作榜按两人总分保存");
        } else {
            hintView.setText("点击任意记录可删除");
        }
    }

    private static class ViewHolder {
        TextView titleView;
        TextView metaView;
        TextView detailOneView;
        TextView detailTwoView;
    }
}
