package com.example.employeeloginpage.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.employeeloginpage.entity.District;
import com.example.employeeloginpage.entity.Employee;
import com.example.employeeloginpage.entity.EmployeeDTO;
import com.example.employeeloginpage.entity.EmployeeSignin;
import com.example.employeeloginpage.entity.Role;
import com.example.employeeloginpage.entity.State;
import com.example.employeeloginpage.entity.StateDistrictRequest;
import com.example.employeeloginpage.exception.UserNotFoundException;
import com.example.employeeloginpage.repository.DistrictRepository;
import com.example.employeeloginpage.repository.EmployeeRepository;
import com.example.employeeloginpage.repository.StateRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AuthService {
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private StateRepository stateRepository;
    
    @Autowired
    private DistrictRepository districtRepository;
    
    public List<State> getAllStates() {
        return stateRepository.findAll();
    }

    public List<String> getDistrictsByStateName(String statename) {
        return districtRepository.findDistrictsByStateName(statename);
    }
    
   


    @Transactional
    public void signUp(EmployeeDTO employeeDTO) {
        // Log the incoming employeeDTO
        System.out.println("Signing up employee: " + employeeDTO);

        // Check if the email already exists
        if (employeeRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Encode the password
        String encodedPassword = passwordEncoder.encode(employeeDTO.getPassword());

        // Check if the State already exists by name
        Optional<State> existingStateOpt = stateRepository.findByStatename(employeeDTO.getStatename());
        State state;

        if (existingStateOpt.isPresent()) {
            state = existingStateOpt.get(); // Fetch the existing state
        } else {
            throw new RuntimeException("State does not exist: " + employeeDTO.getStatename());
        }

        // Validate the districts against the state's available districts
        List<String> availableDistricts = state.getDistricts()
                                                 .stream()
                                                 .map(District::getDistrictname)
                                                 .collect(Collectors.toList());

        for (String districtName : employeeDTO.getDistricts()) {
            if (!availableDistricts.contains(districtName)) {
                throw new RuntimeException("Invalid district: " + districtName + " for state: " + state.getStatename());
            }
        }

        // Create new Employee instance
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setRole(employeeDTO.getRole());
        employee.setPassword(encodedPassword);
        employee.setStatename(state.getStatename()); // Use state name from persisted entity
        employee.setDistricts(employeeDTO.getDistricts()); // Set the list of district names

        // Save the new employee
        employeeRepository.save(employee);
        System.out.println("Employee saved: " + employee);
    }


    
    public ResponseEntity<?> getUserRole(String email) {
        // Fetch the employee as an Optional
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        
        // Check if the employee is present and return the role
        if (employeeOpt.isPresent()) {
            String role = employeeOpt.get().getRole().name(); // Assuming 'getRole()' returns an enum
            return ResponseEntity.ok(role); // Return 200 OK with role in the response
        } else {
            String errorMessage = "Employee not found with email: " + email;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage); // Return 404 NOT FOUND with error message
        }
    }



    // SignIn method
    public String signIn(EmployeeSignin employeeSignin) {
        // Check if the employee exists
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(employeeSignin.getEmail());

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            // Check if the password matches
            if (passwordEncoder.matches(employeeSignin.getPassword(), employee.getPassword())) {
                return "Sign-in successful for " + employee.getName();
            } else {
                // If password does not match
                return "Invalid  password";
            }
        } else {
            // If employee is not found
            return "Invalid email or password";
        }
    }
    
    @Transactional
    public State createOrUpdateStateWithDistricts(StateDistrictRequest request) {
        // Retrieve existing state by name
        Optional<State> existingStateOpt = stateRepository.findByStatename(request.getStateName());
        State state;

        if (existingStateOpt.isPresent()) {
            // If the state exists, get it
            state = existingStateOpt.get();
        } else {
            // If the state doesn't exist, create a new one
            state = new State();
            state.setStatename(request.getStateName());
            // Save the new state to make it persistent before using it in districts
            stateRepository.save(state);
        }

        // Create a list to hold the districts to be added/updated
        List<District> updatedDistricts = new ArrayList<>();

        for (String districtName : request.getDistricts()) {
            // Check if the district already exists for the current state
            Optional<District> existingDistrictOpt = districtRepository.findByDistrictnameAndState(districtName, state);

            if (existingDistrictOpt.isPresent()) {
                // If it exists, we can add it to the list without modification
                updatedDistricts.add(existingDistrictOpt.get());
            } else {
                // If it doesn't exist, create a new district
                District newDistrict = new District();
                newDistrict.setDistrictname(districtName);
                newDistrict.setState(state); // Associate with the state
                updatedDistricts.add(newDistrict);
            }
        }

        // Update the state with the districts, replacing old ones
        state.getDistricts().clear(); // Clear existing districts if you want to replace
        state.getDistricts().addAll(updatedDistricts); // Add updated districts

        // Save the state, which will also save the new districts if cascading is set
        stateRepository.save(state);

        return state; // Return the updated state object
    }

    
    
 // Retrieve employees created by the specified admin email
    public List<Employee> getEmployeesCreatedByAdmin(String email) {
        return employeeRepository.findEmployeesByCreatedBy(email);
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
    }
    public Optional<Employee> getByEmail(String email) {
    	return employeeRepository.getByEmail(email);
    }

 // Method to fetch all employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll(); // Fetch all employees from the database
    }

  // Find employee by ID
 public Employee findById(Long id) {
     return employeeRepository.findById(id).orElse(null);
 }

 // Update employee name
 public void updateEmployeeName(Long id, String newName, String adminName) {
	    // Find the employee by ID, or throw an exception if not found
	    Employee employee = employeeRepository.findById(id)
	            .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

	    // Get the old name
	    String oldName = employee.getName();

	    // Format the new name as "newName-adminName"
	    String formattedNewName = oldName+","+newName + "-" + adminName;

	    // Update the employee's name
	    employee.setName(formattedNewName);

	    // Optionally, log the old name (if you have a field to store it)
	    // This can also be done in a history table if you're using one
	    // employee.setPreviousNames(oldName); // Uncomment if you have this field

	    // Save the updated employee entity
	    employeeRepository.save(employee);

	    // You can print or log for debugging
	    System.out.println("Updated name from '" + oldName + "' to '" + formattedNewName + "'");
	}




    //**************************
    // Update an employee's password
    public String updatePassword(String email, String newPassword) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setPassword(passwordEncoder.encode(newPassword)); // Encode new password
            employeeRepository.save(employee);
            return "Password updated successfully.";
        }

        return "Employee not found.";
    }
    
    public List<Employee> findByRole(String role) {
        return employeeRepository.findByRole(role);
    }
   
 // AuthService.java
    // Update the method to accept String for role instead of Role enum
 // Updated createEmployee method to handle state and districts
    public Employee createEmployee(String name, String email, String password, Role role, String createdBy, State state, List<String> districts) {
        // Encode the password
        String encodedPassword = passwordEncoder.encode(password);

        // Create a new Employee instance
        Employee employee = new Employee();
        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(encodedPassword);
        employee.setRole(role); // Set the role as an enum
        employee.setCreatedBy(createdBy); // Store the admin's name or email
        employee.setStatename(state.getStatename()); // Set the state name from the persisted entity
        employee.setDistricts(districts); // Set the list of district names

        // Log the employee details before saving
        System.out.println("Creating employee: " + employee);

        // Save the employee to the database
        return employeeRepository.save(employee);
    }


 // This method should decode the token and retrieve the admin information (e.g., admin ID or username)
    public String getAdminByToken(String token) {
        // Validate the token
        if (isValidToken(token)) {
            // Extract the admin's username or ID from the token
            return extractAdminFromToken(token);
        }
        return null; // Return null if the token is invalid
    }

    private boolean isValidToken(String token) {
        // Implement your token validation logic here
        return token != null && token.startsWith("Bearer ");
    }

    private String extractAdminFromToken(String token) {
        // This is a placeholder implementation; you should use a proper JWT library.
        // Assuming the token format is: "Bearer adminUser"
        return token.substring(7); // Extracts "adminUser" part
    }
    
    public Employee getByName(String name) {
        Employee employee = employeeRepository.findByName(name);
        if (employee == null) {
            throw new EntityNotFoundException("Employee not found with name: " + name);
        }
        return employee;
    }


}
