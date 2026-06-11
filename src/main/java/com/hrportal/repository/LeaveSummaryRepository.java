package com.hrportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.hrportal.entity.LeaveSummary;
public interface LeaveSummaryRepository extends JpaRepository<LeaveSummary, Long> {
      Optional<LeaveSummary> findByEmployeeId(Long employeeId);
}
