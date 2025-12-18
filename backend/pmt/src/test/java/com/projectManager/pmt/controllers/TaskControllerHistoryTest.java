package com.projectManager.pmt.controllers;

import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void getTaskHistory_success() throws Exception {

        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        // 🧪 Création d’un faux historique
        TaskHistory history = new TaskHistory();
        history.setHistoryId(UUID.randomUUID());
        history.setOldStatus("TODO");
        history.setNewStatus("IN_PROGRESS");
        history.setOldPriority("LOW");
        history.setNewPriority("HIGH");
        history.setModificationDate(LocalDateTime.now());
        history.setChangeDescription("Changement manuel de statut");

        List<TaskHistory> historyList = List.of(history);

        // 🧙 Mock du service
        when(taskService.getTaskHistory(
                eq(projectId),
                eq(taskId),
                eq("current@example.com")
        )).thenReturn(historyList);

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}/history", projectId, taskId)
                        .principal(() -> "current@example.com") // utilisateur authentifié
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].oldStatus").value("TODO"))
                .andExpect(jsonPath("$[0].newStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].oldPriority").value("LOW"))
                .andExpect(jsonPath("$[0].newPriority").value("HIGH"))
                .andExpect(jsonPath("$[0].changeDescription").value("Changement manuel de statut"));
    }
}
