package com.example.bankingapp.service;

import com.example.bankingapp.dto.employee.EmployeeLoginDTO;
import com.example.bankingapp.dto.employee.EmployeeResponseDTO;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(final EmployeeRepository employeeRepository, final PasswordEncoder passwordEncoder){
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private EmployeeResponseDTO mapEmployeeToDTO(Employee employee){
        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO();
        employeeResponseDTO.setAddress(employee.getAddress());
        employeeResponseDTO.setEmail(employee.getEmail());
        employeeResponseDTO.setGender(employee.getGender());
        employeeResponseDTO.setName(employee.getName());
        employeeResponseDTO.setPhoneNumber(employee.getPhoneNumber());
        employeeResponseDTO.setUsername(employee.getUsername());
        employeeResponseDTO.setDateOfBirth(employee.getDateOfBirth());
        employeeResponseDTO.setEmployeeRole(employee.getEmployeeRole());
        employeeResponseDTO.setEmployeeStatus(employee.getEmployeeStatus());

        return employeeResponseDTO;
    }

    public EmployeeResponseDTO processEmployeeLogin(EmployeeLoginDTO employeeLoginDTO){
        if(employeeLoginDTO.getUsername() != null || employeeLoginDTO.getUsername().isBlank()){
            Employee employee = employeeRepository.findByUsername(employeeLoginDTO.getUsername())
                    .orElseThrow(() -> new IllegalStateException("The employee with the username " + employeeLoginDTO.getUsername() + " does not exist." +
                            "Please enter the valid username."));

            if(passwordEncoder.matches(employeeLoginDTO.getPassword(), employee.getPassword())){
                return mapEmployeeToDTO(employee);
            }
            throw new IllegalArgumentException("Password do not match. Please enter the correct password.");
        }
        throw new IllegalArgumentException("Please enter a valid username.");
    }
}
