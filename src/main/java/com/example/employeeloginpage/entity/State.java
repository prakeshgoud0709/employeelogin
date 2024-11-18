package com.example.employeeloginpage.entity;

import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class State {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stateid;

    private String statename;
//	
//    @OneToMany(mappedBy = "state", cascade = CascadeType.ALL)
//    private List<District> districts;
    
 // Initialize the list to avoid null pointer exceptions
//    @OneToMany(mappedBy = "state", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<District> districts = new ArrayList<>();

    
    @OneToMany(mappedBy = "state", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<District> districts = new ArrayList<>();

    
    public State(String statename) {
        this.statename = statename;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
        for (District district : districts) {
            district.setState(this); // Maintain bi-directional relationship
        }
    }
}
