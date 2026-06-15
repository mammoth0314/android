package com.example.studentscoremanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.studentscoremanager.R;
import com.example.studentscoremanager.db.DBHelper;
import com.example.studentscoremanager.model.UserAccount;

import java.util.List;

public class UserAdapter extends BaseAdapter {
    private final List<UserAccount> data;
    private final LayoutInflater inflater;

    public UserAdapter(Context context, List<UserAccount> data) {
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UserAccount getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_user, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserAccount item = getItem(position);
        holder.tvName.setText(item.getRealName());
        holder.tvUsername.setText("账号：" + item.getUsername());
        holder.tvRole.setText("角色：" + roleLabel(item.getRole()));
        return convertView;
    }

    private String roleLabel(String role) {
        if (DBHelper.ROLE_ADMIN.equals(role)) {
            return "管理员";
        }
        if (DBHelper.ROLE_TEACHER.equals(role)) {
            return "老师";
        }
        return "学生";
    }

    private static class ViewHolder {
        final TextView tvName;
        final TextView tvUsername;
        final TextView tvRole;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tvUserName);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvRole = view.findViewById(R.id.tvRole);
        }
    }
}
