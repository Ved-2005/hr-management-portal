package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import com.hrportal.service.TimeOffService;
import com.hrportal.dto.request.TimeOffDTORequest;
import com.hrportal.dto.response.TimeOffDTOResponse;

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
    public ResponseEntity<ApiResponse<TimeOffDTOResponse>> apply(@PathVariable String username,@RequestBody TimeOffDTORequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Successfully applied for leave", TimeOffDTOResponse.toTimeOffDTOResponse(service.apply(username,dto))));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<TimeOffDTOResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending leaves",
                service.getPending().stream().map(TimeOffDTOResponse::toTimeOffDTOResponse).toList()));
    }

    @GetMapping("/employee/{username}")
    public ResponseEntity<ApiResponse<List<TimeOffDTOResponse>>> getByEmployee(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.ok("Employee leaves",
                service.getByEmployee(username).stream().map(TimeOffDTOResponse::toTimeOffDTOResponse).toList()));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TimeOffDTOResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Leave approved", TimeOffDTOResponse.toTimeOffDTOResponse(service.approve(id))));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<TimeOffDTOResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Leave rejected", TimeOffDTOResponse.toTimeOffDTOResponse(service.reject(id))));
    }

    @GetMapping("/employee/{username}/total-leaves")
    public ResponseEntity<ApiResponse<Long>> getTotalLeavesTaken(@PathVariable String username) {
      return ResponseEntity.ok(ApiResponse.ok("Total leaves taken", service.getTotalLeavesTaken(username)));
  }
}
