package com.projectManager.pmt.controllers;

import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class TaskController {

    @Autowired
    private TaskService taskService;



    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<Task> createTask(@PathVariable UUID projectId,@RequestBody TaskCreationRequest taskCreationRequest, @AuthenticationPrincipal UserDetails userDetails) {

        Task task = taskService.createTask(projectId, userDetails.getUsername(), taskCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);


    }

}
