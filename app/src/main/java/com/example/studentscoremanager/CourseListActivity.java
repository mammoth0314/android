package com.example.studentscoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studentscoremanager.adapter.CourseAdapter;
import com.example.studentscoremanager.db.DBHelper;
import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.CourseItem;

import java.util.ArrayList;
import java.util.List;

public class CourseListActivity extends AppCompatActivity {
    private final List<CourseItem> data = new ArrayList<>();
    private CourseAdapter adapter;
    private ScoreDao scoreDao;
    private String username;
    private String role;
    private String realName;
    private ListView listView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("课程管理");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        username = getIntent().getStringExtra(MainActivity.EXTRA_USERNAME);
        role = getIntent().getStringExtra(MainActivity.EXTRA_ROLE);
        realName = getIntent().getStringExtra(MainActivity.EXTRA_REAL_NAME);

        scoreDao = new ScoreDao(this);
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);
        adapter = new CourseAdapter(this, data);
        listView.setAdapter(adapter);
        listView.setEmptyView(tvEmpty);

        if (!DBHelper.ROLE_STUDENT.equals(role)) {
            registerForContextMenu(listView);
        }

        listView.setOnItemClickListener((parent, view, position, id) -> onCourseClick(data.get(position)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        data.clear();
        List<CourseItem> courses = DBHelper.ROLE_TEACHER.equals(role)
                ? scoreDao.getCoursesForTeacher(username)
                : DBHelper.ROLE_STUDENT.equals(role)
                ? scoreDao.getEnrolledCoursesForStudent(username)
                : scoreDao.getAllCourses();
        for (CourseItem course : courses) {
            course.setExtraInfo(buildExtraInfo(course));
            data.add(course);
        }
        adapter.notifyDataSetChanged();
    }

    private String buildExtraInfo(CourseItem course) {
        if (DBHelper.ROLE_STUDENT.equals(role)) {
            return "已选课程";
        }
        return "已选学生：" + scoreDao.getEnrollmentCount(course.getId()) + " 人";
    }

    private void onCourseClick(CourseItem course) {
        if (DBHelper.ROLE_STUDENT.equals(role)) {
            confirmUnenroll(course);
            return;
        }
        List<String> students = new ArrayList<>();
        for (com.example.studentscoremanager.model.UserAccount user : scoreDao.getEnrolledStudents(course.getId())) {
            students.add(user.getRealName() + "（" + user.getUsername() + "）");
        }
        String message = students.isEmpty() ? "当前还没有学生选这门课" : android.text.TextUtils.join("\n", students);
        new AlertDialog.Builder(this)
                .setTitle(course.getCourseName())
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void confirmUnenroll(CourseItem course) {
        new AlertDialog.Builder(this)
                .setTitle("取消选课")
                .setMessage("确定取消课程 “" + course.getCourseName() + "” 吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    scoreDao.unenrollStudent(username, course.getId());
                    Toast.makeText(this, "已取消选课", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course_list, menu);
        menu.findItem(R.id.action_add_course).setVisible(DBHelper.ROLE_TEACHER.equals(role));
        menu.findItem(R.id.action_select_course).setVisible(DBHelper.ROLE_STUDENT.equals(role));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_course) {
            startActivity(new Intent(this, CourseActivity.class)
                    .putExtra(MainActivity.EXTRA_USERNAME, username)
                    .putExtra(MainActivity.EXTRA_REAL_NAME, realName));
            return true;
        } else if (id == R.id.action_select_course) {
            startActivity(new Intent(this, SelectCourseActivity.class)
                    .putExtra(MainActivity.EXTRA_USERNAME, username)
                    .putExtra(MainActivity.EXTRA_REAL_NAME, realName));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            new MenuInflater(this).inflate(R.menu.menu_course_context, menu);
            menu.setHeaderTitle("课程操作");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null || info.position < 0 || info.position >= data.size()) {
            return super.onContextItemSelected(item);
        }
        CourseItem selected = data.get(info.position);
        if (item.getItemId() == R.id.action_edit_course) {
            startActivity(new Intent(this, CourseActivity.class)
                    .putExtra(MainActivity.EXTRA_USERNAME, username)
                    .putExtra(CourseActivity.EXTRA_COURSE_ID, selected.getId()));
            return true;
        } else if (item.getItemId() == R.id.action_delete_course) {
            confirmDeleteCourse(selected);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void confirmDeleteCourse(CourseItem course) {
        new AlertDialog.Builder(this)
                .setTitle("删除课程")
                .setMessage("确定删除课程 “" + course.getCourseName() + "” 吗？相关成绩和选课记录也会删除。")
                .setPositiveButton("删除", (dialog, which) -> {
                    scoreDao.deleteCourse(course.getId());
                    Toast.makeText(this, "课程已删除", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
