package com.gdut.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleFKViolation(DataIntegrityViolationException ex,
                                    HttpServletRequest request,
                                    RedirectAttributes ra) {
        ra.addFlashAttribute("err", "操作失败：存在唯一键/外键约束冲突，无法完成。");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(UnexpectedRollbackException.class)
    public String handleUnexpectedRollback(UnexpectedRollbackException ex,
                                           HttpServletRequest request,
                                           RedirectAttributes ra) {
        ra.addFlashAttribute("err", "操作失败：批量处理过程中存在错误，部分记录已回滚。");
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes ra) {
        ra.addFlashAttribute("err", ex.getMessage());
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isBlank()) ? "redirect:" + referer : "redirect:/";
    }
}