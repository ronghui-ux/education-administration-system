package com.gdut.demo.service;

import com.gdut.demo.model.Course;
import com.gdut.demo.repository.CourseRepository;
import com.gdut.demo.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;

    public CourseService(CourseRepository courseRepository,
                         StaffRepository staffRepository) {
        this.courseRepository = courseRepository;
        this.staffRepository = staffRepository;
    }

    public List<Course> search(String courseId, String name, String teacherName) {
        String cid = normalize(courseId);
        String nm  = normalize(name);
        String tn  = normalize(teacherName);
        return courseRepository.searchByFilters(emptyToNull(cid), emptyToNull(nm), emptyToNull(tn));
    }

    // 供模板显示教师姓名（如果 Course 未映射 Staff 关系）
    public Map<String, String> teacherNameMapFor(List<Course> courses) {
        Map<String, String> map = new HashMap<>();
        courses.stream().map(Course::getTeacherId).filter(id -> id != null && !id.isBlank()).distinct()
                .forEach(id -> staffRepository.findById(id).ifPresent(st -> map.put(id, st.getName())));
        return map;
    }

    private String normalize(String s) { return s == null ? "" : s.trim(); }
    private String emptyToNull(String s) { return (s == null || s.isEmpty()) ? null : s; }
}