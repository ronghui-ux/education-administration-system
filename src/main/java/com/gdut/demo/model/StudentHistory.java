package com.gdut.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 学生历史库：转学/毕业记录
 * 表：edu.student_history
 */
@Entity
@Table(name = "student_history", schema = "edu")
public class StudentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "student_id", length = 20, nullable = false)
    private String studentId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "reason", length = 50) // 例如：transfer_out, transfer_in, graduate
    private String reason;

    @Column(name = "note")
    private String note;

    @Column(name = "moved_at")
    private LocalDateTime movedAt;

    // getters & setters
    public Long getHistoryId() { return historyId; }
    public void setHistoryId(Long historyId) { this.historyId = historyId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getMovedAt() { return movedAt; }
    public void setMovedAt(LocalDateTime movedAt) { this.movedAt = movedAt; }
}
