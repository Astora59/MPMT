package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.dto.AssignTaskRequest;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerAssignTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignTask_success() throws Exception {

        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserEmail("target@example.com");  // utilisateur assigné

        Task updatedTask = new Task();
        updatedTask.setTaskId(taskId);
        updatedTask.setTaskTitle("Ma tâche test");

        // 🔥 Mock du service
        when(taskService.assignTaskToUser(
                eq(projectId),
                eq(taskId),
                eq("current@example.com"),
                eq("target@example.com")
        )).thenReturn(updatedTask);

        mockMvc.perform(
                        put("/projects/{projectId}/tasks/{taskId}/assign", projectId, taskId)
                                .principal(() -> "current@example.com") // user connecté
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskTitle").value("Ma tâche test"))
                .andExpect(jsonPath("$.task_id").value(taskId.toString()));

        // ✅ Vérifie que le service a bien été appelé, donc qu’il enverra un email
        verify(taskService, times(1)).assignTaskToUser(
                eq(projectId),
                eq(taskId),
                eq("current@example.com"),
                eq("target@example.com")
        );
    }
}
