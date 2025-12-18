package com.projectManager.pmt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.controllers.ProjectController;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateProjectSuccess() throws Exception {
        // Simuler l'utilisateur connecté
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn("admin@example.com");
        SecurityContextHolder.setContext(securityContext);

        // Simuler la requête envoyée au contrôleur
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setProjectName("Test Project");
        projectRequest.setProjectDescription("Un projet de test");

        // Simuler le résultat retourné par le service
        Project mockProject = new Project();
        mockProject.setProject_id(UUID.randomUUID());
        mockProject.setProject_name("Test Project");
        mockProject.setProject_description("Un projet de test");
        mockProject.setProject_creation_date(LocalDateTime.now());
        mockProject.setEmail("admin@example.com");

        Mockito.when(projectService.createProject(any(ProjectRequest.class), eq("admin@example.com")))
                .thenReturn(mockProject);

        // Effectuer la requête POST
        mockMvc.perform(post("/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_name").value("Test Project"))
                .andExpect(jsonPath("$.project_description").value("Un projet de test"))
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void testCreateProjectUserNotFound() throws Exception {
        // Simuler l'utilisateur connecté
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn("notfound@example.com");
        SecurityContextHolder.setContext(securityContext);

        // Simuler le ProjectRequest
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setProjectName("Projet inexistant");
        projectRequest.setProjectDescription("Ceci ne devrait pas marcher");

        // Simuler l'erreur renvoyée par le service
        Mockito.when(projectService.createProject(any(ProjectRequest.class), eq("notfound@example.com")))
                .thenThrow(new RuntimeException("Utilisateur non trouvé avec l’email : notfound@example.com"));

        // Effectuer la requête POST
        mockMvc.perform(post("/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andExpect(status().isInternalServerError());
    }
}
