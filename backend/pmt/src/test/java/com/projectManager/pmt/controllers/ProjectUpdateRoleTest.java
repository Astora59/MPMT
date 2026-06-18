package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.dto.AssignRoleRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 désactive la sécurité pour les tests
public class ProjectUpdateRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private UsersRepository usersRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUpdateUserRoleSuccess() throws Exception {
        // 🧱 Données de test
        UUID projectId = UUID.randomUUID();
        AssignRoleRequest request = new AssignRoleRequest();
        request.setEmail("user2@example.com");
        request.setRoleName("observer");

        Project mockProject = new Project();
        mockProject.setProject_id(projectId);
        mockProject.setProject_name("Projet Test");

        // 🎭 Simuler le comportement du service
        Mockito.when(projectService.updateUserRole(
                Mockito.eq(projectId),
                Mockito.eq("admin@example.com"),
                Mockito.any(AssignRoleRequest.class))
        ).thenReturn(mockProject);

        // 📤 Exécution
        mockMvc.perform(put("/projects/{projectId}/role", projectId)
                        .principal(() -> "admin@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Rôle mis à jour avec succès pour " + request.getEmail()));
    }

    @Test
    void testUpdateUserRoleProjectNotFound() throws Exception {
        // 🧱 Données
        UUID projectId = UUID.randomUUID();
        AssignRoleRequest request = new AssignRoleRequest();
        request.setEmail("user2@example.com");
        request.setRoleName("observer");

        // 💥 Simuler une erreur
        Mockito.when(projectService.updateUserRole(
                Mockito.eq(projectId),
                Mockito.eq("admin@example.com"),
                Mockito.any(AssignRoleRequest.class))
        ).thenThrow(new RuntimeException("Projet non trouvé"));

        // 📤 Exécution
        mockMvc.perform(put("/projects/{projectId}/role", projectId)
                        .principal(() -> "admin@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
