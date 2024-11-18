package com.example.employeeloginpage.controller;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.example.employeeloginpage.entity.District;
import com.example.employeeloginpage.entity.Employee;
import com.example.employeeloginpage.entity.EmployeeDTO;
import com.example.employeeloginpage.entity.EmployeeSignin;
import com.example.employeeloginpage.entity.Role;
import com.example.employeeloginpage.entity.State;
import com.example.employeeloginpage.entity.StateDistrictRequest;
import com.example.employeeloginpage.entity.UpdatePasswordRequest;
import com.example.employeeloginpage.error.ApiError;
import com.example.employeeloginpage.exception.UserNotFoundException;
import com.example.employeeloginpage.repository.DistrictRepository;
import com.example.employeeloginpage.repository.StateRepository;
import com.example.employeeloginpage.service.AuthService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
//import ch.qos.logback.core.model.Model;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
    private AuthenticationManager authenticationManager; // Inject AuthenticationManager


    @Autowired
    private AuthService authService;
    
    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private DistrictRepository districtRepository;
    
    @GetMapping("/{name}")
    public ResponseEntity<Employee> getEmployeeByName(@PathVariable("name") String name) {
        Employee employee = authService.getByName(name);
        return ResponseEntity.ok(employee);
    }
    
    
    @PostMapping("/createOrUpdate")
    public ResponseEntity<State> createOrUpdateState(@RequestBody StateDistrictRequest request) {
        State updatedState = authService.createOrUpdateStateWithDistricts(request);
        return ResponseEntity.ok(updatedState);
    }
    
    @GetMapping("/districts")
    public ResponseEntity<List<String>> getDistrictsByState(@RequestParam("statename") String statename) {
        List<String> districts = districtRepository.findDistrictsByStateName(statename);
        if (districts.isEmpty()) {
            return ResponseEntity.noContent().build(); // Optional: return no content if no districts found
        }
        return ResponseEntity.ok(districts);
    }

    
    @GetMapping("/states")
    public ResponseEntity<List<State>> getAllStates() {
        List<State> states = stateRepository.findAll();
        return ResponseEntity.ok(states);
    }

    
    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // Get existing session, don't create a new one
        return session != null && 
               session.getAttribute("email") != null && 
               session.getAttribute("role") != null;
    }

    @GetMapping("/signup")
    public ModelAndView signUpPage() {
    	return new ModelAndView("signup"); // This will resolve to signup.html
    }
    
    @GetMapping("/signin")
    public ModelAndView signInPage(HttpServletRequest request) {
        // If the user is already authenticated, redirect to the appropriate welcome page
        if (isAuthenticated(request)) {
            String role = (String) request.getSession().getAttribute("role");
            System.out.println("User is authenticated with role: " + role); // Debug log
            if ("ADMIN".equals(role)) {
                return new ModelAndView("redirect:/api/auth/adminwelcome");
            } else {
            	
                return new ModelAndView("redirect:/api/auth/employeewelcome");
            }
        }
        System.out.println("User is not authenticated, showing sign-in page."); // Debug log
        return new ModelAndView("signin"); // This will resolve to signin.html
    }
    
    @GetMapping("/adminwelcome")
    public ModelAndView adminWelcomePage(HttpServletRequest request) {
        String adminEmail = (String) request.getSession().getAttribute("email");

        // Check admin email
        System.out.println("Session email: " + adminEmail);

        if (adminEmail == null) {
            return new ModelAndView("redirect:/api/auth/signin");
        }

        // Fetch admin details
        Employee admin = authService.findByEmail(adminEmail);
        if (admin == null) {
            return new ModelAndView("redirect:/api/auth/signin");
        }

        // Fetch all employees
        List<Employee> allEmployees = authService.getAllEmployees();
     // Store the adminName in the session
        request.getSession().setAttribute("adminName", admin.getName());
        // Debug log to check the size of employees fetched
        System.out.println("Total number of employees fetched: " + allEmployees.size());

        ModelAndView modelAndView = new ModelAndView("adminwelcome");
        modelAndView.addObject("adminRole", admin.getRole());
        modelAndView.addObject("adminName", admin.getName());
        modelAndView.addObject("employees", allEmployees); // Pass all employees to the view

        return modelAndView;
    }




    @GetMapping("/employeewelcome")
    public ModelAndView employeeWelcomePage(HttpServletRequest request) {
        String employeeEmail = (String) request.getSession().getAttribute("email");
        
        // Ensure the employee email is present in the session
        if (employeeEmail == null) {
            return new ModelAndView("redirect:/api/auth/signin"); // Redirect to sign-in if not authenticated
        }
        
        ModelAndView modelAndView = new ModelAndView("employeewelcome");
        
        // Fetch employee details
        Employee employee = authService.findByEmail(employeeEmail);
        if (employee == null) {
            return new ModelAndView("redirect:/api/auth/signin"); // Redirect if employee not found
        }
        
        // Add employee details to the model
        modelAndView.addObject("employee", employee);
        
        return modelAndView;
    }

    @GetMapping("/createemployee")
    public ModelAndView createEmployeePage() {
        // This method returns the view for the employee creation page
        return new ModelAndView("createEmployee"); // Resolves to createEmployee.html
    }
    
    
 // Get employee by ID
 // Get employee by ID
    @GetMapping("/updatename/{id}")
    public ModelAndView updateNamePage(@PathVariable("id") Long id) {
        System.out.println("Fetching employee with ID: " + id); // Debugging line
        Employee employee = authService.findById(id);
        
        if (employee != null) {
            System.out.println("Employee found: " + employee.getName());
            ModelAndView modelAndView = new ModelAndView("updatename");
            modelAndView.addObject("employee", employee);
            return modelAndView;
        } else {
            System.out.println("Employee not found, redirecting to admin welcome.");
            return new ModelAndView("redirect:/api/auth/adminwelcome");
        }
    }





    // Update employee name
    @PostMapping("/updatename/{id}")
    public ResponseEntity<String> updateEmployee(@PathVariable("id") Long id, @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
            String newName = requestBody.get("name");

            // Retrieve the admin's name from the session
            HttpSession session = request.getSession(false);
            if (session == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session not found.");
            }

            String adminName = (String) session.getAttribute("adminName");
            if (adminName == null || adminName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin name not found in session.");
            }

            // Update employee name with the admin's name
            authService.updateEmployeeName(id, newName, adminName);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }







    
    @GetMapping("/update-password")
    public ModelAndView updatePasswordPage() {
        return new ModelAndView("updatePassword"); // This will resolve to signup.html
    }
    
    @GetMapping("/logout")
    public ModelAndView logoutPage() {
    	return new ModelAndView("logout"); // This will resolve to signup.html
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody EmployeeDTO employeeDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Collect error messages from all fields
            List<String> errors = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors); // Return detailed error messages
        }
        try {
            authService.signUp(employeeDTO);
            return ResponseEntity.ok("Signup successful.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
//    @PostMapping("/signin")
//    public ModelAndView signIn(@Valid @ModelAttribute EmployeeSignin employeeSignin, HttpServletRequest request) {
//        ModelAndView modelAndView = new ModelAndView();
//
//        try {
//            // Authenticate the user
//            Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(employeeSignin.getEmail(), employeeSignin.getPassword())
//            );
//
//            // Set authentication in the security context
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            // Store email in session
//            request.getSession().setAttribute("email", employeeSignin.getEmail());
//
//            // Retrieve the user role
//            ResponseEntity<?> roleResponse = authService.getUserRole(employeeSignin.getEmail());
//            if (roleResponse.getStatusCode() == HttpStatus.OK) {
//                String role = (String) roleResponse.getBody();
//                request.getSession().setAttribute("role", role);
//
//                // Redirect based on role
//                if ("ADMIN".equals(role)) {
//                    modelAndView.setViewName("redirect:/api/auth/adminwelcome");
//                } else if ("EMPLOYEE".equals(role)) {
//                    modelAndView.setViewName("redirect:/api/auth/employeewelcome");
//                } else {
//                    // Handle any other roles or unexpected cases
//                    modelAndView.setViewName("redirect:/error");
//                }
//            } else {
//                // Handle role retrieval failure
//                modelAndView.addObject("message", "Failed to retrieve user role.");
//                modelAndView.setViewName("signin"); // Redirect to sign-in page with error
//            }
//
//        } catch (BadCredentialsException e) {
//            // Handle invalid credentials
//            modelAndView.addObject("message", "Invalid email or password.");
//            modelAndView.setViewName("signin"); // Redirect back to sign-in page with error
//        } catch (Exception e) {
//            // Handle any other exceptions
//            modelAndView.addObject("message", "An error occurred during sign-in: " + e.getMessage());
//            modelAndView.setViewName("signin"); // Redirect back to sign-in page with error
//        }
//
//        return modelAndView;
//    }
    
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody EmployeeSignin employeeSignin, HttpServletRequest request) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(employeeSignin.getEmail(), employeeSignin.getPassword())
            );

            // Set authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Store email in session
            request.getSession().setAttribute("email", employeeSignin.getEmail());

            // Retrieve the user role
            ResponseEntity<?> roleResponse = authService.getUserRole(employeeSignin.getEmail());
            if (roleResponse.getStatusCode() == HttpStatus.OK) {
                String role = (String) roleResponse.getBody();
                request.getSession().setAttribute("role", role);

                // Create a redirect URL based on the role
                String redirectUrl;
                if ("ADMIN".equals(role)) {
                    redirectUrl = "/api/auth/adminwelcome"; // URL for admin
                } else if ("EMPLOYEE".equals(role)) {
                    redirectUrl = "/api/auth/employeewelcome"; // URL for employee
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unexpected user role.");
                }

                // Return success response with redirect URL
                return ResponseEntity.ok(Map.of("message", "Sign-in successful.", "redirectUrl", redirectUrl));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to retrieve user role.");
            }

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during sign-in: " + e.getMessage());
        }
    }


    
    @PostMapping("/createemployee")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, String> employeeData, HttpServletRequest request) {
        // Get admin's name or email from the session
        String createdBy = (String) request.getSession().getAttribute("email");

        System.out.println("Received employee data: " + employeeData);

        // Extract employee details
        String name = employeeData.get("name");
        String email = employeeData.get("email");
        String password = employeeData.get("password");
        String roleString = employeeData.get("role") != null ? employeeData.get("role").toUpperCase() : null;
        String stateName = employeeData.get("state");
        String districtsValue = employeeData.get("districts");

        // Validate required fields with specific messages
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Name is required."));
        }
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required."));
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password is required."));
        }
        if (roleString == null || roleString.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Role is required."));
        }
        if (stateName == null || stateName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "State is required."));
        }

        // Validate districts
        if (districtsValue == null || districtsValue.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Districts are required."));
        }

        // Proceed with employee creation
        List<String> districtNames = Arrays.asList(districtsValue.split(","));

        try {
            // Convert roleString to Role enum
            Role role = Role.valueOf(roleString);

            // Check if the state exists
            Optional<State> existingStateOpt = stateRepository.findByStatename(stateName);
            State state;

            if (existingStateOpt.isPresent()) {
                state = existingStateOpt.get(); // Fetch the existing state
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "State does not exist: " + stateName));
            }

            // Validate the districts against the state's available districts
            List<String> availableDistricts = state.getDistricts()
                                                   .stream()
                                                   .map(District::getDistrictname)
                                                   .collect(Collectors.toList());

            for (String districtName : districtNames) {
                if (!availableDistricts.contains(districtName.trim())) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid district: " + districtName + " for state: " + state.getStatename()));
                }
            }

            // Call the service to create a new employee
            Employee createdEmployee = authService.createEmployee(name, email, password, role, createdBy, state, districtNames);
            return ResponseEntity.ok(Map.of("success", true, "message", "Employee created successfully!", "employee", createdEmployee));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid role provided."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        String result = authService.updatePassword(updatePasswordRequest.getEmail(), updatePasswordRequest.getNewPassword());
        
        if ("Password updated successfully.".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result); // Handle "Employee not found" case
        }
    }
    

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Invalidate the session to log the user out
        request.getSession().invalidate();
        return ResponseEntity.ok("Logout successful."); // Respond with a success message
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + ex.getMessage());
    }
    
    public class EmployeeNotFoundException extends RuntimeException {
        public EmployeeNotFoundException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<String> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Method Not Allowed: " + ex.getMessage());
    }
    
   
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex) {
        ApiError apiError = new ApiError(ex.getMessage(), Collections.emptyList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        // Log the exception message
        System.err.println("Exception: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

