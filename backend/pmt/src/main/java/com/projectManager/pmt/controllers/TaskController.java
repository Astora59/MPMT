package com.projectManager.pmt.controllers;

import com.projectManager.pmt.dto.AssignTaskRequest;
import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.dto.TaskUpdateRequest;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class TaskController {

    @Autowired
    private TaskService taskService;




    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<Task> createTask(@PathVariable UUID projectId,@RequestBody TaskCreationRequest taskCreationRequest,  @AuthenticationPrincipal String userEmail) {

        // Récupérer les infos de l'user connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = (String) authentication.getPrincipal();


        Task task = taskService.createTask(projectId, userEmail, taskCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);


    }

    @PutMapping("/{projectId}/tasks/{taskId}/assign")
    public ResponseEntity<Task> assignTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody AssignTaskRequest request
    ) {

        // Récupérer les infos de l'user connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = (String) authentication.getPrincipal();


        Task updatedTask = taskService.assignTaskToUser(
                projectId,
                taskId,
                email, // l’utilisateur qui fait la requête
                request.getUserEmail()      // l’utilisateur assigné
        );

        return ResponseEntity.ok(updatedTask);
    }


    @PutMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody TaskUpdateRequest updateRequest
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        Task updatedTask = taskService.updateTask(projectId, taskId, email, updateRequest);
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @AuthenticationPrincipal String userEmail
    ) {
        Task task = taskService.getTaskById(projectId, taskId, userEmail);
        return ResponseEntity.ok(task);
    }



}
