package com.hrportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.hrportal.dto.request.DepartmentDTORequest;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.repository.EmployeeRepository;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.exception.BadRequestException;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.exception.DuplicateResourceException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repo;
    private final EmployeeRepository employeeRepository;
    public Department create(DepartmentDTORequest dto) {
         if (dto.name() == null || dto.name().isBlank()) {
             throw new BadRequestException("Department name is required");
         }
         if (dto.sickLeaves() == null) {
             throw new BadRequestException("Sick leaves is required");
         }
         if (dto.casualLeaves() == null) {
             throw new BadRequestException("Casual leaves is required");
         }
         if (dto.paidLeaves() == null) {
             throw new BadRequestException("Paid leaves is required");
         }
         if (repo.findByName(dto.name()).isPresent()) {
          throw new DuplicateResourceException("Department already exists: " + dto.name());
      }
        Department dept = Department.builder()
                            .name(dto.name())
                            .description(dto.description())
                            .sickLeaves(dto.sickLeaves())
                            .casualLeaves(dto.casualLeaves())
                            .paidLeaves(dto.paidLeaves())
                            .build();
        return repo.save(dept);
    }

    public List<Department> getAll() { return repo.findAll(); }

    public Department getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    public void delete(Long id) {
      Department dept = getById(id);
      List<Employee> activeEmployees = employeeRepository.findByDepartmentIdAndStatusNot(id, EmployeeStatus.TERMINATED);
      if (!activeEmployees.isEmpty()) {
          throw new IllegalStateException("Cannot delete department with active employees");
      }
      if(!dept.isActive()) {
          throw new IllegalStateException("Department is already deleted");
      }
      dept.setActive(false); 
      repo.save(dept);
  }
}