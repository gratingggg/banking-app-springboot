package com.example.bankingapp.controller;

import com.example.bankingapp.dto.customer.CustomerLoginDTO;
import com.example.bankingapp.dto.customer.CustomerRequestDTO;
import com.example.bankingapp.dto.customer.CustomerResponseDTO;
import com.example.bankingapp.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home/customer")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    private List<Map<String, Object>> getMaps(){
        Map<String, Object> username = new LinkedHashMap<>();
        username.put("name", "username");
        username.put("required", true);
        Map<String, Object> password = new LinkedHashMap<>();
        password.put("name", "password");
        password.put("required", true);

        return new ArrayList<>(List.of(username, password));
    }

    private Map<String, Object> personalMapMaker(String name,String pattern){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("required", true);
        if(pattern != null){
            map.put("pattern", pattern);
        }

        return map;
    }

    private List<Map<String, Object>> personalRegisterListMaker(){
        Map<String, Object> name = personalMapMaker("name",  null);
        Map<String, Object> email = personalMapMaker("email", "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$\n");
        Map<String, Object> phoneNumber = personalMapMaker("phoneNumber", "^\\d{10}$");
        Map<String, Object> dateOfBirth = personalMapMaker("dateOfBirth", "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(19|20)\\d\\d$\n");
        Map<String, Object> address = personalMapMaker("address", null);
        Map<String, Object> gender = personalMapMaker("gender", null);
        Map<String, Object> aadharNo = personalMapMaker("aadharNo", "^\\d{12}$");

        List<Map<String, Object>> fields = getMaps();
        fields.addAll(List.of(name, email, phoneNumber, dateOfBirth, address, gender, aadharNo));
        return fields;
    }

    @GetMapping("/register")
    public Map<String, Object> initCustomerRegistration(){
        Map<String, Object> form = new LinkedHashMap<>();

        form.put("Registration", "Customer");

        List<Map<String, Object>> fields = personalRegisterListMaker();
        form.put("fields", fields);

        Map<String, String> actions = new LinkedHashMap<>();
        actions.put("method", "POST");
        actions.put("path", "/home/employee/register");
        form.put("actions",actions);

        return form;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> processCustomerRegistration(@Valid @RequestBody CustomerRequestDTO customerRequestDTO){
        CustomerResponseDTO customerResponseDTO = customerService.processCustomerRegistration(customerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerResponseDTO);
    }

    @GetMapping("/login")
    public Map<String, Object> initCustomerLogin(){
        Map<String, Object> form = new LinkedHashMap<>();

        form.put("Login", "Customer");

        List<Map<String, Object>> fields = getMaps();
        form.put("fields", fields);

        Map<String, String> actions = new LinkedHashMap<>();
        actions.put("method", "POST");
        actions.put("path", "/home/customer/login");
        form.put("actions", actions);

        return form;
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerResponseDTO> processCustomerLogin(@Valid @RequestBody CustomerLoginDTO customerLoginDTO){
        CustomerResponseDTO customerResponseDTO = customerService.processCustomerLogin(customerLoginDTO);
        return ResponseEntity.status(HttpStatus.OK).body(customerResponseDTO);
    }
}
