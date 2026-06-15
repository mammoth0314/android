package com.example.studentscoremanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentscoremanager.db.DBHelper;
import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.UserAccount;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etRealName;
    private Spinner spRole;
    private ScoreDao scoreDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("用户注册");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scoreDao = new ScoreDao(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etRealName = findViewById(R.id.etRealName);
        spRole = findViewById(R.id.spRole);
        Button btnRegister = findViewById(R.id.btnRegister);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"老师", "学生"});
        spRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String username = getText(etUsername);
        String password = getText(etPassword);
        String confirm = getText(etConfirmPassword);
        String realName = getText(etRealName);
        String roleText = (String) spRole.getSelectedItem();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirm) || TextUtils.isEmpty(realName)) {
            showDialog("请完整填写所有注册信息");
            return;
        }
        if (!password.equals(confirm)) {
            showDialog("两次输入的密码不一致");
            return;
        }
        if (scoreDao.getUserByUsername(username) != null) {
            showDialog("该账号已存在");
            return;
        }

        String role = "老师".equals(roleText) ? DBHelper.ROLE_TEACHER : DBHelper.ROLE_STUDENT;
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setRealName(realName);
        long result = scoreDao.registerUser(user);
        if (result > 0) {
            Toast.makeText(this, "注册成功，请返回登录", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            showDialog("注册失败，请检查账号是否重复");
        }
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("注册提示")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
}
