package com.hrportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.repository.TimeOffRepository;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.status.LeaveStatus;
import com.hrportal.type.LeaveType;
import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveSummary;
import com.hrportal.entity.TimeOff;
import com.hrportal.exception.BadRequestException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.dto.TimeOffDto;

@Service
@RequiredArgsConstructor
public class TimeOffService {
    private final TimeOffRepository repo;
    private final EmployeeService employeeService;
    private final LeaveSummaryRepository leaveSummaryRepository;

    public TimeOff apply(Long employeeId, TimeOffDto dto) {

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Employee employee = employeeService.getById(employeeId);

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new BadRequestException("Time off can only be applied for active employees");
        }

        List<TimeOff> pending = repo.findByEmployeeIdAndStatus(employeeId, LeaveStatus.PENDING);
        if (!pending.isEmpty()) {
            throw new BadRequestException("Employee already has a pending time off request");
        }

        LocalDate minAllowedDate = LocalDate.now().minusMonths(1);
        if (dto.startDate().isBefore(minAllowedDate)) {
            throw new BadRequestException("Leaves cannot be applied for dates before " + minAllowedDate);
        }

         LocalDate maxAllowedDate = LocalDate.of(LocalDate.now().getYear() + 1, 4, 1);
        if (dto.endDate().isAfter(maxAllowedDate)) {
            throw new BadRequestException("Leaves cannot be applied beyond date " + maxAllowedDate);
        }

        LeaveSummary summary = leaveSummaryRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave summary not found for employee: " + employeeId));

            long daysRequested = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate()) + 1;

        if(dto.leaveType() == LeaveType.SICK){
            int primaryBalance = summary.getSickLeaveBalance();
            if (primaryBalance < daysRequested) {
            throw new IllegalStateException(
            "Insufficient sick leave balance: " + primaryBalance + " (Paid leaves available: " + summary.getPaidLeaveBalance() + ")");
            }
        }

        else if(dto.leaveType() == LeaveType.CASUAL){
            int primaryBalance = summary.getCasualLeaveBalance();
            if (primaryBalance < daysRequested) {
            throw new IllegalStateException(
            "Insufficient casual leave balance: " + primaryBalance +  " (Paid leaves available: " + summary.getPaidLeaveBalance() + ")");
            }
        }

        else{
            int primaryBalance = summary.getPaidLeaveBalance();
            if (primaryBalance < daysRequested) {
            throw new IllegalStateException(
            "Insufficient paid leaves available: " + summary.getPaidLeaveBalance());
            }
        }
        
        return repo.save(TimeOff.builder()
                .employee(employee)
                .leaveType(dto.leaveType())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .reason(dto.reason())
                .status(LeaveStatus.PENDING)
                .build());
    }

    public TimeOff approve(Long id) {
        TimeOff timeOff = updateStatus(id, LeaveStatus.APPROVED);
        long days = ChronoUnit.DAYS.between(timeOff.getStartDate(), timeOff.getEndDate()) + 1;

        LeaveSummary summary = leaveSummaryRepository.findByEmployeeId(timeOff.getEmployee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave summary not found"));

        if (timeOff.getLeaveType() == LeaveType.SICK) {
          summary.setSickLeaveBalance((int)(summary.getSickLeaveBalance() - days));
        } else if(timeOff.getLeaveType() == LeaveType.CASUAL){
          summary.setCasualLeaveBalance((int)(summary.getCasualLeaveBalance() - days));
        }
        else{
          summary.setPaidLeaveBalance((int)(summary.getPaidLeaveBalance() - days));
        }
        leaveSummaryRepository.save(summary);
        return timeOff;
  }

    public TimeOff reject(Long id) { return updateStatus(id, LeaveStatus.REJECTED); }

    public List<TimeOff> getByEmployee(Long empId) { 
        employeeService.getById(empId);
        return repo.findByEmployeeId(empId);
    }
    public List<TimeOff> getPending() { 
        List<TimeOff> pending = repo.findByStatus(LeaveStatus.PENDING);
        if (pending.isEmpty()) {
            throw new ResourceNotFoundException("No pending leave requests found");
        }
        return pending;
    } 

    private TimeOff updateStatus(Long id, LeaveStatus status) {
        TimeOff lr = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
        if (lr.getStatus() == LeaveStatus.APPROVED) {
          throw new IllegalStateException("Leave is already approved");
        }
        if (lr.getStatus() == LeaveStatus.REJECTED) {
          throw new IllegalStateException("Leave is already rejected");
        }
        lr.setStatus(status);   
        return repo.save(lr);
    }

     public long getTotalLeavesTaken(Long employeeId) {
      employeeService.getById(employeeId); 
      return repo.findByEmployeeIdAndStatus(employeeId, LeaveStatus.APPROVED)
            .stream()
            .mapToLong(t -> ChronoUnit.DAYS.between(t.getStartDate(), t.getEndDate()) + 1)
            .sum();
    }
}
