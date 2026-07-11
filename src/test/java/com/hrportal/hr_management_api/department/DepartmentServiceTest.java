package com.hrportal.hr_management_api.department;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.repository.EmployeeRepository;
import com.hrportal.service.DepartmentService;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.dto.request.DepartmentDTORequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repo;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService service;

    @Test
    void create_shouldSaveDepartment() {
        var dto = new DepartmentDTORequest("Engineering", "Tech team",5,10,10);
        when(repo.findByName("Engineering")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Department result = service.create(dto);

        assertEquals("Engineering", result.getName());
        verify(repo).save(any());
    }

    @Test
    void create_shouldThrowWhenNameAlreadyExists() {
        var dto = new DepartmentDTORequest("Engineering", "Tech team",5,10,10);
        when(repo.findByName("Engineering")).thenReturn(Optional.of(new Department()));

        assertThrows(DuplicateResourceException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void getById_shouldReturnDepartment() {
        Department dept = new Department();
        dept.setName("HR");
        when(repo.findById(1L)).thenReturn(Optional.of(dept));

        Department result = service.getById(1L);

        assertEquals("HR", result.getName());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getAll_shouldReturnList() {
        when(repo.findAll()).thenReturn(List.of(new Department(), new Department()));

        assertEquals(2, service.getAll().size());
    }

    @Test
    void delete_shouldMarkDepartmentInactiveWhenAllEmployeesTerminated() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setActive(true);

        when(repo.findById(1L)).thenReturn(Optional.of(dept));
        when(employeeRepository.findByDepartmentIdAndStatusNot(1L, EmployeeStatus.TERMINATED))
                .thenReturn(List.of());  
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.delete(1L);

        assertFalse(dept.isActive());
        verify(repo).save(dept);
    }

    @Test
    void delete_shouldThrowWhenDepartmentHasActiveEmployees() {
        Department dept = new Department();
        dept.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(dept));
        when(employeeRepository.findByDepartmentIdAndStatusNot(1L, EmployeeStatus.TERMINATED))
                .thenReturn(List.of(new Employee()));  

        assertThrows(IllegalStateException.class, () -> service.delete(1L));
        verify(repo, never()).save(any());
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }
}