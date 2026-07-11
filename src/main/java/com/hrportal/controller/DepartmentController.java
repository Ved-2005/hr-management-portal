package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.hrportal.service.DepartmentService;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.dto.request.DepartmentDTORequest;
import com.hrportal.dto.response.DepartmentDTOResponse;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService service;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTOResponse>> create(@RequestBody DepartmentDTORequest dto) {
         try {
          return ResponseEntity.status(HttpStatus.CREATED)
                  .body(ApiResponse.ok("Department created", DepartmentDTOResponse.toDepartmentDTOResponse(service.create(dto))));
      } catch (DuplicateResourceException ex) {
          return ResponseEntity.status(HttpStatus.CONFLICT)
                  .body(ApiResponse.error(ex.getMessage()));
      }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTOResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Departments fetched",
                service.getAll().stream().map(DepartmentDTOResponse::toDepartmentDTOResponse).toList()));
    }

    @GetMapping("/{id}")
     public ResponseEntity<ApiResponse<DepartmentDTOResponse>> getById(@PathVariable Long id) {
      try {
          return ResponseEntity.ok(ApiResponse.ok("Department fetched", DepartmentDTOResponse.toDepartmentDTOResponse(service.getById(id))));
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
