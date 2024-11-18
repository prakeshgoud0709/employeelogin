package com.example.employeeloginpage.service;


import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.employeeloginpage.entity.Employee;
import com.example.employeeloginpage.repository.EmployeeRepository;


@Service
public class CustomUserDetailsServices implements UserDetailsService {
    
    @Autowired
    private EmployeeRepository employeeRepository;

    // Example UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the Employee entity from the repository using the provided email
        Optional<Employee> employeeOptional = employeeRepository.findByEmail(email);
        
        // Check if the employee exists; if not, throw an exception
        if (!employeeOptional.isPresent()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Get the Employee object from the Optional
        Employee employee = employeeOptional.get();

        // Return UserDetails implementation using Employee
        return new org.springframework.security.core.userdetails.User(
            employee.getEmail(), // Assuming username is the email
            employee.getPassword(), // Make sure this is the encoded password
            getAuthorities(employee) // Get authorities based on employee's roles
        );
    }

    // Method to convert roles to GrantedAuthority
    private Collection<? extends GrantedAuthority> getAuthorities(Employee employee) {
        // Convert Role(s) to GrantedAuthority
        return Collections.singletonList(new SimpleGrantedAuthority(employee.getRole().name()));
    }
}
