package com.projectManager.pmt.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue
    private UUID project_id;

    private String project_name;

    private String project_description;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime project_creation_date;

    @Column(name = "project_admin", nullable = false)
    private UUID project_admin;


    @Column(name = "users_email", nullable = false, length = 50)
    private String email;

    @PrePersist
    public void prePersist() {
        if (project_creation_date == null) {
            project_creation_date = LocalDateTime.now();
        }
    }


    public UUID getProject_admin() {
        return project_admin;
    }

    public void setProject_admin(UUID project_admin) {
        this.project_admin = project_admin;
    }





    public UUID getProject_id() {
        return project_id;
    }

    public void setProject_id(UUID project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_description() {
        return project_description;
    }

    public void setProject_description(String project_description) {
        this.project_description = project_description;
    }

    public LocalDateTime getProject_creation_date() {
        return project_creation_date;
    }

    public void setProject_creation_date(LocalDateTime project_creation_date) {
        this.project_creation_date = project_creation_date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
