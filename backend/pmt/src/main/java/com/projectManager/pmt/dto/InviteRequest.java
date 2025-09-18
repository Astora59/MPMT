package com.projectManager.pmt.dto;

public class InviteRequest {
    private String email;
    private String roleName; // "member" ou "observer"

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
