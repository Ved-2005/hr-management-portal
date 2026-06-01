package com.hrportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.hrportal.dto.DepartmentDto;
import com.hrportal.entity.Department;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.exception.DuplicateResourceException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repo;

    public Department create(DepartmentDto dto) {
         if (repo.findByName(dto.name()).isPresent()) {
          throw new DuplicateResourceException("Department already exists: " + dto.name());
      }
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