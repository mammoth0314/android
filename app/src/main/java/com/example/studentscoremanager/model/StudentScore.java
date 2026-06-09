package com.example.studentscoremanager.model;

public class StudentScore {
    private long id;
    private String studentNo;
    private String studentName;
    private String courseName;
    private int score;

    public StudentScore() {
    }

    public StudentScore(long id, String studentNo, String studentName, String courseName, int score) {
        this.id = id;
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.courseName = courseName;
        this.score = score;
    }

    public StudentScore(String studentNo, String studentName, String courseName, int score) {
        this(0, studentNo, studentName, courseName, score);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

