package com.hrportal.service;

import com.hrportal.auth.JwtUtil;
import com.hrportal.dto.request.AuthDTORequest;
import com.hrportal.entity.Employee;
import com.hrportal.entity.User;
import com.hrportal.exception.BadRequestException;
import com.hrportal.exception.DuplicateResourceException;
import com.hrportal.exception.ResourceNotFoundException;
import com.hrportal.repository.EmployeeRepository;
import com.hrportal.repository.UserRepository;
import com.hrportal.status.EmployeeStatus;
import com.hrportal.status.Role;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(AuthDTORequest dto) {
        if (dto.username() == null || dto.username().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        User user = userRepository.findById(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
        return jwtUtil.generateToken(user);
    }

    public void register(AuthDTORequest dto) {
        if (dto.username() == null || dto.username().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (dto.role() == null) {
            throw new BadRequestException("Role is required");
        }
        if (userRepository.findById(dto.username()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (dto.role() == Role.ADMIN) {
            throw new BadRequestException("You can only signup for HR or employee role.");
        }

        Employee emp = employeeRepository.findByUsername(dto.username())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Signup failed: No official employee record found for username: " + dto.username()));

        if (emp.getStatus() == EmployeeStatus.TERMINATED) {
            throw new ResourceNotFoundException(
                    "Signup failed: No official employee record found for username: " + dto.username());
        }

        userRepository.save(User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .build());
    }
}
