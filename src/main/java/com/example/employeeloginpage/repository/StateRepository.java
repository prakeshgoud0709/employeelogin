package com.example.employeeloginpage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.employeeloginpage.entity.State;


@Repository
public interface StateRepository extends JpaRepository<State, Long> {
//	@Query("SELECT s FROM State s WHERE s.statename = :statename")
	Optional<State> findByStatename(String statename);
	
	@Query("SELECT s FROM State s LEFT JOIN FETCH s.districts")
	List<State> findAllStatesWithDistricts();
	
//	State findByStatename(String statename); // Find state by name

}

