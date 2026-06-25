package com.hrportal.service;

import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.repository.UserRepository;
import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveSummary;
import com.hrportal.entity.User;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.exception.BadRequestException;
import com.hrportal.auth.SecurityContextHelper;
import com.hrportal.dto.EmployeeDto;
import com.hrportal.dto.EmployeePatchDto;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.status.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repo;
    private final DepartmentService departmentService;
    private final LeaveSummaryRepository leaveSummaryRepository;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    public Employee create(EmployeeDto dto) {
        if (repo.findByUsername(dto.username()).isPresent()) {
          throw new DuplicateResourceException("Employee already exists with username: " + dto.username());
        }
        
        Department dept = departmentService.getById(dto.departmentId());
        if(!dept.isActive()) {
            throw new BadRequestException("Department is inactive");
        }

        if (securityContextHelper.isHR()) {
          if (!dto.departmentId().equals(securityContextHelper.getDepartmentId())) {
              throw new BadRequestException("Access denied: HR can only create employees in their department");
            }
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

    public List<Employee> getAll() { 
        if (securityContextHelper.isHR()){
          return repo.findByDepartmentId(securityContextHelper.getDepartmentId());
        }
        return repo.findAll();
    }

    public Employee getById(Long id) {
        Employee emp = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));

        if(securityContextHelper.isHR()){
            if(!emp.getDepartment().getId().equals(securityContextHelper.getDepartmentId())) {
            throw new BadRequestException("Access denied: Employee not in your department");
            }
        }

        if (securityContextHelper.isEmployee()) {
            if (!id.equals(securityContextHelper.getEmployeeId())) {
            throw new BadRequestException("Access denied: Employee can only access their own profile");
            }
        }
        return emp;
    }

    public List<Employee> getByDepartment(Long deptId) { 
        departmentService.getById(deptId);
        if (securityContextHelper.isHR() && !deptId.equals(securityContextHelper.getDepartmentId())) {
            throw new BadRequestException("Access denied: HR can only view employees of their own department");
        }
        return repo.findByDepartmentId(deptId); 
    }

    public List<Employee> search(String name) {
        List<Employee> results = repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
        if(results.isEmpty()) {
            throw new ResourceNotFoundException("No employee found with name: " + name);
        }
        if (securityContextHelper.isHR()) {
        Long deptId = securityContextHelper.getDepartmentId();
        results = results.stream()
                .filter(e -> e.getDepartment().getId().equals(deptId))
                .toList();
        if (results.isEmpty()) throw new ResourceNotFoundException("No employee found with name: " + name + " in your department");
      }
      return results;
    }

    public Employee patch(Long id, EmployeePatchDto dto) {  
      Employee emp = getById(id);
      if(repo.findByUsername(dto.username()).isPresent()){
            throw new DuplicateResourceException("Employee already exists with username: " + dto.username());
      }

      if (securityContextHelper.isHR()) {
          if (!emp.getDepartment().getId().equals(securityContextHelper.getDepartmentId())) {
              throw new BadRequestException("Access denied: HR can only update employee information of their department");
        }
    }

      if (!dto.departmentId().equals(securityContextHelper.getDepartmentId())) {
              throw new BadRequestException("The department cannot be updated");
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
        if (securityContextHelper.isHR()) {
        if (!emp.getDepartment().getId().equals(securityContextHelper.getDepartmentId())) {
            throw new BadRequestException("Access denied: HR can only delete employees of their department");
        }
        User targetUser = userRepository.findById(emp.getUsername()).orElseThrow();
        if(targetUser.getRole()==Role.HR){
            throw new BadRequestException("Only System Administrators have permission to delete HR records");
        }
    }
        if(emp.getStatus()==EmployeeStatus.TERMINATED){
            throw new IllegalStateException("Employee is already deleted");
        }
    emp.setStatus(EmployeeStatus.TERMINATED);
    repo.save(emp);
  }
}
