package com.projectManager.pmt.controllers;

import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void getTaskHistory_success() throws Exception {

        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getPrincipal()).thenReturn("current@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        TaskHistory history = new TaskHistory();
        history.setOldStatus("pending");
        history.setNewStatus("IN_PROGRESS");
        history.setOldPriority("LOW");
        history.setNewPriority("HIGH");
        history.setModificationDate(LocalDateTime.now());
        history.setChangeDescription("Changement manuel de statut");

        when(taskService.getTaskHistory(
                eq(projectId),
                eq(taskId),
                eq("current@example.com")
        )).thenReturn(List.of(history));

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}/history", projectId, taskId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].oldStatus").value("TODO"))
                .andExpect(jsonPath("$[0].newStatus").value("IN_PROGRESS"));
    }
}