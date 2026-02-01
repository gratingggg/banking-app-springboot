package com.example.bankingapp.service;

import com.example.bankingapp.dto.employee.EmployeeLoginDTO;
import com.example.bankingapp.dto.employee.EmployeeResponseDTO;
import com.example.bankingapp.dto.loan.LoanResponseDTO;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.employee.EmployeeStatus;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import com.example.bankingapp.exception.*;
import com.example.bankingapp.repository.EmployeeRepository;
import com.example.bankingapp.repository.LoanRepository;
import com.example.bankingapp.specification.LoanSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoanRepository loanRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder,
                           LoanRepository loanRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.loanRepository = loanRepository;
    }

    private EmployeeResponseDTO mapEmployeeToDTO(Employee employee) {
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

    private Employee validateEmployee(String username) {
        Employee employee = employeeRepository.findByUsername(username).orElseThrow(EmployeeNotFoundException::new);
        if (!employee.getEmployeeStatus().equals(EmployeeStatus.ACTIVE)) throw new EmployeeInactiveException();
        return employee;
    }

    public EmployeeResponseDTO processEmployeeLogin(EmployeeLoginDTO employeeLoginDTO) {

        Employee employee = employeeRepository.findByUsername(employeeLoginDTO.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password."));

        if (!passwordEncoder.matches(employeeLoginDTO.getPassword(), employee.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        if (employee.getEmployeeStatus() != EmployeeStatus.ACTIVE) {
            throw new EmployeeInactiveException();
        }

        return mapEmployeeToDTO(employee);
    }

    public Page<Loan> getPendingLoans(int page, int size, LoanType type, String username) {
        validateEmployee(username);

        Pageable pageable = PageRequest.of(page, size);
        Specification<Loan> spec = LoanSpecification.withStatus(LoanStatus.PENDING)
                .and(LoanSpecification.withType(type));
        return loanRepository.findAll(spec, pageable);
    }

    public LoanResponseDTO processLoan(Long loanId, String action, String username){
        Employee employee = validateEmployee(username);

        Loan loan = loanRepository.findById(loanId).orElseThrow(LoanNotFoundException::new);

        if (loan.getLoanStatus() != LoanStatus.PENDING)
            throw new LoanAlreadyProcessedException();

        if (action.equalsIgnoreCase("APPROVE")) {
            loan.setLoanStatus(LoanStatus.APPROVED);
        } else if (action.equalsIgnoreCase("REJECT")) {
            loan.setLoanStatus(LoanStatus.REJECTED);
        } else {
            throw new InvalidActionException("Action must be APPROVE or REJECT");
        }

        loan.setApprovedBy(employee);
        loan.setDateOfIssuance(LocalDate.now());

        loanRepository.save(loan);

        return new LoanResponseDTO(loan);
    }
}
