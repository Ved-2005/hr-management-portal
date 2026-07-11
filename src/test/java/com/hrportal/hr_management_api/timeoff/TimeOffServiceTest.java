package com.hrportal.hr_management_api.timeoff;

import com.hrportal.dto.request.TimeOffDTORequest;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveSummary;
import com.hrportal.entity.TimeOff;
import com.hrportal.exception.BadRequestException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.repository.TimeOffRepository;
import com.hrportal.service.EmployeeService;
import com.hrportal.service.TimeOffService;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.status.LeaveStatus;
import com.hrportal.type.LeaveType;
import com.hrportal.auth.SecurityContextHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeOffServiceTest {

    @Mock private TimeOffRepository repo;
    @Mock private EmployeeService employeeService;
    @Mock private LeaveSummaryRepository leaveSummaryRepository;
    @Mock private SecurityContextHelper securityContextHelper;
    @InjectMocks private TimeOffService service;

    private Employee activeEmployee;
    private LeaveSummary summary;

    @BeforeEach
    void setUp() {
        activeEmployee = new Employee();
        activeEmployee.setId(1L);
        activeEmployee.setUsername("abcdefg");
        activeEmployee.setStatus(EmployeeStatus.ACTIVE);
        
        Department dept = new Department();
        dept.setId(1L);
        activeEmployee.setDepartment(dept);

        summary = new LeaveSummary();
        summary.setSickLeaveBalance(5);
        summary.setCasualLeaveBalance(10);
        summary.setPaidLeaveBalance(10);
    }

    private TimeOffDTORequest validDto(LeaveType type) {
        return new TimeOffDTORequest(type, LocalDate.now(), LocalDate.now().plusDays(2), "Fever");
    }

    @Test
    void apply_shouldSaveTimeOff() {
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of());
        when(leaveSummaryRepository.findByEmployeeId(1L)).thenReturn(Optional.of(summary));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeOff result = service.apply("abcdefg", validDto(LeaveType.SICK));

        assertEquals(LeaveStatus.PENDING, result.getStatus());
        assertEquals(LeaveType.SICK, result.getLeaveType());
        verify(repo).save(any());
    }

    @Test
    void apply_shouldThrowWhenEndDateBeforeStartDate() {
        TimeOffDTORequest dto = new TimeOffDTORequest(LeaveType.SICK, LocalDate.now().plusDays(3), LocalDate.now(), "test");
        assertThrows(BadRequestException.class, () -> service.apply("abcdefg", dto));
    }

    @Test
    void apply_shouldThrowWhenEmployeeNotActive() {
        activeEmployee.setStatus(EmployeeStatus.ON_LEAVE);
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);

        assertThrows(BadRequestException.class, () -> service.apply("abcdefg", validDto(LeaveType.SICK)));
    }

    @Test
    void apply_shouldThrowWhenPendingLeaveExists() {
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of(new TimeOff()));

        assertThrows(BadRequestException.class, () -> service.apply("abcdefg", validDto(LeaveType.SICK)));
    }

    @Test
    void apply_shouldThrowWhenStartDateTooOld() {
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of());

        TimeOffDTORequest dto = new TimeOffDTORequest(LeaveType.SICK, LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(2).plusDays(2), "test");
        assertThrows(BadRequestException.class, () -> service.apply("abcdefg", dto));
    }

    @Test
    void apply_shouldThrowWhenEndDateBeyondMaxAllowed() {
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of());

        TimeOffDTORequest dto = new TimeOffDTORequest(LeaveType.SICK, LocalDate.now(), LocalDate.of(LocalDate.now().getYear() + 2, 4, 2), "test");
        assertThrows(BadRequestException.class, () -> service.apply("abcdefg", dto));
    }

    @Test
    void apply_shouldThrowWhenInsufficientSickLeaves() {
        summary.setSickLeaveBalance(1);
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of());
        when(leaveSummaryRepository.findByEmployeeId(1L)).thenReturn(Optional.of(summary));

        TimeOffDTORequest dto = new TimeOffDTORequest(LeaveType.SICK, LocalDate.now(), LocalDate.now().plusDays(4), "test");
        assertThrows(IllegalStateException.class, () -> service.apply("abcdefg", dto));
    }

    @Test
    void apply_shouldThrowWhenInsufficientCasualLeaves() {
        summary.setCasualLeaveBalance(1);
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of());
        when(leaveSummaryRepository.findByEmployeeId(1L)).thenReturn(Optional.of(summary));

        TimeOffDTORequest dto = new TimeOffDTORequest(LeaveType.CASUAL, LocalDate.now(), LocalDate.now().plusDays(4), "test");
        assertThrows(IllegalStateException.class, () -> service.apply("abcdefg", dto));
    }

    @Test
    void apply_shouldThrowWhenInsufficientPaidLeaves() {
        summary.setPaidLeaveBalance(1);
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.PENDING)).thenReturn(List.of());
        when(leaveSummaryRepository.findByEmployeeId(1L)).thenReturn(Optional.of(summary));

        TimeOffDTORequest dto = new TimeOffDTORequest(LeaveType.PAID, LocalDate.now(), LocalDate.now().plusDays(4), "test");
        assertThrows(IllegalStateException.class, () -> service.apply("abcdefg", dto));
    }


    @Test
    void approve_shouldApproveAndDeductSickLeaves() {
        TimeOff timeOff = TimeOff.builder()
                .employee(activeEmployee).leaveType(LeaveType.SICK)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(2))
                .status(LeaveStatus.PENDING).build();

        when(repo.findById(1L)).thenReturn(Optional.of(timeOff));
        when(securityContextHelper.getDepartmentId()).thenReturn(1L);
        when(leaveSummaryRepository.findByEmployeeId(1L)).thenReturn(Optional.of(summary));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.approve(1L);

        assertEquals(2, summary.getSickLeaveBalance()); 
        verify(leaveSummaryRepository).save(summary);
    }

    @Test
    void approve_shouldThrowWhenAlreadyApproved() {
        TimeOff timeOff = TimeOff.builder().employee(activeEmployee).status(LeaveStatus.APPROVED).build();
        when(repo.findById(1L)).thenReturn(Optional.of(timeOff));
        when(securityContextHelper.getDepartmentId()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.approve(1L));
    }

    @Test
    void approve_shouldThrowWhenAlreadyRejected() {
        TimeOff timeOff = TimeOff.builder().employee(activeEmployee).status(LeaveStatus.REJECTED).build();
        when(repo.findById(1L)).thenReturn(Optional.of(timeOff));
        when(securityContextHelper.getDepartmentId()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.approve(1L));
    }


    @Test
    void reject_shouldRejectPendingLeave() {
        TimeOff timeOff = TimeOff.builder().employee(activeEmployee).status(LeaveStatus.PENDING).build();
        when(repo.findById(1L)).thenReturn(Optional.of(timeOff));
        when(securityContextHelper.getDepartmentId()).thenReturn(1L);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeOff result = service.reject(1L);

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
    }

    @Test
    void reject_shouldThrowWhenAlreadyApproved() {
        TimeOff timeOff = TimeOff.builder().employee(activeEmployee).status(LeaveStatus.APPROVED).build();
        when(repo.findById(1L)).thenReturn(Optional.of(timeOff));
        when(securityContextHelper.getDepartmentId()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.reject(1L));
    }


    @Test
    void getByEmployee_shouldReturnTimeOffs() {
        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeId(1L)).thenReturn(List.of(new TimeOff(), new TimeOff()));

        assertEquals(2, service.getByEmployee("abcdefg").size());
    }

    @Test
    void getByEmployee_shouldThrowWhenEmployeeNotFound() {
        when(employeeService.getByUsername("abcdefg")).thenThrow(new ResourceNotFoundException("Employee not found: 99"));

        assertThrows(ResourceNotFoundException.class, () -> service.getByEmployee("abcdefg"));
    }


    @Test
    void getPending_shouldReturnPendingList() {
        when(repo.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(new TimeOff()));

        assertEquals(1, service.getPending().size());
    }

    @Test
    void getPending_shouldThrowWhenNoPendingLeaves() {
        when(repo.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.getPending());
    }

    @Test
    void getTotalLeavesTaken_shouldReturnTotalDays() {
        TimeOff t1 = TimeOff.builder()
                .startDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 1, 3)).build(); // 3 days
        TimeOff t2 = TimeOff.builder()
                .startDate(LocalDate.of(2026, 2, 1)).endDate(LocalDate.of(2026, 2, 2)).build(); // 2 days

        when(employeeService.getByUsername("abcdefg")).thenReturn(activeEmployee);
        when(repo.findByEmployeeIdAndStatus(1L, LeaveStatus.APPROVED)).thenReturn(List.of(t1, t2));

        assertEquals(5, service.getTotalLeavesTaken("abcdefg"));
    }

    @Test
    void getTotalLeavesTaken_shouldThrowWhenEmployeeNotFound() {
        when(employeeService.getByUsername("abcdefg")).thenThrow(new ResourceNotFoundException("Employee not found: 99"));

        assertThrows(ResourceNotFoundException.class, () -> service.getTotalLeavesTaken("abcdefg"));
    }
}