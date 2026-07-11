package com.hrportal.controller;

import com.hrportal.common.ApiResponse;
import com.hrportal.dto.request.AuthDTORequest;
import com.hrportal.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody AuthDTORequest dto) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", service.login(dto)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody AuthDTORequest dto) {
        service.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", null));
    }
}
