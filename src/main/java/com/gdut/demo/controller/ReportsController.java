package com.gdut.demo.controller;

import com.gdut.demo.service.ReportsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping(value = "/registration.csv", produces = "text/csv")
    public ResponseEntity<byte[]> registrationCsv(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false) String deptId
    ) {
        byte[] data = reportsService.exportRegistrationCsv(term, courseId, courseName, teacher, deptId);
        return csvResponse(data, "registration", term, courseId, courseName, teacher, deptId);
    }

    @GetMapping(value = "/grades.csv", produces = "text/csv")
    public ResponseEntity<byte[]> gradesCsv(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String deptId
    ) {
        byte[] data = reportsService.exportGradesCsv(term, courseId, courseName, teacherName, deptId);
        return csvResponse(data, "grades", term, courseId, courseName, teacherName, deptId);
    }

    private ResponseEntity<byte[]> csvResponse(byte[] data, String prefix, String... parts) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        StringBuilder name = new StringBuilder(prefix).append("_").append(ts);
        // 简要拼上非空的筛选摘要
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                name.append("_").append(safe(p));
            }
        }
        name.append(".csv");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    private String safe(String s) {
        String t = s.trim().replaceAll("[\\r\\n,]+", "_");
        return t.length() > 24 ? t.substring(0, 24) : t;
    }
}