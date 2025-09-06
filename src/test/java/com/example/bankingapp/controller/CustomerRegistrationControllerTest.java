package com.example.bankingapp.controller;

import com.example.bankingapp.dto.customer.CustomerRequestDTO;
import com.example.bankingapp.entities.baseentities.PersonGender;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.repository.CustomerRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CustomerRegistrationControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    private final MockMvc mockMvc;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerRegistrationControllerTest(MockMvc mockMvc, CustomerRepository customerRepository,
                                              PasswordEncoder passwordEncoder){
        this.mockMvc = mockMvc;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private CustomerRequestDTO createCustomerRequestDTO(int i){
        CustomerRequestDTO customer = new CustomerRequestDTO();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword("secret" + i);
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + i, 1, 1));
        customer.setAadharNo("1231231231" + i );
        customer.setPhoneNumber("12341234" + i);

        return customer;
    }

    private Customer createCustomer(int i){
        Customer customer = new Customer();
        customer.setName("Rudra " + i + " Ceaser");
        customer.setUsername("rudra1" + i + "23");
        customer.setPassword(passwordEncoder.encode("secret" + i));
        customer.setEmail("ru" + i + "dra@example.com");
        customer.setGender(PersonGender.MALE);
        customer.setAddress("Mars" + i);
        customer.setDateOfBirth(LocalDate.of(2000 + i, 1, 1));
        customer.setAadharNo("1231231231" + i );
        customer.setPhoneNumber("12341234" + i);

        return customer;
    }

    private ResultMatcher[] assertCustomerDTO(int i){
        return new ResultMatcher[]{
                jsonPath("$.name").value("Rudra " + i + " Ceaser"),
                jsonPath("$.username").value("rudra1" + i + "23"),
                jsonPath("$.email").value("ru" + i + "dra@example.com"),
                jsonPath("$.gender").value("MALE"),
                jsonPath("$.address").value("Mars" + i),
                jsonPath("$.dateOfBirth").value("01-01-" + (2000 + i)),
                jsonPath("$.phoneNumber").value("12341234" + i)
        };
    }

    @Test
    public void WhenRegisterCustomer_thenOk() throws Exception{
        CustomerRequestDTO customer = createCustomerRequestDTO(10);
        String requestBody = objectMapper.writeValueAsString(customer);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpectAll(assertCustomerDTO(10));
    }

    @Test
    public void WhenRegisterBlankCustomer_thenInvalidError() throws Exception{
        CustomerRequestDTO dto = new CustomerRequestDTO();
        dto.setUsername("");
        dto.setName("");
        dto.setPassword("");
        dto.setAddress("");
        dto.setGender(PersonGender.MALE);
        dto.setEmail("");
        dto.setPhoneNumber("");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        dto.setAadharNo("");

        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username cannot be blank."))
                .andExpect(jsonPath("$.errors.password").value("Password cannot be blank."))
                .andExpect(jsonPath("$.errors.name").value("Name cannot be blank."))
                .andExpect(jsonPath("$.errors.address").value("Address cannot be blank."))
                .andExpect(jsonPath("$.errors.email").value("Email cannot be blank."))
                .andExpect(jsonPath("$.errors.phoneNumber").value("Phone number must be exactly 10 digits."))
                .andExpect(jsonPath("$.errors.aadharNo").value("Aadhar number must be exactly 12 digits."));
    }

    @Test
    public void WhenInvalidPhoneNumber_ThenBadRequest() throws Exception{
        CustomerRequestDTO dto = createCustomerRequestDTO(11);
        dto.setPhoneNumber("abc12#4231");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneNumber").value("Phone number must be exactly 10 digits."));
    }

    @Test
    public void WhenInvalidEmail_ThenBadRequest() throws Exception{
        CustomerRequestDTO dto = createCustomerRequestDTO(12);
        dto.setEmail("abdslksjgasdfa");
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format."));
    }

    @Test
    public void WhenRegisterWithDuplicateEmail_ThenConflict() throws Exception{
        Customer customer = createCustomer(13);
        customerRepository.save(customer);
        CustomerRequestDTO dto = createCustomerRequestDTO(14);
        dto.setEmail(customer.getEmail());
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The email " + dto.getEmail() + " is already registered. Please enter another email."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void WhenRegisterWithDuplicatePhoneNumber_ThenConflict() throws Exception{
        Customer customer = createCustomer(15);
        customerRepository.save(customer);
        CustomerRequestDTO dto = createCustomerRequestDTO(16);
        dto.setPhoneNumber(customer.getPhoneNumber());
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The phone number " + dto.getPhoneNumber() + " is already registered. Please enter another phone number."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }
    @Test
    public void WhenRegisterWithDuplicateAadharNumber_ThenConflict() throws Exception{
        Customer customer = createCustomer(17);
        customerRepository.save(customer);
        CustomerRequestDTO dto = createCustomerRequestDTO(18);
        dto.setAadharNo(customer.getAadharNo());
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The aadhar number " + dto.getAadharNo() + " is already registered. Please enter another aadhar number."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void WhenRegisterWithDuplicateUsername_ThenConflict() throws Exception{
        Customer customer = createCustomer(19);
        customerRepository.save(customer);
        CustomerRequestDTO dto = createCustomerRequestDTO(20);
        dto.setUsername(customer.getUsername());
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The username " + dto.getUsername() + " is already taken. Please choose another username."))
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CONFLICT.value()));
    }

    @Test
    public void WhenRegisterWithInvalidGender_ThenBadRequest() throws Exception{
        CustomerRequestDTO dto = createCustomerRequestDTO(21);
        String requestBody = objectMapper.writeValueAsString(dto);
        requestBody = requestBody.replace("\"MALE\"", "\"INVALID\"");
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gender").value( "Invalid gender. Allowed genders : MALE, FEMALE, TRANSGENDER"));
    }

    @Test
    public void WhenRegisterWithInvalidDOB_ThenBadRequest() throws Exception{
        CustomerRequestDTO dto = createCustomerRequestDTO(22);
        dto.setDateOfBirth(LocalDate.of(2026, 8, 8));
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc
                .perform(post("/home/customer/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dateOfBirth").value("Invalid date. Please enter a valid date."));
    }
}
