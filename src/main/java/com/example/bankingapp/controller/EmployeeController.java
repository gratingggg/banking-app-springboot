package com.example.bankingapp.controller;

import com.example.bankingapp.dto.employee.EmployeeResponseDTO;
import com.example.bankingapp.dto.loan.LoanResponseDTO;
import com.example.bankingapp.dto.transaction.TransactionResponseDTO;
import com.example.bankingapp.service.EmployeeService;
import com.example.bankingapp.utils.Endpoints;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(Endpoints.EMPLOYEE_ME)
    public ResponseEntity<EmployeeResponseDTO> getMyDetailsEmployee(Principal principal){
        EmployeeResponseDTO employeeResponseDTO = employeeService.getMyDetails(principal.getName());
        return ResponseEntity.ok(employeeResponseDTO);
    }

    @PostMapping(Endpoints.EMPLOYEE_LOANS_PROCESS)
    public ResponseEntity<LoanResponseDTO> processLoan(@PathVariable Long loanId,
                                                       String action,
                                                       Principal principal){
        LoanResponseDTO loanResponseDTO = employeeService.processLoan(loanId, action, principal.getName());
        return ResponseEntity.ok(loanResponseDTO);
    }

    @PostMapping(Endpoints.EMPLOYEE_LOANS_DISBURSE)
    public ResponseEntity<TransactionResponseDTO> disburseLoan(@PathVariable Long loanId, Principal principal){
        TransactionResponseDTO transactionResponseDTO = employeeService.disburseLoan(loanId, principal.getName());
        return ResponseEntity.ok(transactionResponseDTO);
    }
}
