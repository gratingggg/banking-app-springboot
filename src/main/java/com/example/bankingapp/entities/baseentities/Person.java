package com.example.bankingapp.entities.baseentities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@MappedSuperclass
public class Person extends BaseEntity {
    @Column(name = "name")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Column(name = "email")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Column(name = "phone_number")
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\d{10}$", message = "Invalid phone number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    @NotNull(message = "Date of birth cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    @NotBlank(message = "Address cannot be blank")
    private String address;

    @Column(name = "gender")
    @NotNull(message = "Gender cannot be null")
    private PersonGender gender;

    @Column(name = "username", nullable = false)
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password cannot be blank")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PersonGender getGender() {
        return gender;
    }

    public void setGender(PersonGender gender) {
        this.gender = gender;
    }
}
