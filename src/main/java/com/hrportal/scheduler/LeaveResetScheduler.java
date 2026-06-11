package com.hrportal.scheduler;

import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveSummary;
import com.hrportal.entity.TimeOff;
import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.repository.TimeOffRepository;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.status.LeaveStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LeaveResetScheduler {

    private final EmployeeRepository employeeRepository;
    private final LeaveSummaryRepository leaveSummaryRepository;
    private final TimeOffRepository timeOffRepository;

    @Scheduled(cron = "0 0 0 1 4 *") 
    @Transactional
    public void resetLeavesOnNewYear() {
        timeOffRepository.deleteAll();
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        for (Employee emp : activeEmployees) {
            LeaveSummary summary = leaveSummaryRepository.findByEmployeeId(emp.getId())
                    .orElse(null);
            if (summary != null) {
                summary.setSickLeaveBalance(emp.getDepartment().getSickLeaves());
                summary.setCasualLeaveBalance(emp.getDepartment().getCasualLeaves());
                summary.setPaidLeaveBalance(emp.getDepartment().getPaidLeaves());
                leaveSummaryRepository.save(summary);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *")  
    @Transactional
    public void updateEmployeeLeaveStatus() {

        List<TimeOff> startingToday = timeOffRepository
        .findByStatusAndStartDate(LeaveStatus.APPROVED, LocalDate.now());
        for (TimeOff timeOff : startingToday) {
            timeOff.getEmployee().setStatus(EmployeeStatus.ON_LEAVE);
            employeeRepository.save(timeOff.getEmployee());
        }

        List<TimeOff> endedYesterday = timeOffRepository
        .findByStatusAndEndDate(LeaveStatus.APPROVED, LocalDate.now().minusDays(1));
        for (TimeOff timeOff : endedYesterday) {
            timeOff.getEmployee().setStatus(EmployeeStatus.ACTIVE);
            employeeRepository.save(timeOff.getEmployee());
        }
    }

    @Scheduled(cron = "0 0 0 * * *") 
    @Transactional
    public void removeTerminatedEmployeeLeaveSummaries() {
        List<Employee> terminatedEmployees = employeeRepository.findByStatus(EmployeeStatus.TERMINATED);
        for (Employee emp : terminatedEmployees) {
            leaveSummaryRepository.deleteById(emp.getId());
        }
    }
}
