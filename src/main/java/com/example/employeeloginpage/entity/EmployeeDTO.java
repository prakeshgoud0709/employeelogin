package com.example.employeeloginpage.entity;

import java.util.List;


import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private String name;
    private String email;
    @Pattern(
    	    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$",
    	    message = "Password must include at least one digit, one lowercase letter, one uppercase letter, and one special character, and must be at least 8 characters long."
    	)
    private String password;
    
    private Role role; // Include role in DTO 
//   

    private String statename; // This should be the state name selected in the form
    
    private List<String> districts; // Optionally, you can include this
    
}