package com.example.employeeloginpage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.employeeloginpage.service.CustomUserDetailsServices;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsServices customUserDetailsServices;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .requestMatchers("/api/auth/**").permitAll() // Allow access to authentication endpoints
//                .requestMatchers("/api/auth/updatename").permitAll() // Allow access to the update name page
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/signin")
                .successHandler((request, response, authentication) -> {
                    var authorities = authentication.getAuthorities();
                    String redirectUrl = authorities.stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))
                            ? "/adminwelcome"
                            : "/employeewelcome";
                    response.sendRedirect(redirectUrl);
                })
                .permitAll()
            .and()
            .logout()
                .logoutSuccessUrl("/signin")
                .invalidateHttpSession(true)
                .permitAll();
        
        return http.build();
    }


    
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(customUserDetailsServices)
            .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
