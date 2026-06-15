package com.example.studentscoremanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "student_score.db";
    public static final int DB_VERSION = 5;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_ENROLLMENTS = "enrollments";
    public static final String TABLE_SCORES = "scores";

    public static final String COL_ID = "_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_ROLE = "role";
    public static final String COL_REAL_NAME = "real_name";
    public static final String COL_COURSE_NAME = "course_name";
    public static final String COL_TEACHER_USERNAME = "teacher_username";
    public static final String COL_TEACHER_NAME = "teacher_name";
    public static final String COL_STUDENT_USERNAME = "student_username";
    public static final String COL_STUDENT_NAME = "student_name";
    public static final String COL_COURSE_ID = "course_id";
    public static final String COL_SCORE = "score";

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_ROLE + " TEXT NOT NULL, "
                + COL_REAL_NAME + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_COURSES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_COURSE_NAME + " TEXT NOT NULL, "
                + COL_TEACHER_USERNAME + " TEXT NOT NULL, "
                + COL_TEACHER_NAME + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_ENROLLMENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_STUDENT_USERNAME + " TEXT NOT NULL, "
                + COL_STUDENT_NAME + " TEXT NOT NULL, "
                + COL_COURSE_ID + " INTEGER NOT NULL, "
                + COL_COURSE_NAME + " TEXT NOT NULL, "
                + COL_TEACHER_USERNAME + " TEXT NOT NULL, "
                + COL_TEACHER_NAME + " TEXT NOT NULL, "
                + "UNIQUE(" + COL_STUDENT_USERNAME + "," + COL_COURSE_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_SCORES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_STUDENT_USERNAME + " TEXT NOT NULL, "
                + COL_STUDENT_NAME + " TEXT NOT NULL, "
                + COL_COURSE_ID + " INTEGER NOT NULL, "
                + COL_COURSE_NAME + " TEXT NOT NULL, "
                + COL_TEACHER_USERNAME + " TEXT NOT NULL, "
                + COL_TEACHER_NAME + " TEXT NOT NULL, "
                + COL_SCORE + " INTEGER NOT NULL)");

        ensureIndexes(db);
        seedAdmin(db);
    }

    private void seedAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, "admin");
        values.put(COL_PASSWORD, "123456");
        values.put(COL_ROLE, ROLE_ADMIN);
        values.put(COL_REAL_NAME, "系统管理员");
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void ensureIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_scores_student_course "
                + "ON " + TABLE_SCORES + "(" + COL_STUDENT_USERNAME + ", " + COL_COURSE_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE FROM " + TABLE_SCORES + " WHERE " + COL_ID
                + " NOT IN (SELECT MAX(" + COL_ID + ") FROM " + TABLE_SCORES
                + " GROUP BY " + COL_STUDENT_USERNAME + ", " + COL_COURSE_ID + ")");
        ensureIndexes(db);
        seedAdmin(db);
    }
}
