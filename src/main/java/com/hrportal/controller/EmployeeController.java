package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import com.hrportal.dto.request.EmployeeDTORequest;
import com.hrportal.dto.response.EmployeeDTOResponse;
import com.hrportal.service.EmployeeService;
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
    public ResponseEntity<ApiResponse<EmployeeDTOResponse>> create(@RequestBody EmployeeDTORequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Employee created", EmployeeDTOResponse.toEmployeeDTOResponse(service.create(dto))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTOResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Employees fetched",
                service.getAll().stream().map(EmployeeDTOResponse::toEmployeeDTOResponse).toList()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<EmployeeDTOResponse>> getById(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.ok("Employee fetched", EmployeeDTOResponse.toEmployeeDTOResponse(service.getByUsername(username))));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmployeeDTOResponse>>> search(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok("Search results",
                service.search(name).stream().map(EmployeeDTOResponse::toEmployeeDTOResponse).toList()));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<ApiResponse<List<EmployeeDTOResponse>>> getByDept(@PathVariable Long deptId) {
        return ResponseEntity.ok(ApiResponse.ok("Filtered",
                service.getByDepartment(deptId).stream().map(EmployeeDTOResponse::toEmployeeDTOResponse).toList()));
    }

    @PatchMapping("/{username}")
    public ResponseEntity<ApiResponse<EmployeeDTOResponse>> patch(@PathVariable String username, @RequestBody EmployeeDTORequest dto) {
      return ResponseEntity.ok(ApiResponse.ok("Employee updated", EmployeeDTOResponse.toEmployeeDTOResponse(service.patch(username, dto))));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String username) {
        service.delete(username);
        return ResponseEntity.ok(ApiResponse.ok("Employee deleted", null));
    }
}
