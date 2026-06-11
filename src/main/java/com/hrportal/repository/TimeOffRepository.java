package com.hrportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

import com.hrportal.entity.TimeOff;
import com.hrportal.status.LeaveStatus;

public interface TimeOffRepository extends JpaRepository<TimeOff, Long> {
    List<TimeOff> findByEmployeeId(Long employeeId);
    List<TimeOff> findByStatus(LeaveStatus status);
    List<TimeOff> findByEmployeeIdAndStatus(Long employeeId,LeaveStatus status);
    List<TimeOff> findByStatusAndStartDate(LeaveStatus status, LocalDate startDate);
    List<TimeOff> findByStatusAndEndDate(LeaveStatus status, LocalDate endDate);
}
