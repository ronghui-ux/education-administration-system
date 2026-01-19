package com.gdut.demo.dto;

public class EnrollMatrixResultItem {
    private String studentId;
    private String courseId;
    private boolean success;
    private String message;

    public EnrollMatrixResultItem() {}

    public EnrollMatrixResultItem(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}