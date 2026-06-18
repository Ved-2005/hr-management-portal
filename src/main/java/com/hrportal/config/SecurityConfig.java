package com.hrportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.hrportal.exception.CustomAccessDeniedHandler;

import com.hrportal.auth.JwtAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(e -> e
                .accessDeniedHandler(accessDeniedHandler)
                )


            .authorizeHttpRequests(a -> a
            .requestMatchers("/api/auth/**").permitAll()

            .requestMatchers(HttpMethod.GET, "/api/departments", "/api/departments/**").hasAnyAuthority("ADMIN", "HR")
            .requestMatchers(HttpMethod.POST, "/api/departments").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasAuthority("ADMIN")

            .requestMatchers(HttpMethod.POST, "/api/employees").hasAnyAuthority("ADMIN", "HR")
            .requestMatchers(HttpMethod.GET, "/api/employees").hasAnyAuthority("ADMIN", "HR")
            .requestMatchers(HttpMethod.GET, "/api/employees/search").hasAnyAuthority("ADMIN", "HR")
            .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyAuthority("ADMIN", "HR", "EMPLOYEE")
            .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyAuthority("ADMIN", "HR")
            .requestMatchers(HttpMethod.PATCH, "/api/employees/**").hasAnyAuthority("ADMIN", "HR")
            .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAnyAuthority("ADMIN", "HR")

            .requestMatchers(HttpMethod.POST, "/api/leaves/employee/**").hasAuthority("EMPLOYEE")
            .requestMatchers(HttpMethod.GET, "/api/leaves/pending").hasAuthority("HR")
            .requestMatchers(HttpMethod.PATCH, "/api/leaves/*/approve").hasAuthority("HR")
            .requestMatchers(HttpMethod.PATCH, "/api/leaves/*/reject").hasAuthority("HR")
            .requestMatchers(HttpMethod.GET, "/api/leaves/employee/**").hasAnyAuthority("HR", "EMPLOYEE")
            .requestMatchers(HttpMethod.GET, "/api/leaves/total/**").hasAnyAuthority("HR", "EMPLOYEE")
            .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}