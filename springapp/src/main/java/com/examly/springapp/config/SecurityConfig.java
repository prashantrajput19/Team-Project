package com.examly.springapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .cors(cors -> {})
          .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth

              .requestMatchers("/api/register", "/api/login", "/api/otp/**").permitAll()
              .requestMatchers("/error").permitAll()

              .requestMatchers(HttpMethod.GET,    "/api/architects/top-rated").authenticated()
              .requestMatchers(HttpMethod.GET,    "/api/architects/locations").authenticated()
              .requestMatchers(HttpMethod.POST,   "/api/architects").hasRole("Admin")
              .requestMatchers(HttpMethod.GET,    "/api/architects").authenticated()
              .requestMatchers(HttpMethod.GET,    "/api/architects/*").hasRole("Admin")
              .requestMatchers(HttpMethod.PUT,    "/api/architects/*").hasRole("Admin")
              .requestMatchers(HttpMethod.DELETE, "/api/architects/*").hasRole("Admin")

              .requestMatchers(HttpMethod.GET,    "/api/inquiries/user/*").hasRole("User")
              .requestMatchers(HttpMethod.POST,   "/api/inquiries/*/subinquiries").authenticated()
              .requestMatchers(HttpMethod.POST,   "/api/inquiries").hasRole("User")
              .requestMatchers(HttpMethod.GET,    "/api/inquiries").hasRole("Admin")
              .requestMatchers(HttpMethod.PUT,    "/api/inquiries/*").hasRole("Admin")
              .requestMatchers(HttpMethod.DELETE, "/api/inquiries/*").hasRole("User")

              .requestMatchers(HttpMethod.GET,    "/api/feedback/user/*").hasRole("User")
              .requestMatchers(HttpMethod.POST,   "/api/feedback").hasRole("User")
              .requestMatchers(HttpMethod.GET,    "/api/feedback").hasRole("Admin")
              .requestMatchers(HttpMethod.GET,    "/api/feedback/all").hasRole("Admin")
              .requestMatchers(HttpMethod.DELETE, "/api/feedback/*").hasRole("User")
              .requestMatchers(HttpMethod.GET,    "/api/feedback/*").authenticated()

              .anyRequest().authenticated())
          .addFilterBefore(jwtAuthenticationFilter,
                           UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}