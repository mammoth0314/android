package com.example.studentscoremanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.studentscoremanager.adapter.ScoreAdapter;
import com.example.studentscoremanager.db.DBHelper;
import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.ScoreRecord;
import com.example.studentscoremanager.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ROLE = "role";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_REAL_NAME = "realName";

    private final List<ScoreRecord> data = new ArrayList<>();
    private ScoreAdapter adapter;
    private ScoreDao scoreDao;
    private ListView listView;
    private TextView tvEmpty;
    private String username;
    private String role;
    private String realName;
    private String currentOrderBy = DBHelper.COL_ID + " DESC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = getIntent().getStringExtra(EXTRA_USERNAME);
        role = getIntent().getStringExtra(EXTRA_ROLE);
        realName = getIntent().getStringExtra(EXTRA_REAL_NAME);

        if (TextUtils.isEmpty(username)) {
            username = SessionManager.getUsername(this);
        }
        if (TextUtils.isEmpty(role)) {
            role = SessionManager.getRole(this);
        }
        if (TextUtils.isEmpty(realName)) {
            realName = SessionManager.getRealName(this);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("学生成绩管理系统");
            getSupportActionBar().setSubtitle(buildSubtitle());
        }

        scoreDao = new ScoreDao(this);
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ScoreAdapter(this, data);
        listView.setAdapter(adapter);
        listView.setEmptyView(tvEmpty);

        if (isTeacherOrAdmin()) {
            registerForContextMenu(listView);
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            ScoreRecord record = data.get(position);
            Toast.makeText(this,
                    "课程：" + record.getCourseName() + "，学生：" + record.getStudentName()
                            + "，成绩：" + (record.getScore() == null ? "未评分" : record.getScore() + "分"),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private String buildSubtitle() {
        String displayRole;
        if (DBHelper.ROLE_ADMIN.equals(role)) {
            displayRole = "管理员";
        } else if (DBHelper.ROLE_TEACHER.equals(role)) {
            displayRole = "老师";
        } else {
            displayRole = "学生";
        }
        return displayRole + "：" + (TextUtils.isEmpty(realName) ? username : realName);
    }

    private boolean isTeacherOrAdmin() {
        return DBHelper.ROLE_ADMIN.equals(role) || DBHelper.ROLE_TEACHER.equals(role);
    }

    private boolean isStudent() {
        return DBHelper.ROLE_STUDENT.equals(role);
    }

    private void loadData() {
        data.clear();
        data.addAll(scoreDao.getVisibleScores(role, username, currentOrderBy));
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem addScore = menu.findItem(R.id.action_add_score);
        MenuItem addCourse = menu.findItem(R.id.action_add_course);
        MenuItem selectCourse = menu.findItem(R.id.action_select_course);
        MenuItem clearAll = menu.findItem(R.id.action_clear_all);

        if (isStudent()) {
            addScore.setVisible(false);
            addCourse.setVisible(false);
            clearAll.setVisible(false);
            selectCourse.setVisible(true);
        } else if (DBHelper.ROLE_TEACHER.equals(role)) {
            selectCourse.setVisible(false);
            clearAll.setVisible(false);
        } else {
            selectCourse.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_score) {
            startActivity(new Intent(this, EditActivity.class)
                    .putExtra(EXTRA_ROLE, role)
                    .putExtra(EXTRA_USERNAME, username)
                    .putExtra(EXTRA_REAL_NAME, realName));
            return true;
        } else if (id == R.id.action_add_course) {
            startActivity(new Intent(this, CourseActivity.class)
                    .putExtra(EXTRA_USERNAME, username)
                    .putExtra(EXTRA_REAL_NAME, realName));
            return true;
        } else if (id == R.id.action_select_course) {
            startActivity(new Intent(this, SelectCourseActivity.class)
                    .putExtra(EXTRA_USERNAME, username)
                    .putExtra(EXTRA_REAL_NAME, realName));
            return true;
        } else if (id == R.id.action_clear_all) {
            confirmClearAll();
            return true;
        } else if (id == R.id.action_refresh) {
            loadData();
            return true;
        } else if (id == R.id.action_sort_score_asc) {
            currentOrderBy = DBHelper.COL_SCORE + " ASC, " + DBHelper.COL_ID + " ASC";
            loadData();
            Toast.makeText(this, "已按成绩升序排序", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sort_score_desc) {
            currentOrderBy = DBHelper.COL_SCORE + " DESC, " + DBHelper.COL_ID + " DESC";
            loadData();
            Toast.makeText(this, "已按成绩降序排序", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            SessionManager.clear(this);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmClearAll() {
        if (data.isEmpty()) {
            Toast.makeText(this, "当前没有可清空的数据", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("清空成绩")
                .setMessage("确定要删除全部成绩记录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    int count = scoreDao.deleteAllScores();
                    Toast.makeText(this, "已清空 " + count + " 条记录", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView && isTeacherOrAdmin()) {
            new MenuInflater(this).inflate(R.menu.menu_context, menu);
            menu.setHeaderTitle("请选择操作");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null || info.position < 0 || info.position >= data.size()) {
            return super.onContextItemSelected(item);
        }
        ScoreRecord selected = data.get(info.position);

        if (item.getItemId() == R.id.action_edit_score) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_SCORE_ID, selected.getId());
            intent.putExtra(EXTRA_ROLE, role);
            intent.putExtra(EXTRA_USERNAME, username);
            intent.putExtra(EXTRA_REAL_NAME, realName);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_delete_score) {
            confirmDelete(selected);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void confirmDelete(ScoreRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("删除成绩")
                .setMessage("确定删除 " + record.getStudentName() + " 的 " + record.getCourseName() + " 成绩吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    scoreDao.deleteScore(record.getId());
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
