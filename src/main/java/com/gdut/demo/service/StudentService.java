package com.gdut.demo.service;

import com.gdut.demo.model.Student;
import com.gdut.demo.model.StudentHistory;
import com.gdut.demo.repository.StudentHistoryRepository;
import com.gdut.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public List<Student> searchByFilters(String studentId, String name, String deptId) {
        String sid = blankToNull(studentId);
        String nm  = blankToNull(name);
        String did = blankToNull(deptId);

        if (sid != null && nm == null && did == null) {
            return studentRepository.findByStudentIdIgnoreCase(sid);
        }
        return studentRepository.searchByFiltersNative(sid, nm, did);
    }

    public Optional<Student> findById(String studentId) {
        if (studentId == null) return Optional.empty();
        return studentRepository.findById(studentId.trim());
    }

    // 改为使用原生 LIMIT 1 的大小写不敏感查询，避免 openGauss 语法错误
    public Optional<Student> findByIdIgnoreCase(String studentId) {
        if (studentId == null) return Optional.empty();
        return studentRepository.findOneIgnoreCaseNative(studentId.trim());
    }

    public Student save(Student student) {
        if (student.getStudentId() != null) {
            student.setStudentId(student.getStudentId().trim());
        }
        if (student.getDeptId() != null) {
            student.setDeptId(student.getDeptId().trim());
        }
        return studentRepository.save(student);
    }

    public void deleteById(String studentId) {
        studentRepository.deleteById(studentId.trim());
    }

    @Transactional
    public void graduate(String studentId, String note, LocalDateTime movedAt) {
        moveToHistoryIgnoreCase(studentId, "graduate", note, movedAt);
    }

    @Transactional
    public void transferOut(String studentId, String note, LocalDateTime movedAt) {
        moveToHistoryIgnoreCase(studentId, "transfer_out", note, movedAt);
    }

    @Transactional
    public void transferIn(Student newStudent, String note, LocalDateTime movedAt) {
        if (newStudent.getStudentId() != null) newStudent.setStudentId(newStudent.getStudentId().trim());
        if (newStudent.getDeptId() != null) newStudent.setDeptId(newStudent.getDeptId().trim());
        studentRepository.save(newStudent);

        StudentHistory h = new StudentHistory();
        h.setStudentId(newStudent.getStudentId());
        h.setName(newStudent.getName());
        h.setReason("transfer_in");
        h.setNote(note);
        h.setMovedAt(movedAt == null ? LocalDateTime.now() : movedAt);
        historyRepository.save(h);
    }

    private void moveToHistoryIgnoreCase(String studentId, String reason, String note, LocalDateTime movedAt) {
        Student s = findByIdIgnoreCase(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在：" + studentId));
        StudentHistory h = new StudentHistory();
        h.setStudentId(s.getStudentId());
        h.setName(s.getName());
        h.setReason(reason);
        h.setNote(note);
        h.setMovedAt(movedAt == null ? LocalDateTime.now() : movedAt);
        historyRepository.save(h);
        studentRepository.delete(s);
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}