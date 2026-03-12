package com.projectManager.pmt.dto;

import java.util.UUID;

public class LoginResponse {

    private String token;
    private UUID users_id;

    public LoginResponse(String token, UUID users_id) {
        this.token = token;
        this.users_id = users_id;
    }

    public String getToken() {
        return token;
    }

    public UUID getUsers_id() {
        return users_id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsers_id(UUID users_id) {
        this.users_id = users_id;
    }
}