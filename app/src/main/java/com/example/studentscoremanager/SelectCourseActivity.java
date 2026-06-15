package com.example.studentscoremanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.CourseItem;
import com.example.studentscoremanager.model.UserAccount;

import java.util.ArrayList;
import java.util.List;

public class SelectCourseActivity extends AppCompatActivity {
    private final List<CourseItem> courses = new ArrayList<>();
    private ScoreDao scoreDao;
    private String username;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_course);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("选择课程");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scoreDao = new ScoreDao(this);
        username = getIntent().getStringExtra(MainActivity.EXTRA_USERNAME);
        listView = findViewById(R.id.listView);
        Button btnConfirm = findViewById(R.id.btnConfirm);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, new ArrayList<>());
        listView.setAdapter(adapter);

        loadCourses();
        btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void loadCourses() {
        courses.clear();
        courses.addAll(scoreDao.getAvailableCoursesForStudent(username));
        List<String> names = new ArrayList<>();
        for (CourseItem course : courses) {
            names.add(course.getCourseName() + "  |  " + course.getTeacherName());
        }
        adapter.clear();
        adapter.addAll(names);
        adapter.notifyDataSetChanged();
    }

    private void confirmSelection() {
        UserAccount student = scoreDao.getUserByUsername(username);
        if (student == null) {
            Toast.makeText(this, "学生信息不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = 0;
        for (int i = 0; i < courses.size(); i++) {
            if (listView.isItemChecked(i)) {
                long result = scoreDao.enrollStudent(student.getUsername(), student.getRealName(), courses.get(i));
                if (result > 0) {
                    count++;
                }
            }
        }
        Toast.makeText(this, "已选择 " + count + " 门课程", Toast.LENGTH_SHORT).show();
        finish();
    }
}
