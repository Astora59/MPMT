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

    private LocalDateTime project_creation_date;

    @Column(name = "users_email", nullable = false, length = 50)
    private String email;





}
