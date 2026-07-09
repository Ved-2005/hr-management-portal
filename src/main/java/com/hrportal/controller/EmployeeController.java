package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import com.hrportal.entity.Employee;
import com.hrportal.dto.EmployeeDto;
import com.hrportal.dto.EmployeePatchDto;
import com.hrportal.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> create(@Valid @RequestBody EmployeeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Employee created", service.create(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Employee>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Employees fetched", service.getAll()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<Employee>> getById(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.ok("Employee fetched", service.getByUsername(username)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Employee>>> search(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok("Search results", service.search(name)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<ApiResponse<List<Employee>>> getByDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(ApiResponse.ok("Filtered", service.getByDepartment(deptId)));
    }

    @PatchMapping("/{username}")
    public ResponseEntity<ApiResponse<Employee>> patch(@PathVariable String username, @Valid @RequestBody EmployeePatchDto dto) {
      return ResponseEntity.ok(ApiResponse.ok("Employee updated", service.patch(username, dto)));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String username) {
        service.delete(username);
        return ResponseEntity.ok(ApiResponse.ok("Employee deleted", null));
    }
}
