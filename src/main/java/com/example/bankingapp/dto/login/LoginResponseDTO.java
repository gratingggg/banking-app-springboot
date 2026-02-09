package com.example.bankingapp.dto.login;

import com.example.bankingapp.Role;

public class LoginResponseDTO {

    public LoginResponseDTO(String username, Role role, String token){
        this.username = username;
        this.role = role;
        this.token = token;
    }

    private String username;

    private Role role;

    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
