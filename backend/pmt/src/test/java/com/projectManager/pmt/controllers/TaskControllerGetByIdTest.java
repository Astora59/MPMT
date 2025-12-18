package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerGetByIdTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;

    @Test
    void getTaskById_success() throws Exception {

        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        String userEmail = "user@test.com";

        // simulate @AuthenticationPrincipal
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userEmail);
        SecurityContextHolder.setContext(securityContext);

        // Create fake task
        Task task = new Task();
        task.setTaskId(taskId);
        task.setTaskTitle("Test Task");
        task.setTaskDescription("A sample task");
        task.setTaskStatus("PENDING");

        when(taskService.getTaskById(projectId, taskId, userEmail)).thenReturn(task);

        mockMvc.perform(get("/projects/" + projectId + "/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task_id").value(taskId.toString()))
                .andExpect(jsonPath("$.taskTitle").value("Test Task"))
                .andExpect(jsonPath("$.taskDescription").value("A sample task"))
                .andExpect(jsonPath("$.taskStatus").value("PENDING"));
    }
}

