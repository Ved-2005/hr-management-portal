package com.hrportal.hr_management_api.employee;

import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.dto.request.EmployeeDTORequest;
import com.hrportal.service.DepartmentService;
import com.hrportal.service.EmployeeService;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.exception.BadRequestException;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.auth.SecurityContextHelper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

  @ExtendWith(MockitoExtension.class)
  class EmployeeServiceTest {

    @Mock private EmployeeRepository repo;
    @Mock private DepartmentService departmentService;
    @Mock private LeaveSummaryRepository leaveSummaryRepository;
    @Mock private SecurityContextHelper securityContextHelper;
    @InjectMocks private EmployeeService service;

    private Department mockDepartment() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setName("Engineering");
        return dept;
    }

    private EmployeeDTORequest validDto() {
        return new EmployeeDTORequest("John", "Doe", "johnray", "Dev", 50000.0, 1L);
    }

    @Test
    void getAll_shouldReturnList() {
        when(repo.findAll()).thenReturn(List.of(new Employee(), new Employee()));

        assertEquals(2, service.getAll().size());
    }

    @Test
    void create_shouldSaveEmployee() {
        when(repo.findByUsername("johnray")).thenReturn(Optional.empty());
        when(departmentService.getById(1L)).thenReturn(mockDepartment());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Employee result = service.create(validDto());

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("johnray", result.getUsername());
        assertEquals("Dev", result.getDesignation());
        assertEquals(50000.0, result.getSalary());
        assertEquals("Engineering", result.getDepartment().getName());
        assertEquals(EmployeeStatus.ACTIVE, result.getStatus());
        verify(repo).save(any());
    }

    @Test
    void create_shouldThrowWhenUsernameAlreadyExists() {
        when(repo.findByUsername("johnray")).thenReturn(Optional.of(new Employee()));

        assertThrows(DuplicateResourceException.class, () -> service.create(validDto()));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDepartmentIsInactive() {
        Department inactiveDept = new Department();
        inactiveDept.setId(1L);
        inactiveDept.setActive(false);

        when(repo.findByUsername("johnray")).thenReturn(Optional.empty());
        when(departmentService.getById(1L)).thenReturn(inactiveDept);

        assertThrows(BadRequestException.class, () -> service.create(validDto()));
        verify(repo, never()).save(any());
   }

    @Test
    void create_shouldThrowWhenDepartmentNotFound() {
        when(repo.findByUsername("johnray")).thenReturn(Optional.empty());
        when(departmentService.getById(1L)).thenThrow(new ResourceNotFoundException("Department not found: 1"));

        assertThrows(ResourceNotFoundException.class, () -> service.create(validDto()));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenFirstNameIsNull() {
        EmployeeDTORequest dto = new EmployeeDTORequest(null, "Doe", "johnray", "Dev", 50000.0, 1L);

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenFirstNameIsBlank() {
        EmployeeDTORequest dto = new EmployeeDTORequest("  ", "Doe", "johnray", "Dev", 50000.0, 1L);

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenLastNameIsNull() {
        EmployeeDTORequest dto = new EmployeeDTORequest("John", null, "johnray", "Dev", 50000.0, 1L);

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenUsernameIsNull() {
        EmployeeDTORequest dto = new EmployeeDTORequest("John", "Doe", null, "Dev", 50000.0, 1L);

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenDepartmentIdIsNull() {
        EmployeeDTORequest dto = new EmployeeDTORequest("John", "Doe", "johnray", "Dev", 50000.0, null);

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenSalaryIsNotPositive() {
        EmployeeDTORequest dto = new EmployeeDTORequest("John", "Doe", "johnray", "Dev", 0.0, 1L);

        assertThrows(BadRequestException.class, () -> service.create(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void patch_shouldThrowWhenSalaryIsNotPositive() {
        Employee existing = new Employee();
        existing.setUsername("abcdefg");
        when(repo.findByUsername("abcdefg")).thenReturn(Optional.of(existing));

        EmployeeDTORequest patch = new EmployeeDTORequest(null, null, null, null, -100.0, null);

        assertThrows(BadRequestException.class, () -> service.patch("abcdefg", patch));
        verify(repo, never()).save(any());
    }

    @Test
    void getByUsername_shouldReturnEmployee() {
        Employee emp = new Employee();
        emp.setFirstName("John");
        when(repo.findByUsername("johnray")).thenReturn(Optional.of(emp));

        assertEquals("John", service.getByUsername("johnray").getFirstName());
    }

    @Test
    void getByUsername_shouldThrowWhenNotFound() {
        when(repo.findByUsername("abcdefg")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByUsername("abcdefg"));
    }

    @Test
    void search_shouldReturnMatchingEmployees() {
        when(repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "john"))
                .thenReturn(List.of(new Employee()));

        assertEquals(1, service.search("john").size());
    }

    @Test
    void getByDepartment_shouldReturnEmployeesInDepartment() {
        Employee emp1 = new Employee();
        Employee emp2 = new Employee();
        when(repo.findByDepartmentId(1L)).thenReturn(List.of(emp1, emp2));

        List<Employee> result = service.getByDepartment(1L);

        assertEquals(2, result.size());
        verify(repo).findByDepartmentId(1L);
    }

    @Test
    void delete_shouldMarkEmployeeAsTerminated() {
        Employee emp = new Employee();
        emp.setStatus(EmployeeStatus.ACTIVE);
        when(repo.findByUsername("abcdefg")).thenReturn(Optional.of(emp));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.delete("abcdefg");

        assertEquals(EmployeeStatus.TERMINATED, emp.getStatus());
        verify(repo).save(emp);
        verify(repo, never()).deleteById(any());
}

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(repo.findByUsername("abcdefg")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete("abcdefg"));
    }

    @Test
    void patch_shouldUpdateOnlyProvidedFields() {
        Employee existing = new Employee();
        existing.setFirstName("John");
        existing.setLastName("Doe");
        existing.setUsername("abcdefg");
        when(repo.findByUsername("abcdefg")).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        EmployeeDTORequest patch = new EmployeeDTORequest(null, "Musk", "johnray", "Senior Dev", null, null);
        Employee result = service.patch("abcdefg", patch);

        assertEquals("John", result.getFirstName());      
        assertEquals("Musk", result.getLastName());
        assertEquals("johnray", result.getUsername());
        assertEquals("Senior Dev", result.getDesignation()); 
    }


}
