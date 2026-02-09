package com.example.bankingapp;

import com.example.bankingapp.dto.employee.EmployeeLoginRequestDTO;
import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeRole;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.repository.EmployeeRepository;
import com.example.bankingapp.utils.Endpoints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeTests {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeTests(MockMvc mockMvc,
                         ObjectMapper objectMapper,
                         EmployeeRepository employeeRepository,
                         PasswordEncoder passwordEncoder){
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Employee createEmployee(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        Employee employee = new Employee();
        employee.setName("Parth " + i + " William");
        employee.setUsername("parth1" + i + "23");
        employee.setPassword(passwordEncoder.encode("secret" + i));
        employee.setEmail("pa" + i + "rth@example.com");
        employee.setGender(PersonGender.MALE);
        employee.setAddress("Mars" + i);
        employee.setDateOfBirth(LocalDate.of(2000 + num, 1, 1));
        employee.setPhoneNumber("1234123" + i);
        employee.setEmployeeRole(EmployeeRole.TELLER);
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);

        return employee;
    }

    private EmployeeLoginRequestDTO createEmployeeLoginDTO(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        EmployeeLoginRequestDTO dto = new EmployeeLoginRequestDTO();
        dto.setUsername("parth1" + i + "23");
        dto.setPassword("secret" + i);

        return dto;
    }

    private ResultMatcher[] assertEmployeeDTO(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        return new ResultMatcher[]{
                jsonPath("$.name").value("Parth " + i + " William"),
                jsonPath("$.username").value("parth1" + i + "23"),
                jsonPath("$.email").value("pa" + i + "rth@example.com"),
                jsonPath("$.gender").value("MALE"),
                jsonPath("$.address").value("Mars" + i),
                jsonPath("$.dateOfBirth").value("01-01-" + (2000 + num)),
                jsonPath("$.phoneNumber").value("1234123" + i),
                jsonPath("$.employeeRole").value("TELLER"),
                jsonPath("$.employeeStatus").value("ACTIVE")
        };
    }

    @Test
    public void WhenLoginEmployee_ThenOk() throws Exception{
        Employee employee = createEmployee(10);
        employeeRepository.save(employee);
        EmployeeLoginRequestDTO dto = createEmployeeLoginDTO(10);
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(Endpoints.EMPLOYEE_ME)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpectAll(assertEmployeeDTO(10));
    }

    @Test
    public void WhenLoginWrongEmail_ThenUnauthorized() throws Exception{
        Employee employee = createEmployee(11);
        employeeRepository.save(employee);
        EmployeeLoginRequestDTO dto = createEmployeeLoginDTO(11);
        dto.setUsername("rudra");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(Endpoints.EMPLOYEE_ME)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void WhenLoginWrongPassword_ThenUnauthorized() throws Exception{
        Employee employee = createEmployee(12);
        employeeRepository.save(employee);
        EmployeeLoginRequestDTO dto = createEmployeeLoginDTO(12);
        dto.setPassword("secret");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(Endpoints.EMPLOYEE_ME)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void WhenLoginMissingUsername_ThenBadRequest() throws Exception{
        Employee employee = createEmployee(13);
        employeeRepository.save(employee);
        EmployeeLoginRequestDTO dto = createEmployeeLoginDTO(13);
        dto.setUsername("");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(Endpoints.EMPLOYEE_ME)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username cannot be blank."));
    }

    @Test
    public void WhenLoginMissingPassword_ThenBadRequest() throws Exception{
        Employee employee = createEmployee(14);
        employeeRepository.save(employee);
        EmployeeLoginRequestDTO dto = createEmployeeLoginDTO(14);
        dto.setPassword("");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(Endpoints.EMPLOYEE_ME)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password cannot be blank."));
    }

    @Test
    public void WhenLoginInactiveEmployee_ThenUnauthorized() throws Exception{
        Employee employee = createEmployee(15);
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
        EmployeeLoginRequestDTO dto = createEmployeeLoginDTO(15);
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(Endpoints.EMPLOYEE_ME)
                        .with(user(employee.getUsername()).roles(employee.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Your status is currently not active. Please contact the admin."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }
}
