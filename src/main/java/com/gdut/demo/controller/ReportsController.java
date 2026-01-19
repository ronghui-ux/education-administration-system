package com.gdut.demo.controller;

import com.gdut.demo.service.ReportsService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    // 成绩登记表导出（CSV）
    // 过滤：term（可空）、courseId、courseName、teacher、deptId（教师所在系）
    @GetMapping(value = "/reports/registration.csv", produces = "text/csv")
    public ResponseEntity<ByteArrayResource> exportRegistration(@RequestParam(required = false) String term,
                                                                @RequestParam(required = false) String courseId,
                                                                @RequestParam(required = false) String courseName,
                                                                @RequestParam(required = false) String teacher,
                                                                @RequestParam(required = false) String deptId) {
        byte[] data = reportsService.exportRegistrationCsv(term, courseId, courseName, teacher, deptId);
        return attachment("registration.csv", data);
    }

    // 成绩报表导出（CSV）— 含分段统计
    @GetMapping(value = "/reports/grades.csv", produces = "text/csv")
    public ResponseEntity<ByteArrayResource> exportGrades(@RequestParam(required = false) String term,
                                                          @RequestParam(required = false) String courseId,
                                                          @RequestParam(required = false) String courseName,
                                                          @RequestParam(required = false) String teacherName,
                                                          @RequestParam(required = false) String deptId) {
        byte[] data = reportsService.exportGradesCsv(term, courseId, courseName, teacherName, deptId);
        return attachment("grades.csv", data);
    }

    private ResponseEntity<ByteArrayResource> attachment(String filename, byte[] data) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(new ByteArrayResource(data));
    }
}