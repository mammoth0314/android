package com.example.studentscoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.UserAccount;
import com.example.studentscoremanager.util.PrefUtils;
import com.example.studentscoremanager.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etPassword;
    private CheckBox cbRemember;
    private ScoreDao scoreDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        scoreDao = new ScoreDao(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvHint = findViewById(R.id.tvLoginHint);

        String rememberedAccount = PrefUtils.getRememberedAccount(this);
        if (!TextUtils.isEmpty(rememberedAccount)) {
            etUsername.setText(rememberedAccount);
            cbRemember.setChecked(PrefUtils.isRememberEnabled(this));
        }

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvHint.setText("默认管理员：admin / 123456");
    }

    private void handleLogin() {
        String username = getText(etUsername);
        String password = getText(etPassword);
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showDialog("请输入账号和密码");
            return;
        }

        UserAccount account = scoreDao.login(username, password);
        if (account == null) {
            showDialog("账号或密码错误，请重新输入");
            return;
        }

        if (cbRemember.isChecked()) {
            PrefUtils.saveRememberedAccount(this, username);
        } else {
            PrefUtils.clearRememberedAccount(this);
        }
        SessionManager.save(this, account.getUsername(), account.getRole(), account.getRealName());
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", account.getUsername());
        intent.putExtra("role", account.getRole());
        intent.putExtra("realName", account.getRealName());
        startActivity(intent);
        finish();
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("登录提示")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
}
