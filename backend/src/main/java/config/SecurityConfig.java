package com.insurai.insurai_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import com.insurai.insurai_backend.config.EmployeeJwtAuthenticationFilter;
import com.insurai.insurai_backend.config.AgentJwtAuthenticationFilter;
import com.insurai.insurai_backend.config.HrJwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final EmployeeJwtAuthenticationFilter employeeFilter;
    private final AgentJwtAuthenticationFilter agentFilter;
    private final HrJwtAuthenticationFilter hrFilter;

  
    public SecurityConfig(EmployeeJwtAuthenticationFilter employeeFilter,
                          AgentJwtAuthenticationFilter agentFilter,
                          HrJwtAuthenticationFilter hrFilter) {
        this.employeeFilter = employeeFilter;
        this.agentFilter = agentFilter;
        this.hrFilter = hrFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .cors(cors -> {}) 
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC ENDPOINTS (Login avvadaniki ivi permitAll undali)
                .requestMatchers(
                    "/hello",
                    "/auth/**",
                    "/uploads/**",
                    "/employee/login",
                    "/employee/register",
                    "/agent/register",
                    "/agent/login",
                    "/hr/login",
                    "/admin/policies",
                    "/admin/policies/save"
                ).permitAll()

                // 2. ROLE BASED ACCESS
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .requestMatchers("/hr/**").hasRole("HR")
                .requestMatchers("/agent/**").hasRole("AGENT")

                // 3. OTHERS
                .anyRequest().authenticated()
            )
            .httpBasic(h -> h.disable())
            .formLogin(f -> f.disable());

        
        http.addFilterBefore(employeeFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(agentFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(hrFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "http://localhost:8077")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}