package com.projectManager.pmt.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
public class Notification {

    @Id
    @GeneratedValue
    private UUID notificationId;

    private String email;

    private String taskId;

    private LocalDateTime sentDate;
}
