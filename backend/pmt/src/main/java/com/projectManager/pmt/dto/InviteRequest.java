package com.projectManager.pmt.dto;

public class InviteRequest {

    private String email;
    private String roleName = "member"; // facultatif, "member" par défaut

    // getters et setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String role) {
        this.roleName = role;
    }
}
