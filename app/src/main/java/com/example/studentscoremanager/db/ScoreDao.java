package com.example.studentscoremanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.studentscoremanager.model.CourseItem;
import com.example.studentscoremanager.model.EnrollmentItem;
import com.example.studentscoremanager.model.ScoreRecord;
import com.example.studentscoremanager.model.UserAccount;

import java.util.ArrayList;
import java.util.List;

public class ScoreDao {
    private final DBHelper helper;

    public ScoreDao(Context context) {
        helper = new DBHelper(context.getApplicationContext());
    }

    public UserAccount login(String username, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USERS, null,
                DBHelper.COL_USERNAME + "=? AND " + DBHelper.COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        UserAccount account = null;
        if (cursor.moveToFirst()) {
            account = readUser(cursor);
        }
        cursor.close();
        return account;
    }

    public UserAccount getUserByUsername(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USERS, null,
                DBHelper.COL_USERNAME + "=?",
                new String[]{username}, null, null, null);
        UserAccount account = null;
        if (cursor.moveToFirst()) {
            account = readUser(cursor);
        }
        cursor.close();
        return account;
    }

    public long registerUser(UserAccount user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_USERNAME, user.getUsername());
        values.put(DBHelper.COL_PASSWORD, user.getPassword());
        values.put(DBHelper.COL_ROLE, user.getRole());
        values.put(DBHelper.COL_REAL_NAME, user.getRealName());
        return db.insert(DBHelper.TABLE_USERS, null, values);
    }

    public List<UserAccount> getStudents() {
        return getUsersByRole(DBHelper.ROLE_STUDENT);
    }

    public List<UserAccount> getTeachers() {
        return getUsersByRole(DBHelper.ROLE_TEACHER);
    }

    public List<UserAccount> getUsersByRole(String role) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_USERS, null,
                DBHelper.COL_ROLE + "=?",
                new String[]{role}, null, null, DBHelper.COL_REAL_NAME + " COLLATE LOCALIZED ASC");
        List<UserAccount> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(readUser(cursor));
        }
        cursor.close();
        return list;
    }

    public long addCourse(String courseName, String teacherUsername, String teacherName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_COURSE_NAME, courseName);
        values.put(DBHelper.COL_TEACHER_USERNAME, teacherUsername);
        values.put(DBHelper.COL_TEACHER_NAME, teacherName);
        return db.insert(DBHelper.TABLE_COURSES, null, values);
    }

    public List<CourseItem> getAllCourses() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_COURSES, null, null, null, null, null,
                DBHelper.COL_ID + " DESC");
        List<CourseItem> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(readCourse(cursor));
        }
        cursor.close();
        return list;
    }

    public List<CourseItem> getCoursesForTeacher(String teacherUsername) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_COURSES, null,
                DBHelper.COL_TEACHER_USERNAME + "=?",
                new String[]{teacherUsername}, null, null,
                DBHelper.COL_ID + " DESC");
        List<CourseItem> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(readCourse(cursor));
        }
        cursor.close();
        return list;
    }

    public CourseItem getCourseById(long courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_COURSES, null,
                DBHelper.COL_ID + "=?",
                new String[]{String.valueOf(courseId)}, null, null, null);
        CourseItem course = null;
        if (cursor.moveToFirst()) {
            course = readCourse(cursor);
        }
        cursor.close();
        return course;
    }

    public long enrollStudent(String studentUsername, String studentName, CourseItem course) {
        if (course == null) {
            return -1;
        }
        if (isEnrolled(studentUsername, course.getId())) {
            return -2;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_STUDENT_USERNAME, studentUsername);
        values.put(DBHelper.COL_STUDENT_NAME, studentName);
        values.put(DBHelper.COL_COURSE_ID, course.getId());
        values.put(DBHelper.COL_COURSE_NAME, course.getCourseName());
        values.put(DBHelper.COL_TEACHER_USERNAME, course.getTeacherUsername());
        values.put(DBHelper.COL_TEACHER_NAME, course.getTeacherName());
        return db.insert(DBHelper.TABLE_ENROLLMENTS, null, values);
    }

    public boolean isEnrolled(String studentUsername, long courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_ENROLLMENTS, new String[]{DBHelper.COL_ID},
                DBHelper.COL_STUDENT_USERNAME + "=? AND " + DBHelper.COL_COURSE_ID + "=?",
                new String[]{studentUsername, String.valueOf(courseId)},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public List<CourseItem> getEnrolledCoursesForStudent(String studentUsername) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_ENROLLMENTS, null,
                DBHelper.COL_STUDENT_USERNAME + "=?",
                new String[]{studentUsername}, null, null,
                DBHelper.COL_ID + " DESC");
        List<CourseItem> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(new CourseItem(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_NAME))));
        }
        cursor.close();
        return list;
    }

    public List<CourseItem> getAvailableCoursesForStudent(String studentUsername) {
        List<CourseItem> all = getAllCourses();
        List<CourseItem> available = new ArrayList<>();
        for (CourseItem course : all) {
            if (!isEnrolled(studentUsername, course.getId())) {
                available.add(course);
            }
        }
        return available;
    }

    public List<EnrollmentItem> getStudentsForCourse(long courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_ENROLLMENTS, null,
                DBHelper.COL_COURSE_ID + "=?",
                new String[]{String.valueOf(courseId)}, null, null,
                DBHelper.COL_STUDENT_NAME + " COLLATE LOCALIZED ASC");
        List<EnrollmentItem> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(new EnrollmentItem(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_NAME))));
        }
        cursor.close();
        return list;
    }

    public List<UserAccount> getEnrolledStudents(long courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_ENROLLMENTS, null,
                DBHelper.COL_COURSE_ID + "=?",
                new String[]{String.valueOf(courseId)}, null, null,
                DBHelper.COL_STUDENT_NAME + " COLLATE LOCALIZED ASC");
        List<UserAccount> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            UserAccount user = new UserAccount();
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_USERNAME)));
            user.setRealName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_NAME)));
            list.add(user);
        }
        cursor.close();
        return list;
    }

    public long saveScore(ScoreRecord record) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_STUDENT_USERNAME, record.getStudentUsername());
        values.put(DBHelper.COL_STUDENT_NAME, record.getStudentName());
        values.put(DBHelper.COL_COURSE_ID, record.getCourseId());
        values.put(DBHelper.COL_COURSE_NAME, record.getCourseName());
        values.put(DBHelper.COL_TEACHER_USERNAME, record.getTeacherUsername());
        values.put(DBHelper.COL_TEACHER_NAME, record.getTeacherName());
        values.put(DBHelper.COL_SCORE, record.getScore());

        try {
            if (record.getId() > 0) {
                return db.update(DBHelper.TABLE_SCORES, values, DBHelper.COL_ID + "=?",
                        new String[]{String.valueOf(record.getId())});
            }

            ScoreRecord existing = getScoreByStudentAndCourse(
                    record.getStudentUsername(), record.getCourseId());
            if (existing != null) {
                return -2;
            }
            return db.insert(DBHelper.TABLE_SCORES, null, values);
        } catch (SQLiteConstraintException e) {
            return -2;
        }
    }

    public ScoreRecord getScoreByStudentAndCourse(String studentUsername, long courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_SCORES, null,
                DBHelper.COL_STUDENT_USERNAME + "=? AND " + DBHelper.COL_COURSE_ID + "=?",
                new String[]{studentUsername, String.valueOf(courseId)},
                null, null, null);
        ScoreRecord record = null;
        if (cursor.moveToFirst()) {
            record = readScore(cursor);
        }
        cursor.close();
        return record;
    }

    public ScoreRecord getScoreById(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_SCORES, null,
                DBHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        ScoreRecord record = null;
        if (cursor.moveToFirst()) {
            record = readScore(cursor);
        }
        cursor.close();
        return record;
    }

    public int deleteScore(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DBHelper.TABLE_SCORES, DBHelper.COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public List<ScoreRecord> getVisibleScores(String role, String username) {
        return getVisibleScores(role, username, DBHelper.COL_ID + " DESC");
    }

    public List<ScoreRecord> getVisibleScores(String role, String username, String orderBy) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = null;
        String[] args = null;
        if (DBHelper.ROLE_TEACHER.equals(role)) {
            selection = DBHelper.COL_TEACHER_USERNAME + "=?";
            args = new String[]{username};
        } else if (DBHelper.ROLE_STUDENT.equals(role)) {
            selection = DBHelper.COL_STUDENT_USERNAME + "=?";
            args = new String[]{username};
        }
        Cursor cursor = db.query(DBHelper.TABLE_SCORES, null, selection, args,
                null, null, orderBy);
        List<ScoreRecord> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(readScore(cursor));
        }
        cursor.close();
        return list;
    }

    public List<ScoreRecord> getScoresForTeacherCourse(String teacherUsername, long courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_SCORES, null,
                DBHelper.COL_TEACHER_USERNAME + "=? AND " + DBHelper.COL_COURSE_ID + "=?",
                new String[]{teacherUsername, String.valueOf(courseId)},
                null, null, DBHelper.COL_ID + " DESC");
        List<ScoreRecord> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(readScore(cursor));
        }
        cursor.close();
        return list;
    }

    public List<ScoreRecord> getAllScores() {
        return getVisibleScores(null, null);
    }

    public int deleteAllScores() {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(DBHelper.TABLE_SCORES, null, null);
    }

    public List<EnrollmentItem> getAllEnrollmentCourses(String studentUsername) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_ENROLLMENTS, null,
                DBHelper.COL_STUDENT_USERNAME + "=?",
                new String[]{studentUsername}, null, null,
                DBHelper.COL_ID + " DESC");
        List<EnrollmentItem> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(new EnrollmentItem(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_NAME))));
        }
        cursor.close();
        return list;
    }

    private UserAccount readUser(Cursor cursor) {
        UserAccount user = new UserAccount();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROLE)));
        user.setRealName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_REAL_NAME)));
        return user;
    }

    private CourseItem readCourse(Cursor cursor) {
        CourseItem course = new CourseItem();
        course.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ID)));
        course.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME)));
        course.setTeacherUsername(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME)));
        course.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_NAME)));
        return course;
    }

    private ScoreRecord readScore(Cursor cursor) {
        ScoreRecord record = new ScoreRecord();
        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ID)));
        record.setStudentUsername(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_USERNAME)));
        record.setStudentName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STUDENT_NAME)));
        record.setCourseId(cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_ID)));
        record.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_COURSE_NAME)));
        record.setTeacherUsername(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_USERNAME)));
        record.setTeacherName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEACHER_NAME)));
        if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SCORE)))) {
            record.setScore(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SCORE)));
        }
        return record;
    }
}
