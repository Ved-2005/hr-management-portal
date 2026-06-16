package com.hrportal.auth;

import com.hrportal.entity.Employee;
import com.hrportal.entity.User;

import com.hrportal.repository.EmployeeRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

import java.util.Date;

@Component
public class JwtUtil {
    
    private final EmployeeRepository employeeRepository;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    JwtUtil(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {

        Employee employee = employeeRepository.findByUsername(user.getUsername()).orElse(null);

        Long employeeId = (employee != null) ? employee.getId() : null;
        Long departmentId = (employee != null && employee.getDepartment() != null) 
                            ? employee.getDepartment().getId() : null;
                            
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .claim("employeeId", employeeId)
                .claim("departmentId", departmentId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getKey()).build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

    public boolean isValid(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}