package com.projectManager.pmt.services;

import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.dto.TaskUpdateRequest;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.TaskHistory;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    Task createTask(UUID projectId,String userEmail, TaskCreationRequest taskCreationRequest);

    Task assignTaskToUser(UUID projectId, UUID taskId, String currentUserEmail, String targetUserEmail);

    Task updateTask(UUID projectId, UUID taskId, String userEmail, TaskUpdateRequest updateRequest);

    Task getTaskById(UUID projectId, UUID taskId, String userEmail);

    List<Task> getTasksByStatus(UUID projectId, String userEmail, String status);

    List<TaskHistory> getTaskHistory(UUID projectId, UUID taskId, String userEmail);


}
