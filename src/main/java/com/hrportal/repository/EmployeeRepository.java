package com.hrportal.repository;

import com.hrportal.entity.Employee;
import com.hrportal.status.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String first, String last);
    Optional<Employee> findByUsername(String username);
    List<Employee> findByDepartmentIdAndStatusNot(Long departmentId, EmployeeStatus status);
}