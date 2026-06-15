package com.example.studentscoremanager;

import android.os.Bundle;
import android.view.ContextMenu;
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

import com.example.studentscoremanager.adapter.UserAdapter;
import com.example.studentscoremanager.db.DBHelper;
import com.example.studentscoremanager.db.ScoreDao;
import com.example.studentscoremanager.model.UserAccount;

import java.util.ArrayList;
import java.util.List;

public class UserManageActivity extends AppCompatActivity {
    private final List<UserAccount> data = new ArrayList<>();
    private UserAdapter adapter;
    private ScoreDao scoreDao;
    private ListView listView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("用户管理");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scoreDao = new ScoreDao(this);
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);
        adapter = new UserAdapter(this, data);
        listView.setAdapter(adapter);
        listView.setEmptyView(tvEmpty);
        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        data.clear();
        data.addAll(scoreDao.getAllUsers());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            new MenuInflater(this).inflate(R.menu.menu_user_context, menu);
            menu.setHeaderTitle("用户操作");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null || info.position < 0 || info.position >= data.size()) {
            return super.onContextItemSelected(item);
        }
        UserAccount selected = data.get(info.position);
        if (item.getItemId() == R.id.action_delete_user) {
            confirmDeleteUser(selected);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void confirmDeleteUser(UserAccount user) {
        if (DBHelper.ROLE_ADMIN.equals(user.getRole())) {
            Toast.makeText(this, "默认管理员不能删除", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("删除用户")
                .setMessage("确定删除 " + user.getRealName() + " 吗？相关课程、成绩和选课数据会一起清理。")
                .setPositiveButton("删除", (dialog, which) -> {
                    scoreDao.deleteUser(user.getUsername());
                    Toast.makeText(this, "用户已删除", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
