package com.projectManager.pmt.controllers;

import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerGetByStatusTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;

    @Test
    void getTasksByStatus_success() throws Exception {

        UUID projectId = UUID.randomUUID();
        String userEmail = "user@test.com";
        String status = "pending";

        // Mock Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userEmail);
        SecurityContextHolder.setContext(securityContext);

        // Fake tasks
        Task t1 = new Task();
        t1.setTaskId(UUID.randomUUID());
        t1.setTaskTitle("Task 1");
        t1.setTaskStatus("PENDING");

        Task t2 = new Task();
        t2.setTaskId(UUID.randomUUID());
        t2.setTaskTitle("Task 2");
        t2.setTaskStatus("PENDING");

        List<Task> tasks = List.of(t1, t2);

        when(taskService.getTasksByStatus(projectId, userEmail, status))
                .thenReturn(tasks);

        mockMvc.perform(
                        get("/projects/" + projectId + "/tasks/status/" + status)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].taskTitle").value("Task 1"))
                .andExpect(jsonPath("$[0].taskStatus").value("PENDING"))
                .andExpect(jsonPath("$[1].taskTitle").value("Task 2"))
                .andExpect(jsonPath("$[1].taskStatus").value("PENDING"));
    }
}
