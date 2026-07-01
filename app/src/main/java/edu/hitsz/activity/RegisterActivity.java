package edu.hitsz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import edu.hitsz.R;

public class RegisterActivity extends AppCompatActivity {
    private static final int REGISTER_OK = 0x44;
    private static final int REGISTER_EXISTS = 0x55;
    private static final int REGISTER_ERROR = 0x66;

    private final static String TAG = "RegisterActivity";

    private Button login;
    private EditText register_ID;
    private EditText register_password;
    private EditText register_confirm_password;

    private String user_ID;
    private String user_password;
    private String user_confirm_password;

    private Handler handler;

    private boolean passwordsMatched() {
        return user_password != null && user_password.equals(user_confirm_password);
    }

    private void submitRegister() {
        user_ID = register_ID.getText().toString().trim();
        user_password = register_password.getText().toString();
        user_confirm_password = register_confirm_password.getText().toString();

        if (!passwordsMatched()) {
            Toast.makeText(RegisterActivity.this, "两次密码输入不同，请重新输入", Toast.LENGTH_LONG).show();
            return;
        }
        new Thread(new Connect()).start();
    }

    private void postRegisterResult(int what, String payload) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = payload;
        handler.sendMessage(message);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        login = findViewById(R.id.register_btn);
        register_ID = findViewById(R.id.register_ID);
        register_password = findViewById(R.id.register_password);
        register_confirm_password = findViewById(R.id.confirm_password);

        register_ID.setTextColor(Color.WHITE);
        register_password.setTextColor(Color.WHITE);
        register_confirm_password.setTextColor(Color.WHITE);

        login.setOnClickListener(v -> submitRegister());

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == REGISTER_OK) {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
                } else if (msg.what == REGISTER_EXISTS) {
                    Toast.makeText(RegisterActivity.this, "该用户ID已存在", Toast.LENGTH_SHORT).show();
                } else if (msg.what == REGISTER_ERROR) {
                    String errorMsg = (String) msg.obj;
                    Toast.makeText(RegisterActivity.this,
                            "NETWORK_ERROR".equals(errorMsg) ? "连接不到服务器，请检查网络" : "注册失败",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public class Connect implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = MainActivity.connectToServerWithFallback(MainActivity.AUTH_SERVER_PORT, 5000);

                /**
                 * 向服务器发送请求码
                 */
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                /**
                 * 客户端组装 JSON对象
                 */
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("operation", "register");
                jsonObject.put("ID",user_ID);
                jsonObject.put("PSW",user_password);
                Log.e(TAG, jsonObject.toString());
                // 发送 JSON 对象给服务器
                out.println(jsonObject.toString());

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = reader.readLine(); // 读取另一头传过来的数据
                System.out.println(line+"12345");

                if (Objects.equals(line, "register_success")) {
                    postRegisterResult(REGISTER_OK, "register_success");
                } else if (Objects.equals(line, "register_failed")) {
                    postRegisterResult(REGISTER_EXISTS, "register_failed");
                } else {
                    postRegisterResult(REGISTER_ERROR, "userID_failed");
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                postRegisterResult(REGISTER_ERROR, "NETWORK_ERROR");
            }
        }
    }
}
