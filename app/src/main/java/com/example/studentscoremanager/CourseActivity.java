package com.example.studentscoremanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.UserAccount;

public class CourseActivity extends AppCompatActivity {
    private EditText etCourseName;
    private ScoreDao scoreDao;
    private String username;
    private String realName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("添加课程");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scoreDao = new ScoreDao(this);
        username = getIntent().getStringExtra(MainActivity.EXTRA_USERNAME);
        realName = getIntent().getStringExtra(MainActivity.EXTRA_REAL_NAME);
        etCourseName = findViewById(R.id.etCourseName);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> saveCourse());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveCourse() {
        String courseName = getText(etCourseName);
        if (TextUtils.isEmpty(courseName)) {
            showDialog("请输入课程名称");
            return;
        }
        UserAccount teacher = scoreDao.getUserByUsername(username);
        if (teacher == null) {
            showDialog("教师信息不存在");
            return;
        }
        long result = scoreDao.addCourse(courseName, teacher.getUsername(), teacher.getRealName());
        if (result > 0) {
            Toast.makeText(this, "课程添加成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            showDialog("课程添加失败");
        }
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
}

