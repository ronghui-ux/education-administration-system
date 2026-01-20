package com.gdut.demo.controller;

import com.gdut.demo.model.Enrollment;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/enroll/manage")
public class EnrollManageController {

    private final EnrollmentService enrollmentService;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollManageController(EnrollmentService enrollmentService,
                                  EnrollmentRepository enrollmentRepository) {
        this.enrollmentService = enrollmentService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping
    public String page(@RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String term,
                       @RequestParam(required = false) String ok,
                       @RequestParam(required = false) String err,
                       Model model) {
        String sid = nz(studentId).trim();
        String cid = nz(courseId).trim();
        String tm  = nz(term).trim();

        List<Enrollment> byStudent = (sid.isEmpty() || tm.isEmpty())
                ? List.of()
                : enrollmentService.listByStudentAndTerm(sid, tm);
        List<Enrollment> byCourse = (cid.isEmpty() || tm.isEmpty())
                ? List.of()
                : enrollmentService.listByCourseAndTerm(cid, tm);

        model.addAttribute("studentId", sid);
        model.addAttribute("courseId", cid);
        model.addAttribute("term", tm);
        model.addAttribute("byStudent", byStudent);
        model.addAttribute("byCourse", byCourse);
        model.addAttribute("ok", ok);
        model.addAttribute("err", err);
        return "enroll/manage";
    }

    // 批量为学生选课
    @PostMapping("/by-student")
    public String batchForStudent(@RequestParam String studentId,
                                  @RequestParam String term,
                                  @RequestParam String courseIds) {
        List<String> ids = split(courseIds);
        var results = enrollmentService.enrollBatchByStudent(studentId, term, ids);
        long ok = results.stream().filter(r -> r.isSuccess()).count();
        long fail = results.size() - ok;
        return "redirect:/enroll/manage?studentId=" + enc(studentId) + "&term=" + enc(term)
                + (ok > 0 ? "&ok=" + ok : "")
                + (fail > 0 ? "&err=" + fail : "");
    }

    // 删除一条选课
    @PostMapping("/delete")
    public String delete(@RequestParam String studentId,
                         @RequestParam String courseId,
                         @RequestParam String term) {
        enrollmentRepository.deleteByStudentIdAndCourseIdAndTerm(studentId.trim(), courseId.trim(), term.trim());
        return "redirect:/enroll/manage?studentId=" + enc(studentId) + "&courseId=" + enc(courseId) + "&term=" + enc(term) + "&ok=1";
    }

    private List<String> split(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split("[,\\s]+"))
                .map(String::trim).filter(x -> !x.isEmpty()).toList();
    }
    private String enc(String s) { return java.net.URLEncoder.encode(nz(s), java.nio.charset.StandardCharsets.UTF_8); }
    private String nz(String s) { return s == null ? "" : s; }
}