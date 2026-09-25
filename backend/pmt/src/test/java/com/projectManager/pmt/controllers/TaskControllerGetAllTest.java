package com.projectManager.pmt.controllers;

import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerGetAllTest {

    private static final String USER_EMAIL = "user@test.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @BeforeEach
    void authenticate() {
        // Ce endpoint utilise authentication.getName() : on fournit une vraie authentification
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(USER_EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllTasksByProject_returnsTasksOfProject() throws Exception {
        UUID projectId = UUID.randomUUID();

        Task t1 = new Task();
        t1.setTaskId(UUID.randomUUID());
        t1.setTaskTitle("Task 1");

        Task t2 = new Task();
        t2.setTaskId(UUID.randomUUID());
        t2.setTaskTitle("Task 2");

        when(taskService.getAllTasksByProject(projectId, USER_EMAIL)).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].taskTitle").value("Task 1"))
                .andExpect(jsonPath("$[1].taskTitle").value("Task 2"));

        verify(taskService).getAllTasksByProject(projectId, USER_EMAIL);
    }

    @Test
    void getAllTasksByProject_returnsEmptyList_whenProjectHasNoTask() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(taskService.getAllTasksByProject(projectId, USER_EMAIL)).thenReturn(List.of());

        mockMvc.perform(get("/projects/{projectId}/tasks", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
