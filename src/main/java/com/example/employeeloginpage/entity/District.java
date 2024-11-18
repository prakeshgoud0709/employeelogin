package com.example.employeeloginpage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
public class District {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long districtid;

	 @Column(name = "districtname", nullable = false) // Ensure the column is not null
	    private String districtname;

	 @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "stateid", nullable = false)
	 @JsonIgnore // Prevents circular reference during serialization
	    private State state;
    
	 public District(String districtname, State state) {
	        this.districtname = districtname;
	        this.state = state;
	    }
}
