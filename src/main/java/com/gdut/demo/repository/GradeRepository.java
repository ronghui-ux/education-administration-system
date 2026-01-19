package com.gdut.demo.repository;

import com.gdut.demo.model.Grade;
import com.gdut.demo.model.GradeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, GradeId> { }