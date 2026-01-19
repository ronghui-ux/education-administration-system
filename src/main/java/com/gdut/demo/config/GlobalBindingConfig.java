package com.gdut.demo.config;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 全局绑定配置：允许空字符串绑定到 BigDecimal（空 => null）。
 */
@ControllerAdvice
public class GlobalBindingConfig {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, nf, true));
    }
}