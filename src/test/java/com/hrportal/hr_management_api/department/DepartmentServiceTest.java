package com.hrportal.hr_management_api.department;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.service.DepartmentService;
import com.hrportal.entity.Department;
import com.hrportal.dto.DepartmentDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repo;

    @InjectMocks
    private DepartmentService service;

    @Test
    void create_shouldSaveDepartment() {
        var dto = new DepartmentDto("Engineering", "Tech team");
        when(repo.findByName("Engineering")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Department result = service.create(dto);

        assertEquals("Engineering", result.getName());
        verify(repo).save(any());
    }

    @Test
    void create_shouldThrowWhenNameAlreadyExists() {
        var dto = new DepartmentDto("Engineering", "Tech team");
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
    void delete_shouldCallDeleteById() {
        when(repo.findById(1L)).thenReturn(Optional.of(new Department()));

        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
    }
}

