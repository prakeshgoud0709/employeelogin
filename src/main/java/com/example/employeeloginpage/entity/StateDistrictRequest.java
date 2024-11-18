package com.example.employeeloginpage.entity;

import java.util.ArrayList;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StateDistrictRequest {
    private String stateName;
    private List<String> districts = new ArrayList<>(); // Initialize with an empty list

    
}

