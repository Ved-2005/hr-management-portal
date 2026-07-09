package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import com.hrportal.service.TimeOffService;
import com.hrportal.entity.TimeOff;
import com.hrportal.dto.TimeOffDto;

import jakarta.validation.Valid;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class TimeOffController {
    private final TimeOffService service;

    @PostMapping("/employee/{username}")
    public ResponseEntity<ApiResponse<TimeOff>> apply(@PathVariable String username,@Valid @RequestBody TimeOffDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Successfully applied for leave", service.apply(username,dto)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TimeOff>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending leaves", service.getPending()));
    }

    @GetMapping("/employee/{username}")
    public ResponseEntity<ApiResponse<List<TimeOff>>> getByEmployee(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.ok("Employee leaves", service.getByEmployee(username)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TimeOff>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Leave approved", service.approve(id)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<TimeOff>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Leave rejected", service.reject(id)));
    }

    @GetMapping("/employee/{username}/total-leaves")
    public ResponseEntity<ApiResponse<Long>> getTotalLeavesTaken(@PathVariable String username) {
      return ResponseEntity.ok(ApiResponse.ok("Total leaves taken", service.getTotalLeavesTaken(username)));
  }
}
