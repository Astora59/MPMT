package com.projectManager.pmt.services;

import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.models.Task;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface TaskService {
    Task createTask(UUID projectId,UUID userId, TaskCreationRequest taskCreationRequest);
}
