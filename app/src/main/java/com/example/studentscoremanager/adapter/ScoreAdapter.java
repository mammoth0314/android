package com.example.studentscoremanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.studentscoremanager.R;
import com.example.studentscoremanager.model.ScoreRecord;

import java.util.List;

public class ScoreAdapter extends BaseAdapter {
    private final List<ScoreRecord> data;
    private final LayoutInflater inflater;

    public ScoreAdapter(Context context, List<ScoreRecord> data) {
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ScoreRecord getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_student, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScoreRecord item = getItem(position);
        holder.tvName.setText(item.getStudentName());
        holder.tvScore.setText(item.getScore() == null ? "未录入" : item.getScore() + " 分");
        holder.tvStudentNo.setText("账号：" + item.getStudentUsername());
        holder.tvCourse.setText("课程：" + item.getCourseName());
        holder.tvId.setText("老师：" + item.getTeacherName());
        return convertView;
    }

    private static class ViewHolder {
        final TextView tvName;
        final TextView tvScore;
        final TextView tvStudentNo;
        final TextView tvCourse;
        final TextView tvId;

        ViewHolder(View view) {
            tvName = view.findViewById(R.id.tvName);
            tvScore = view.findViewById(R.id.tvScore);
            tvStudentNo = view.findViewById(R.id.tvStudentNo);
            tvCourse = view.findViewById(R.id.tvCourse);
            tvId = view.findViewById(R.id.tvId);
        }
    }
}
