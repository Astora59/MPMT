package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTaskSuccess() throws Exception {
        UUID projectId = UUID.randomUUID();

        TaskCreationRequest request = new TaskCreationRequest();
        request.setTaskTitle("Créer la maquette");
        request.setTaskDescription("Faire la maquette du dashboard");
        request.setTaskDeadline(LocalDateTime.now().plusDays(3));
        request.setTaskStatus("pending");
        request.setTaskPriority("high");

        Task mockTask = new Task();
        mockTask.setTaskTitle(request.getTaskTitle());
        mockTask.setTaskDescription(request.getTaskDescription());
        mockTask.setTaskDeadline(request.getTaskDeadline());
        mockTask.setTaskStatus(request.getTaskStatus());
        mockTask.setTaskPriority(request.getTaskPriority());

        // Simule un retour normal du service
        when(taskService.createTask(eq(projectId), any(), any(TaskCreationRequest.class)))
                .thenReturn(mockTask);

        mockMvc.perform(post("/projects/{projectId}/tasks", projectId)
                        .principal(() -> "user@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskTitle").value("Créer la maquette"))
                .andExpect(jsonPath("$.taskDescription").value("Faire la maquette du dashboard"))
                .andExpect(jsonPath("$.taskStatus").value("pending"));
    }

    @Test
    void testCreateTaskProjectNotFound() throws Exception {
        UUID projectId = UUID.randomUUID();

        TaskCreationRequest request = new TaskCreationRequest();
        request.setTaskTitle("Créer la maquette");

        // Simule une erreur levée par le service
        when(taskService.createTask(eq(projectId), any(), any(TaskCreationRequest.class)))
                .thenThrow(new RuntimeException("Projet introuvable"));

        mockMvc.perform(post("/projects/{projectId}/tasks", projectId)
                        .principal(() -> "user@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Projet introuvable"));
    }
}
