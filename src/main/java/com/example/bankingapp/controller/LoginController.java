package com.example.bankingapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class LoginController {

    @GetMapping
    public Map<String, List<String>> getLandingPage(){
        List<String> loginOptions = new ArrayList<>(List.of("employee", "customer", "register"));
        Map<String, List<String>> options = new HashMap<>();
        options.put("options", loginOptions);

        return options;
    }
}
