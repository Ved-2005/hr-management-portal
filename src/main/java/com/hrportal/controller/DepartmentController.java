package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.hrportal.service.DepartmentService;
import com.hrportal.entity.Department;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.dto.DepartmentDto;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService service;

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> create(@Valid @RequestBody DepartmentDto dto) {
         try {
          return ResponseEntity.status(HttpStatus.CREATED)
                  .body(ApiResponse.ok("Department created", service.create(dto)));
      } catch (DuplicateResourceException ex) {
          return ResponseEntity.status(HttpStatus.CONFLICT)
                  .body(ApiResponse.error(ex.getMessage()));
      }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Departments fetched", service.getAll()));
    }

    @GetMapping("/{id}")
     public ResponseEntity<ApiResponse<Department>> getById(@PathVariable Long id) {
      try {
          return ResponseEntity.ok(ApiResponse.ok("Department fetched", service.getById(id)));
      } catch (ResourceNotFoundException ex) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
                  .body(ApiResponse.error(ex.getMessage()));
      }
  }

    @DeleteMapping("/{id}")  
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
      try {
          service.delete(id);
          return ResponseEntity.ok(ApiResponse.ok("Department deleted", null));
      } catch (ResourceNotFoundException ex) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
                  .body(ApiResponse.error(ex.getMessage()));
      }
  }
}