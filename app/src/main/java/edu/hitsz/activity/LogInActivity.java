package edu.hitsz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
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

public class LogInActivity extends AppCompatActivity {
    private static final int LOGIN_OK = 0x11;
    private static final int LOGIN_PASSWORD_FAILED = 0x22;
    private static final int LOGIN_USER_MISSING = 0x33;

    private final static String TAG = "LogInActivity";

    private Button loginButton;
    private Button registerButton;
    private EditText login_ID;
    private EditText login_password;

    private String user_ID;
    private String user_password;

    private PrintWriter out;
    private Socket socket;
    public Handler handler;

    private void jumpToRegister() {
        startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
    }

    private void submitLogin() {
        user_ID = login_ID.getText().toString().trim();
        user_password = login_password.getText().toString();
        new Thread(new Connect()).start();
    }

    private Handler buildUiHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case LOGIN_OK:
                        Toast.makeText(LogInActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LogInActivity.this, OnlineActivity.class));
                        break;
                    case LOGIN_PASSWORD_FAILED:
                        Toast.makeText(LogInActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    case LOGIN_USER_MISSING:
                        Toast.makeText(LogInActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_in);

        loginButton = findViewById(R.id.login_in_btn);
        registerButton = findViewById(R.id.register_btn);
        login_ID = findViewById(R.id.login_ID);
        login_password = findViewById(R.id.login_password);

        login_ID.setTextColor(Color.WHITE);
        login_password.setTextColor(Color.WHITE);

        registerButton.setOnClickListener(v -> jumpToRegister());
        loginButton.setOnClickListener(v -> submitLogin());
        handler = buildUiHandler();
    }

    private void dispatchResult(int what, String payload) {
        Message message = Message.obtain();
        message.what = what;
        message.obj = payload;
        handler.sendMessage(message);
    }

    public class Connect implements Runnable {

        private BufferedReader in;

        @Override
        public void run() {
            try {
                socket = MainActivity.connectToServerWithFallback(MainActivity.AUTH_SERVER_PORT, 5000);

                /**
                 * 向服务器发送请求码
                 */
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

                /**
                 * 客户端组装 JSON对象
                 */
                JSONObject jsonObject = new JSONObject();
                //发送操作码
                jsonObject.put("operation", "login");
                jsonObject.put("ID",user_ID);
                jsonObject.put("PSW",user_password);
                Log.e(TAG, jsonObject.toString());
                // 发送 JSON 对象给服务器
                out.println(jsonObject.toString());

                /**
                 * 判断从服务器端返回的信息
                 */
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = in.readLine(); // 读取另一头传过来的数据
                System.out.println(line+"45678");

                if (Objects.equals(line, "login_success")) {
                    dispatchResult(LOGIN_OK, "login_success");
                } else if (Objects.equals(line, "password_failed")) {
                    dispatchResult(LOGIN_PASSWORD_FAILED, "password_failed");
                } else if (Objects.equals(line, "userID_failed")) {
                    dispatchResult(LOGIN_USER_MISSING, "userID_failed");
                }

                socket.shutdownOutput();
                socket.close(); // 关闭socket

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
