package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.dto.TaskUpdateRequest;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;

    @Test
    void updateTask_success() throws Exception {

        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        String currentUserEmail = "admin@test.com";

        // 🔥 Mock de SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUserEmail);
        SecurityContextHolder.setContext(securityContext);

        // 🔥 Préparation de la tâche mise à jour
        Task updatedTask = new Task();
        updatedTask.setTaskId(taskId);
        updatedTask.setTaskTitle("Updated Title");
        updatedTask.setTaskDescription("Updated Description");
        updatedTask.setTaskStatus("IN_PROGRESS");

        when(taskService.updateTask(Mockito.eq(projectId), Mockito.eq(taskId),
                Mockito.eq(currentUserEmail), Mockito.any(TaskUpdateRequest.class)))
                .thenReturn(updatedTask);

        // 🔥 Requête
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTaskTitle("Updated Title");
        updateRequest.setTaskDescription("Updated Description");
        updateRequest.setTaskStatus("IN_PROGRESS");

        mockMvc.perform(put("/projects/" + projectId + "/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskTitle").value("Updated Title"))
                .andExpect(jsonPath("$.taskDescription").value("Updated Description"))
                .andExpect(jsonPath("$.taskStatus").value("IN_PROGRESS"));
    }
}
