package com.hrportal.service;

import com.hrportal.repository.EmployeeRepository;
import com.hrportal.entity.Employee;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.dto.EmployeeDto;
import com.hrportal.dto.EmployeePatchDto;
import com.hrportal.status.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repo;
    private final DepartmentService departmentService;

    public Employee create(EmployeeDto dto) {
        if (repo.findByEmail(dto.email()).isPresent()) {
          throw new DuplicateResourceException("Employee already exists with email: " + dto.email());
        }
        Employee emp = Employee.builder()
                .firstName(dto.firstName()).lastName(dto.lastName())
                .email(dto.email())
                .designation(dto.designation()).salary(dto.salary())
                .status(EmployeeStatus.ACTIVE)
                .department(departmentService.getById(dto.departmentId()))
                .build();
        return repo.save(emp);
    }

    public List<Employee> getAll() { return repo.findAll(); }

    public Employee getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    public List<Employee> getByDepartment(Long deptId) { return repo.findByDepartmentId(deptId); }

    public List<Employee> search(String name) {
        return repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    public Employee update(Long id, EmployeeDto dto) {
        Employee emp = getById(id);
        if(repo.findByEmail(dto.email()).isPresent()){
            throw new DuplicateResourceException("Employee already exists with email: " + dto.email());
        }
        emp.setFirstName(dto.firstName());
        emp.setLastName(dto.lastName());
        emp.setEmail(dto.email());
        emp.setDesignation(dto.designation());
        emp.setSalary(dto.salary());
        emp.setDepartment(departmentService.getById(dto.departmentId()));
        return repo.save(emp);
    }

    public Employee patch(Long id, EmployeePatchDto dto) {  
      Employee emp = getById(id);
      if(repo.findByEmail(dto.email()).isPresent()){
            throw new DuplicateResourceException("Employee already exists with email: " + dto.email());
      }
      if (dto.firstName() != null) emp.setFirstName(dto.firstName());
      if (dto.lastName() != null) emp.setLastName(dto.lastName());
      if (dto.email() != null) emp.setEmail(dto.email());
      if (dto.designation() != null) emp.setDesignation(dto.designation());
      if (dto.salary() != null) emp.setSalary(dto.salary());
      if (dto.departmentId() != null) emp.setDepartment(departmentService.getById(dto.departmentId()));
      return repo.save(emp);
  }

    public void delete(Long id) {
      Employee emp = getById(id);
      emp.setStatus(EmployeeStatus.TERMINATED);
      repo.save(emp);
  }
}
