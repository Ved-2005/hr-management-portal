package com.hrportal.controller;

import com.hrportal.auth.JwtUtil;

import com.hrportal.common.ApiResponse;

import com.hrportal.dto.LoginDto;
import com.hrportal.dto.RegisterDto;

import com.hrportal.entity.Employee;
import com.hrportal.entity.User;

import com.hrportal.exception.ResourceNotFoundException;

import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.UserRepository;

import com.hrportal.status.Role;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginDto dto) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok("Login successful", jwtUtil.generateToken(user)));
        }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDto dto) {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username already exists"));
        }

        if ((dto.role() == Role.HR || dto.role() == Role.EMPLOYEE) && dto.employeeId() == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Employee ID is strictly required when registering an HR or Employee account."));
        }

        Employee employee = null;
        if (dto.employeeId() != null) {
            employee = employeeRepository.findById(dto.employeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + dto.employeeId()));
        }

        userRepository.save(User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .employee(employee)
                .build());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", null));
        }
}