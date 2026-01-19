package com.gdut.demo.service;

import com.gdut.demo.model.Student;
import com.gdut.demo.model.StudentHistory;
import com.gdut.demo.repository.StudentHistoryRepository;
import com.gdut.demo.repository.StudentRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentHistoryRepository historyRepository;

    public StudentService(StudentRepository studentRepository,
                          StudentHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.historyRepository = historyRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public List<Student> search(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) {
            return studentRepository.findAll();
        }
        return studentRepository
                .findByStudentIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrDeptIdContainingIgnoreCase(kw, kw, kw);
    }

    // 新增：按学号/姓名/系号的任意组合进行模糊查询
    public List<Student> searchByFilters(String studentId, String name, String deptId) {
        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (studentId != null && !studentId.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("studentId")), "%" + studentId.trim().toLowerCase() + "%"));
            }
            if (name != null && !name.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (deptId != null && !deptId.isBlank()) {
                preds.add(cb.like(cb.lower(root.get("deptId")), "%" + deptId.trim().toLowerCase() + "%"));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
        return studentRepository.findAll(spec);
    }

    public Optional<Student> findById(String studentId) {
        if (studentId == null) return Optional.empty();
        return studentRepository.findById(studentId.trim());
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void deleteById(String studentId) {
        studentRepository.deleteById(studentId.trim());
    }

    /* ---------- 历史与转学/毕业处理 ---------- */

    @Transactional
    public void graduate(String studentId, String note, LocalDateTime movedAt) {
        moveToHistory(studentId, "graduate", note, movedAt);
    }

    @Transactional
    public void transferOut(String studentId, String note, LocalDateTime movedAt) {
        moveToHistory(studentId, "transfer_out", note, movedAt);
    }

    @Transactional
    public void transferIn(Student newStudent, String note, LocalDateTime movedAt) {
        studentRepository.save(newStudent);
        StudentHistory h = new StudentHistory();
        h.setStudentId(newStudent.getStudentId());
        h.setName(newStudent.getName());
        h.setReason("transfer_in");
        h.setNote(note);
        h.setMovedAt(movedAt == null ? LocalDateTime.now() : movedAt);
        historyRepository.save(h);
    }

    private void moveToHistory(String studentId, String reason, String note, LocalDateTime movedAt) {
        Student s = studentRepository.findById(studentId.trim())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在：" + studentId));
        StudentHistory h = new StudentHistory();
        h.setStudentId(s.getStudentId());
        h.setName(s.getName());
        h.setReason(reason);
        h.setNote(note);
        h.setMovedAt(movedAt == null ? LocalDateTime.now() : movedAt);
        historyRepository.save(h);
        studentRepository.deleteById(s.getStudentId());
    }
}