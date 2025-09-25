package com.projectManager.pmt.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue
    private UUID role_id;

    @Column(name = "role_name", nullable = false)
    private String roleName; // admin, member, observer

    // Relation vers l'utilisateur
    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private Users user;

    // Relation vers le projet
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Getters et Setters
    public UUID getRole_id() {
        return role_id;
    }
    public void setRole_id(UUID role_id) {
        this.role_id = role_id;
    }

    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Users getUser() {
        return user;
    }
    public void setUser(Users user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }
}
