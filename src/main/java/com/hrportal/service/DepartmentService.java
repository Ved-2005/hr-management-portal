package com.hrportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.hrportal.dto.DepartmentDto;
import com.hrportal.entity.Department;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repo;

    public Department create(DepartmentDto dto) {
        Department dept = Department.builder()
                            .name(dto.name())
                            .description(dto.description())
                            .build();
        return repo.save(dept);
    }

    public List<Department> getAll() { return repo.findAll(); }

    public Department getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    public void delete(Long id) {
        getById(id);
        repo.deleteById(id);
    }
}