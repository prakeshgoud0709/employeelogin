package com.example.employeeloginpage.entity;

public class SignInResponse {
    private String message;

    public SignInResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
