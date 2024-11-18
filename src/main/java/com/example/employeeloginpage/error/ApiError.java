package com.example.employeeloginpage.error;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private String message;
    private List<String> details;
}
