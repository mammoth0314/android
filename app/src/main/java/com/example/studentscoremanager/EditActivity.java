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
import com.example.studentscoremanager.model.CourseItem;
import com.example.studentscoremanager.model.ScoreRecord;
import com.example.studentscoremanager.model.UserAccount;
import com.example.studentscoremanager.util.SimpleItemSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE_ID = "score_id";

    private Spinner spCourse;
    private Spinner spStudent;
    private EditText etScore;
    private ScoreDao scoreDao;
    private String role;
    private String username;
    private long scoreId = -1L;
    private ScoreRecord existingRecord;
    private final List<CourseItem> courses = new ArrayList<>();
    private final List<UserAccount> students = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        scoreDao = new ScoreDao(this);
        role = getIntent().getStringExtra(MainActivity.EXTRA_ROLE);
        username = getIntent().getStringExtra(MainActivity.EXTRA_USERNAME);
        scoreId = getIntent().getLongExtra(EXTRA_SCORE_ID, -1L);

        if (scoreId > 0) {
            existingRecord = scoreDao.getScoreById(scoreId);
            if (existingRecord == null) {
                Toast.makeText(this, "记录不存在", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (DBHelper.ROLE_TEACHER.equals(role)
                    && !username.equals(existingRecord.getTeacherUsername())) {
                Toast.makeText(this, "你没有权限修改这条记录", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        spCourse = findViewById(R.id.spCourse);
        spStudent = findViewById(R.id.spStudent);
        etScore = findViewById(R.id.etScore);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(scoreId > 0 ? "修改成绩" : "新增成绩");
        }

        btnSave.setOnClickListener(v -> saveScore());
        btnCancel.setOnClickListener(v -> finish());

        loadCourses();
    }

    private void loadCourses() {
        courses.clear();
        if (DBHelper.ROLE_TEACHER.equals(role)) {
            courses.addAll(scoreDao.getCoursesForTeacher(username));
        } else {
            courses.addAll(scoreDao.getAllCourses());
        }

        List<String> courseNames = new ArrayList<>();
        for (CourseItem course : courses) {
            courseNames.add(buildCourseLabel(course));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, courseNames);
        spCourse.setAdapter(adapter);

        if (scoreId > 0) {
            loadExistingScore();
            return;
        }

        spCourse.setOnItemSelectedListener(
                new SimpleItemSelectedListener(position -> loadStudents(position, null)));
        if (!courses.isEmpty()) {
            loadStudents(0, null);
        } else {
            loadStudents(-1, null);
        }
    }

    private void loadStudents(int coursePosition, String selectedStudentUsername) {
        students.clear();
        if (coursePosition >= 0 && coursePosition < courses.size()) {
            CourseItem course = courses.get(coursePosition);
            if (DBHelper.ROLE_TEACHER.equals(role)) {
                students.addAll(scoreDao.getEnrolledStudents(course.getId()));
            } else {
                students.addAll(scoreDao.getStudents());
            }
        }

        List<String> studentNames = new ArrayList<>();
        for (UserAccount student : students) {
            studentNames.add(buildStudentLabel(student));
        }

        int selectedIndex = -1;
        if (!TextUtils.isEmpty(selectedStudentUsername)) {
            selectedIndex = findStudentIndex(selectedStudentUsername);
            if (selectedIndex < 0 && existingRecord != null
                    && TextUtils.equals(existingRecord.getStudentUsername(), selectedStudentUsername)) {
                UserAccount tempStudent = new UserAccount();
                tempStudent.setUsername(existingRecord.getStudentUsername());
                tempStudent.setRealName(existingRecord.getStudentName());
                students.add(tempStudent);
                studentNames.add(buildStudentLabel(tempStudent));
                selectedIndex = students.size() - 1;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, studentNames);
        spStudent.setAdapter(adapter);
        if (selectedIndex >= 0) {
            spStudent.setSelection(selectedIndex);
        }
    }

    private void loadExistingScore() {
        if (existingRecord == null) {
            Toast.makeText(this, "记录不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int courseIndex = findCourseIndex(existingRecord.getCourseId());
        if (courseIndex < 0) {
            Toast.makeText(this, "记录对应的课程不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        spCourse.setSelection(courseIndex);
        loadStudents(courseIndex, existingRecord.getStudentUsername());

        if (existingRecord.getScore() != null) {
            etScore.setText(String.valueOf(existingRecord.getScore()));
        }

        spCourse.setEnabled(false);
        spStudent.setEnabled(false);
    }

    private int findCourseIndex(long courseId) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId() == courseId) {
                return i;
            }
        }
        return -1;
    }

    private int findStudentIndex(String studentUsername) {
        for (int i = 0; i < students.size(); i++) {
            if (TextUtils.equals(students.get(i).getUsername(), studentUsername)) {
                return i;
            }
        }
        return -1;
    }

    private void saveScore() {
        if (courses.isEmpty()) {
            showDialog("当前没有可用课程，请先添加课程");
            return;
        }

        String scoreText = getText(etScore);
        if (TextUtils.isEmpty(scoreText)) {
            showDialog("请输入成绩");
            return;
        }

        int scoreValue;
        try {
            scoreValue = Integer.parseInt(scoreText);
        } catch (NumberFormatException e) {
            showDialog("成绩必须是数字");
            return;
        }
        if (scoreValue < 0 || scoreValue > 100) {
            showDialog("成绩范围应为 0 到 100");
            return;
        }

        ScoreRecord record = new ScoreRecord();
        if (scoreId > 0) {
            if (existingRecord == null) {
                showDialog("记录不存在，无法修改");
                return;
            }
            record.setId(existingRecord.getId());
            record.setCourseId(existingRecord.getCourseId());
            record.setCourseName(existingRecord.getCourseName());
            record.setTeacherUsername(existingRecord.getTeacherUsername());
            record.setTeacherName(existingRecord.getTeacherName());
            record.setStudentUsername(existingRecord.getStudentUsername());
            record.setStudentName(existingRecord.getStudentName());
        } else {
            if (students.isEmpty()) {
                showDialog("当前课程还没有选课学生");
                return;
            }

            int courseIndex = spCourse.getSelectedItemPosition();
            int studentIndex = spStudent.getSelectedItemPosition();
            if (courseIndex < 0 || courseIndex >= courses.size()
                    || studentIndex < 0 || studentIndex >= students.size()) {
                showDialog("请选择课程和学生");
                return;
            }

            CourseItem course = courses.get(courseIndex);
            UserAccount student = students.get(studentIndex);
            if (DBHelper.ROLE_TEACHER.equals(role)
                    && !username.equals(course.getTeacherUsername())) {
                showDialog("老师只能给自己课程的学生评分");
                return;
            }

            record.setId(0);
            record.setCourseId(course.getId());
            record.setCourseName(course.getCourseName());
            record.setTeacherUsername(course.getTeacherUsername());
            record.setTeacherName(course.getTeacherName());
            record.setStudentUsername(student.getUsername());
            record.setStudentName(student.getRealName());
        }
        record.setScore(scoreValue);

        long result = scoreDao.saveScore(record);
        if (result == -2) {
            showDialog("该学生这门课已经有成绩了，请直接修改原成绩");
            return;
        }
        Toast.makeText(this, result > 0 ? "保存成功" : "保存失败", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String buildCourseLabel(CourseItem course) {
        return course.getCourseName() + "（" + course.getTeacherName() + "）";
    }

    private String buildStudentLabel(UserAccount student) {
        return student.getRealName() + "（" + student.getUsername() + "）";
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
}
