package com.example.bankingapp;

import com.example.bankingapp.repository.CustomerRepository;
import com.example.bankingapp.repository.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, EmployeeRepository employeeRepository){
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return customerRepository.findByUsername(username)
                .map(customer -> new User(
                        customer.getUsername(),
                        customer.getPassword(),
                        List.of(new SimpleGrantedAuthority(customer.getRole().name()))
                ))
                .orElseGet(() -> employeeRepository.findByUsername(username)
                        .map(employee -> new User(
                                employee.getUsername(),
                                employee.getPassword(),
                                List.of(new SimpleGrantedAuthority(employee.getRole().name()))
                        ))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found.")));
    }
}
