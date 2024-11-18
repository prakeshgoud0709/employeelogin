package com.example.employeeloginpage.entity;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequest {
    private String email;
    @Pattern(
    	    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$",
    	    message = "Password must include at least one digit, one lowercase letter, one uppercase letter, and one special character, and must be at least 8 characters long."
    	)
    private String newPassword;
    
    // Getters and Setters
}