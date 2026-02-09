package com.example.bankingapp.controller;

import com.example.bankingapp.dto.employee.EmployeeLoginRequestDTO;
import com.example.bankingapp.dto.employee.EmployeeResponseDTO;
import com.example.bankingapp.dto.loan.LoanResponseDTO;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanType;
import com.example.bankingapp.service.EmployeeService;
import com.example.bankingapp.utils.Endpoints;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
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

    @PostMapping(Endpoints.EMPLOYEE_ME)
    public ResponseEntity<EmployeeResponseDTO> processEmployeeLogin(@Valid @RequestBody EmployeeLoginRequestDTO employeeLoginDTO){
        EmployeeResponseDTO employeeResponseDTO = employeeService.processEmployeeLogin(employeeLoginDTO);
        return ResponseEntity.ok(employeeResponseDTO);
    }

    @GetMapping(Endpoints.EMPLOYEE_LOANS_PENDING)
    public ResponseEntity<Page<Loan>> getPendingLoans(@RequestParam(required = false, defaultValue = "0") int page,
                                                      @RequestParam(required = false, defaultValue = "10") int size,
                                                      @RequestParam(required = false) LoanType type,
                                                      Principal principal){
        Page<Loan> loans = employeeService.getPendingLoans(page, size, type, principal.getName());
        return ResponseEntity.ok(loans);
    }

    @PostMapping(Endpoints.EMPLOYEE_LOANS_PROCESS)
    public ResponseEntity<LoanResponseDTO> processLoan(@PathVariable Long loanId,
                                                       String action,
                                                       Principal principal){
        LoanResponseDTO loanResponseDTO = employeeService.processLoan(loanId, action, principal.getName());
        return ResponseEntity.ok(loanResponseDTO);
    }
}
