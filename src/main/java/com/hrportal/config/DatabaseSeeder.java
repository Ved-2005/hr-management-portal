package com.hrportal.config;

import com.hrportal.entity.Department;
import com.hrportal.entity.Employee;
import com.hrportal.entity.LeaveSummary;
import com.hrportal.entity.User;
import com.hrportal.repository.DepartmentRepository;
import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.LeaveSummaryRepository;
import com.hrportal.repository.UserRepository;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.status.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveSummaryRepository leaveSummaryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {

            Department itDept = Department.builder()
                    .name("IT Administration")
                    .description("System Level Administrative Department")
                    .active(true)
                    .sickLeaves(10)
                    .casualLeaves(10)
                    .paidLeaves(20)
                    .build();
            itDept = departmentRepository.save(itDept);

            Employee adminEmployee = Employee.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .username("syadmin")
                    .designation("System Administrator")
                    .salary(100000.0)
                    .status(EmployeeStatus.ACTIVE)
                    .department(itDept)
                    .build();
            employeeRepository.save(adminEmployee);

            User adminUser = User.builder()
                    .username(adminEmployee.getUsername())
                    .password(passwordEncoder.encode("syadmin"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminUser);

            LeaveSummary summary = LeaveSummary.builder()
                .employee(adminEmployee)
                .sickLeaveBalance(itDept.getSickLeaves())
                .casualLeaveBalance(itDept.getCasualLeaves())
                .paidLeaveBalance(itDept.getPaidLeaves())
                .build();
            leaveSummaryRepository.save(summary);
            
        }
    }
}