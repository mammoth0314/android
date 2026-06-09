package com.example.studentscoremanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.studentscoremanager.R;
import com.example.studentscoremanager.model.CourseItem;

import java.util.List;

public class CourseAdapter extends BaseAdapter {
    private final List<CourseItem> data;
    private final LayoutInflater inflater;

    public CourseAdapter(Context context, List<CourseItem> data) {
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CourseItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_course, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CourseItem item = getItem(position);
        holder.tvCourseName.setText(item.getCourseName());
        holder.tvTeacher.setText("任课教师：" + item.getTeacherName());
        return convertView;
    }

    private static class ViewHolder {
        final TextView tvCourseName;
        final TextView tvTeacher;

        ViewHolder(View view) {
            tvCourseName = view.findViewById(R.id.tvCourseName);
            tvTeacher = view.findViewById(R.id.tvTeacher);
        }
    }
}

