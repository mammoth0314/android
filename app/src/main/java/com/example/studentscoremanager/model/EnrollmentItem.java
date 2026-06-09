package com.example.studentscoremanager.model;

public class EnrollmentItem {
    private long courseId;
    private String courseName;
    private String teacherUsername;
    private String teacherName;

    public EnrollmentItem() {
    }

    public EnrollmentItem(long courseId, String courseName, String teacherUsername, String teacherName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherUsername = teacherUsername;
        this.teacherName = teacherName;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}

