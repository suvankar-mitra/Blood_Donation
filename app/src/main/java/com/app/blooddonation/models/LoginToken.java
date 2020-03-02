package com.app.blooddonation.models;

import java.util.Date;

public class LoginToken {
    private String token;

    public LoginToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
