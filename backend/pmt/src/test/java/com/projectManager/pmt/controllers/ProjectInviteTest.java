package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 désactive Spring Security pour ce test
public class ProjectInviteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ Cas 1 : Invitation réussie
    @Test
    void testInviteUserSuccess() throws Exception {
        // 🧱 Données de test
        UUID projectId = UUID.randomUUID();
        InviteRequest inviteRequest = new InviteRequest();
        inviteRequest.setEmail("user2@example.com");
        inviteRequest.setRoleName("member");

        Project mockProject = new Project();
        mockProject.setProject_id(projectId);
        mockProject.setProject_name("Projet Test");

        // 🎭 Simule un comportement "réussi"
        Mockito.when(projectService.inviteUserToProject(
                Mockito.eq(projectId),
                Mockito.anyString(), // ✅ on accepte n’importe quel principal
                Mockito.any(InviteRequest.class))
        ).thenReturn(mockProject);

        // 📤 Exécution
        mockMvc.perform(post("/projects/{projectId}/invite", projectId)
                        .principal(() -> "admin@example.com") // 🧠 Simule un utilisateur connecté
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur invité avec succès"));
    }

    // ✅ Cas 2 : Projet non trouvé → erreur 500
    @Test
    void testInviteUserProjectNotFound() throws Exception {
        // 🧱 Données
        UUID projectId = UUID.randomUUID();
        InviteRequest inviteRequest = new InviteRequest();
        inviteRequest.setEmail("user2@example.com");
        inviteRequest.setRoleName("member");

        // 💥 Simule une erreur "Projet non trouvé"
        Mockito.when(projectService.inviteUserToProject(
                Mockito.eq(projectId),
                Mockito.anyString(), // ✅ ici aussi on accepte n’importe quel principal
                Mockito.any(InviteRequest.class))
        ).thenThrow(new RuntimeException("Projet non trouvé"));

        // 📤 Exécution
        mockMvc.perform(post("/projects/{projectId}/invite", projectId)
                        .principal(() -> "admin@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isInternalServerError());
    }
}

