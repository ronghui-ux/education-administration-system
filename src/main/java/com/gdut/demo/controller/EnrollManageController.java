package com.gdut.demo.controller;

import com.gdut.demo.dto.EnrollMatrixResultItem;
import com.gdut.demo.model.Enrollment;
import com.gdut.demo.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * v9
 * 变更点：
 * - 修复“批量为课程选学生”在出现部分失败后引发 Hibernate 断言（null identifier）的问题：
 *   控制器不再调用批量事务方法，而是逐条调用 enrollmentService.enrollStudent(...)，
 *   每条独立事务、独立捕获异常，避免整批被标记为 rollback-only。
 * - 统一支持按学生与按课程的查询；参数大小写与空格均会被修剪。
 * - /by-course 同时支持 GET/POST；GET 仅查询，POST 执行批量选课。
 * - 批量结果通过 FlashAttribute 回传并在页面展示。
 */
@Controller
@RequestMapping("/enroll/manage")
public class EnrollManageController {

    private final EnrollmentService enrollmentService;

    public EnrollManageController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /* 页面入口：同时支持“按学生”和“按课程”的查询 */
    @GetMapping
    public String manage(@RequestParam(required = false) String studentId,
                         @RequestParam(required = false) String courseId,
                         @RequestParam(required = false) String term,
                         Model model) {
        String sid = nz(studentId);
        String cid = nz(courseId);
        String tm  = nz(term);

        // 左侧：按学生
        model.addAttribute("studentId", sid);
        model.addAttribute("term", tm);
        List<Enrollment> byStudent = new ArrayList<>();
        if (!sid.isEmpty() && !tm.isEmpty()) {
            byStudent = enrollmentService.listByStudentAndTerm(sid, tm);
        }
        model.addAttribute("byStudent", byStudent);

        // 右侧：按课程
        model.addAttribute("courseId", cid);
        List<Enrollment> byCourse = new ArrayList<>();
        if (!cid.isEmpty() && !tm.isEmpty()) {
            byCourse = enrollmentService.listByCourseAndTerm(cid, tm);
        }
        model.addAttribute("byCourse", byCourse);

        return "enroll/manage";
    }

    /* 批量为“学生”选课（左侧）——逐条独立调用，避免整批事务回滚 */
    @PostMapping("/by-student")
    public String batchByStudent(@RequestParam String studentId,
                                 @RequestParam String term,
                                 @RequestParam(name = "courseIds", required = false) String courseIdsText,
                                 RedirectAttributes ra) {
        String sid = nz(studentId);
        String tm  = nz(term);
        if (sid.isEmpty() || tm.isEmpty()) {
            ra.addFlashAttribute("err", "学号与学期不能为空");
            return "redirect:/enroll/manage";
        }

        List<String> courseIds = split(courseIdsText);
        if (courseIds.isEmpty()) {
            // 仅查询
            return "redirect:/enroll/manage?studentId=" + sid + "&term=" + tm;
        }

        List<EnrollMatrixResultItem> results = new ArrayList<>();
        for (String cid : courseIds) {
            EnrollMatrixResultItem item = new EnrollMatrixResultItem(sid, cid);
            try {
                enrollmentService.enrollStudent(sid, cid, tm); // 每条一个事务
                item.setSuccess(true);
                item.setMessage("OK");
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(ex.getMessage());
            }
            results.add(item);
        }

        long ok = results.stream().filter(EnrollMatrixResultItem::isSuccess).count();
        long fail = results.size() - ok;
        if (fail == 0) {
            ra.addFlashAttribute("ok", String.format("批量选课成功：%d 条", ok));
        } else {
            ra.addFlashAttribute("err", String.format("批量选课完成：成功 %d 条，失败 %d 条。", ok, fail));
            ra.addFlashAttribute("batchResults", results);
        }
        return "redirect:/enroll/manage?studentId=" + sid + "&term=" + tm + "&courseId=";
    }

    /* 批量为“课程”选学生（右侧）——支持 GET/POST；逐条独立调用避免整批失败 */
    @RequestMapping(value = "/by-course", method = {RequestMethod.GET, RequestMethod.POST})
    public String batchByCourse(@RequestParam String courseId,
                                @RequestParam String term,
                                @RequestParam(name = "studentIds", required = false) String studentIdsText,
                                RedirectAttributes ra) {
        String cid = nz(courseId);
        String tm  = nz(term);
        if (cid.isEmpty() || tm.isEmpty()) {
            ra.addFlashAttribute("err", "课程号与学期不能为空");
            return "redirect:/enroll/manage";
        }

        List<String> studentIds = split(studentIdsText);
        if (studentIds.isEmpty()) {
            // 仅查询
            return "redirect:/enroll/manage?courseId=" + cid + "&term=" + tm;
        }

        List<EnrollMatrixResultItem> results = new ArrayList<>();
        for (String sid : studentIds) {
            EnrollMatrixResultItem item = new EnrollMatrixResultItem(sid, cid);
            try {
                enrollmentService.enrollStudent(sid, cid, tm); // 每条一个事务
                item.setSuccess(true);
                item.setMessage("OK");
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(ex.getMessage());
            }
            results.add(item);
        }

        long ok = results.stream().filter(EnrollMatrixResultItem::isSuccess).count();
        long fail = results.size() - ok;
        if (fail == 0) {
            ra.addFlashAttribute("ok", String.format("批量选课成功：%d 条", ok));
        } else {
            ra.addFlashAttribute("err", String.format("批量选课完成：成功 %d 条，失败 %d 条。", ok, fail));
            ra.addFlashAttribute("batchResults", results);
        }
        return "redirect:/enroll/manage?courseId=" + cid + "&term=" + tm + "&studentId=";
    }

    /* 删除选课（两侧列表通用） */
    @PostMapping("/delete")
    public String delete(@RequestParam String studentId,
                         @RequestParam String courseId,
                         @RequestParam String term,
                         RedirectAttributes ra) {
        try {
            enrollmentService.deleteEnrollment(studentId, courseId, term);
            ra.addFlashAttribute("ok", "删除成功：" + studentId + " / " + courseId + " / " + term);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "删除失败：" + ex.getMessage());
        }
        return "redirect:/enroll/manage?studentId=" + nz(studentId) + "&courseId=" + nz(courseId) + "&term=" + nz(term);
    }

    /* 更新选课状态（两侧列表通用） */
    @PostMapping("/status")
    public String updateStatus(@RequestParam String studentId,
                               @RequestParam String courseId,
                               @RequestParam String term,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        try {
            enrollmentService.updateEnrollmentStatus(studentId, courseId, term, status);
            ra.addFlashAttribute("ok", "状态已更新：" + studentId + " / " + courseId + " -> " + status);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "更新失败：" + ex.getMessage());
        }
        return "redirect:/enroll/manage?studentId=" + nz(studentId) + "&courseId=" + nz(courseId) + "&term=" + nz(term);
    }

    /* helpers */
    private String nz(String s) { return s == null ? "" : s.trim(); }

    private List<String> split(String text) {
        if (text == null) return List.of();
        String t = text.trim();
        if (t.isEmpty()) return List.of();
        String[] parts = t.split("[\\r\\n,\\s]+");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String v = p.trim();
            if (!v.isEmpty()) list.add(v);
        }
        return list;
    }
}