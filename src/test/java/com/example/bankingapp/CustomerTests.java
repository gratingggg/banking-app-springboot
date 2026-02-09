package com.example.bankingapp;

import com.example.bankingapp.dto.customer.CustomerLoginRequestDTO;
import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.repository.CustomerRepository;
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
public class CustomerTests {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerTests(MockMvc mockMvc,
                         ObjectMapper objectMapper,
                         CustomerRepository customerRepository,
                         PasswordEncoder passwordEncoder){
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Customer createCustomer(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        Customer customer = new Customer();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword(passwordEncoder.encode("secret" + i));
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + num, 1, 1));
        customer.setAadharNo("123123123" + i );
        customer.setPhoneNumber("1234123" + i);

        return customer;
    }

    private CustomerLoginRequestDTO createCustomerLoginDTO(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        CustomerLoginRequestDTO dto = new CustomerLoginRequestDTO();
        dto.setUsername("rudra1" + i + "23");
        dto.setPassword("secret" + i);

        return dto;
    }

    private ResultMatcher[] assertCustomerDTO(int num){
        String i = "" + num;
        if(num < 100) i = "0" + num;
        return new ResultMatcher[]{
                jsonPath("$.name").value("Rudra " + i + " Ceaser"),
                jsonPath("$.username").value("rudra1" + i + "23"),
                jsonPath("$.email").value("ru" + i + "dra@example.com"),
                jsonPath("$.gender").value("MALE"),
                jsonPath("$.address").value("Mars" + i),
                jsonPath("$.dateOfBirth").value("01-01-" + (2000 + num)),
                jsonPath("$.phoneNumber").value("1234123" + i)
        };
    }

    @Test
    public void WhenGetCustomer_ThenOk() throws Exception{
        Customer customer = createCustomer(23);
        customerRepository.save(customer);
        CustomerLoginRequestDTO dto = createCustomerLoginDTO(23);
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(Endpoints.CUSTOMER_ME)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpectAll(assertCustomerDTO(23));
    }

    @Test
    public void WhenLoginWrongEmail_ThenUnauthorized() throws Exception{
        Customer customer = createCustomer(24);
        customerRepository.save(customer);
        CustomerLoginRequestDTO dto = createCustomerLoginDTO(24);
        dto.setUsername("rudra123");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(Endpoints.CUSTOMER_ME)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The customer with the username rudra123 does not exist.Please enter the valid username."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void WhenLoginWrongPassword_ThenUnauthorized() throws Exception{
        Customer customer = createCustomer(25);
        customerRepository.save(customer);
        CustomerLoginRequestDTO dto = createCustomerLoginDTO(25);
        dto.setPassword("secret");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(Endpoints.CUSTOMER_ME)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password do not match. Please enter the correct password."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void WhenLoginMissingUsername_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(26);
        customerRepository.save(customer);
        CustomerLoginRequestDTO dto = createCustomerLoginDTO(26);
        dto.setUsername("");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(Endpoints.CUSTOMER_ME)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username cannot be blank."));
    }

    @Test
    public void WhenLoginMissingPassword_ThenBadRequest() throws Exception{
        Customer customer = createCustomer(27);
        customerRepository.save(customer);
        CustomerLoginRequestDTO dto = createCustomerLoginDTO(27);
        dto.setPassword("");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post(Endpoints.CUSTOMER_ME)
                        .with(user(customer.getUsername()).roles(customer.getRole().toString()))
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password cannot be blank."));
    }
}
