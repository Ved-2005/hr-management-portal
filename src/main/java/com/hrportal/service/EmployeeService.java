package com.hrportal.service;

import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveSummary;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.exception.BadRequestException;
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
    private final LeaveSummaryRepository leaveSummaryRepository;

    public Employee create(EmployeeDto dto) {
        if (repo.findByUsername(dto.username()).isPresent()) {
          throw new DuplicateResourceException("Employee already exists with username: " + dto.username());
        }
        
        Department dept = departmentService.getById(dto.departmentId());
        if(!dept.isActive()) {
            throw new BadRequestException("Department is inactive");
        }
        
        Employee emp = Employee.builder()
                .firstName(dto.firstName()).lastName(dto.lastName())
                .username(dto.username())
                .designation(dto.designation()).salary(dto.salary())
                .status(EmployeeStatus.ACTIVE)
                .department(departmentService.getById(dto.departmentId()))
                .build();

        Employee saved = repo.save(emp);

        leaveSummaryRepository.save(LeaveSummary.builder()
                .employee(saved)
                .sickLeaveBalance(dept.getSickLeaves())
                .casualLeaveBalance(dept.getCasualLeaves())
                .paidLeaveBalance(dept.getPaidLeaves())
                .build());

        return saved;
    }

    public List<Employee> getAll() { return repo.findAll(); }

    public Employee getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    public List<Employee> getByDepartment(Long deptId) { 
        departmentService.getById(deptId);
        return repo.findByDepartmentId(deptId); 
    }

    public List<Employee> search(String name) {
        List<Employee> results = repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
        if(results.isEmpty()) {
            throw new ResourceNotFoundException("No employee found with name: " + name);
        }
      return results;
    }

    public Employee update(Long id, EmployeeDto dto) {
        Employee emp = getById(id);
        repo.findByUsername(dto.username()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(id)) {
                throw new DuplicateResourceException("Username is already taken by another employee.");
            }
        });
        emp.setFirstName(dto.firstName());
        emp.setLastName(dto.lastName());
        emp.setUsername(dto.username());
        emp.setDesignation(dto.designation());
        emp.setSalary(dto.salary());
        emp.setDepartment(departmentService.getById(dto.departmentId()));
        return repo.save(emp);
    }

    public Employee patch(Long id, EmployeePatchDto dto) {  
      Employee emp = getById(id);
      if(repo.findByUsername(dto.username()).isPresent()){
            throw new DuplicateResourceException("Employee already exists with username: " + dto.username());
      }
      if (dto.firstName() != null) emp.setFirstName(dto.firstName());
      if (dto.lastName() != null) emp.setLastName(dto.lastName());
      if (dto.username() != null) emp.setUsername(dto.username());
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
