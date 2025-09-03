package com.example.bankingapp.controller;

import com.example.bankingapp.dto.employee.EmployeeLoginDTO;
import com.example.bankingapp.dto.employee.EmployeeResponseDTO;
import com.example.bankingapp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("home/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(final EmployeeService employeeService){
        this.employeeService = employeeService;
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

    @GetMapping
    public Map<String, Object> initEmployeeLogin(){
        Map<String, Object> form = new LinkedHashMap<>();

        form.put("Login", "Employee");

        List<Map<String, Object>> fields = getMaps();
        form.put("fields", fields);

        Map<String, String> actions = new LinkedHashMap<>();
        actions.put("method", "POST");
        actions.put("path", "home/employee/login");
        form.put("actions", actions);

        return form;
    }

    @PostMapping("/login")
    public ResponseEntity<EmployeeResponseDTO> processEmployeeLogin(@Valid @RequestBody EmployeeLoginDTO employeeLoginDTO){
        EmployeeResponseDTO employeeResponseDTO = employeeService.processEmployeeLogin(employeeLoginDTO);
        return ResponseEntity.status(HttpStatus.OK).body(employeeResponseDTO);
    }
}
