package com.example.employeeloginpage.service;


import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.employeeloginpage.entity.Employee;


public class CustomUserDetails implements UserDetails {

    private String email;
    private String username;
    private String password;
    private String role; // Assuming you have a role field in Employee

    public CustomUserDetails(Employee employee) {
        this.email = employee.getEmail();
        this.username = employee.getName(); // Assuming name is used as username
        this.password = employee.getPassword();
        this.role = employee.getRole().name(); // Adjust as per your Employee model
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)); // Add ROLE_ prefix
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return getName();
    }

    @Override
    public String getPassword() {
        return password; // Return the actual password
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return getName();
	}
}
