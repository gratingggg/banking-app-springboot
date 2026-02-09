package com.example.bankingapp.controller;

import com.example.bankingapp.dto.login.LoginRequestDTO;
import com.example.bankingapp.dto.login.LoginResponseDTO;
import com.example.bankingapp.service.LoginService;
import com.example.bankingapp.utils.Endpoints;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Endpoints.LOGIN)
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService){
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO){
        LoginResponseDTO responseDTO = loginService.loginUser(loginRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
