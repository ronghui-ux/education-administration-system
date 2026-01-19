package com.gdut.demo;

import com.gdut.demo.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    private final StudentRepository studentRepository;
    public StartupRunner(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        long count = studentRepository.count();
        System.out.println("学生表行数: " + count);
    }
}
