package com.example.bankingapp.service;

import com.example.bankingapp.Role;
import com.example.bankingapp.dto.login.LoginRequestDTO;
import com.example.bankingapp.dto.login.LoginResponseDTO;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.exception.InvalidCredentialsException;
import com.example.bankingapp.repository.CustomerRepository;
import com.example.bankingapp.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginService(CustomerRepository customerRepository,
                        EmployeeRepository employeeRepository,
                        PasswordEncoder passwordEncoder,
                        TokenService tokenService){
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    private LoginResponseDTO buildResponse(String username, Role role){
        String token = tokenService.generateToken(username, role);
        return new LoginResponseDTO(username, role, token);
    }

    private void validatePassword(String rawPassword, String encodedPassword){
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO){

        Optional<Customer> customerOptional = customerRepository.findByUsername(loginRequestDTO.getUsername());
        if(customerOptional.isPresent()){
            Customer customer = customerOptional.get();
            validatePassword(loginRequestDTO.getPassword(), customer.getPassword());
            return buildResponse(customer.getUsername(), customer.getRole());
        }

        Optional<Employee> employeeOptional = employeeRepository.findByUsername(loginRequestDTO.getUsername());
        if(employeeOptional.isPresent()){
            Employee employee = employeeOptional.get();
            validatePassword(loginRequestDTO.getPassword(), employee.getPassword());
            return buildResponse(employee.getUsername(), employee.getRole());
        }

        throw new InvalidCredentialsException("Invalid username or password");
    }
}
