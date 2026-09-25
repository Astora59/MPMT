package com.projectManager.pmt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectManager.pmt.dto.AssignRoleRequest;
import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.dto.ProjectMemberResponse;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.ProjectService;
import org.junit.jupiter.api.AfterEach;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Les repositories sont injectés directement dans le contrôleur : ils doivent être mockés
// pour que le contexte Spring se charge, même si les endpoints testés ne les utilisent pas.
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    private static final String EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UsersRepository usersRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateViaSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getPrincipal()).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ------------------------------------------------------------------
    // POST /projects/create
    // ------------------------------------------------------------------

    @Test
    void createProject_returnsOk_withCreatedProject() throws Exception {
        authenticateViaSecurityContext();

        ProjectRequest request = new ProjectRequest();
        Project created = mock(Project.class);
        when(created.getProject_name()).thenReturn("Mon projet");
        when(projectService.createProject(any(ProjectRequest.class), eq(EMAIL))).thenReturn(created);

        mockMvc.perform(post("/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_name").value("Mon projet"));
    }

    @Test
    void createProject_returns500_whenServiceThrows() throws Exception {
        authenticateViaSecurityContext();
        when(projectService.createProject(any(ProjectRequest.class), eq(EMAIL)))
                .thenThrow(new RuntimeException("Utilisateur non trouvé"));

        mockMvc.perform(post("/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProjectRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Utilisateur non trouvé"));
    }

    // ------------------------------------------------------------------
    // POST /projects/{projectId}/invite
    // ------------------------------------------------------------------

    @Test
    void inviteUser_returnsOk_whenSuccessful() throws Exception {
        UUID projectId = UUID.randomUUID();
        InviteRequest request = new InviteRequest();

        mockMvc.perform(post("/projects/{projectId}/invite", projectId)
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur invité avec succès"));

        verify(projectService).inviteUserToProject(eq(projectId), eq(EMAIL), any(InviteRequest.class));
    }

    @Test
    void inviteUser_returns500_whenServiceThrows() throws Exception {
        UUID projectId = UUID.randomUUID();
        doThrow(new RuntimeException("Seul l'admin peut inviter des utilisateurs dans ce projet"))
                .when(projectService).inviteUserToProject(eq(projectId), eq(EMAIL), any(InviteRequest.class));

        mockMvc.perform(post("/projects/{projectId}/invite", projectId)
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InviteRequest())))
                .andExpect(status().isInternalServerError());
    }

    // ------------------------------------------------------------------
    // GET /projects
    // ------------------------------------------------------------------

    @Test
    void getAllProjects_returnsList() throws Exception {
        when(projectService.getAllProjects()).thenReturn(List.of(mock(Project.class), mock(Project.class)));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ------------------------------------------------------------------
    // PUT /projects/{projectId}/role
    // ------------------------------------------------------------------

    @Test
    void updateUserRole_returnsOk_whenSuccessful() throws Exception {
        UUID projectId = UUID.randomUUID();
        AssignRoleRequest request = new AssignRoleRequest();
        request.setEmail("target@example.com");

        mockMvc.perform(put("/projects/{projectId}/role", projectId)
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Rôle mis à jour avec succès pour target@example.com"));
    }

    @Test
    void updateUserRole_returns500_whenServiceThrows() throws Exception {
        UUID projectId = UUID.randomUUID();
        doThrow(new RuntimeException("Rôle introuvable"))
                .when(projectService).updateUserRole(eq(projectId), eq(EMAIL), any(AssignRoleRequest.class));

        mockMvc.perform(put("/projects/{projectId}/role", projectId)
                        .principal(() -> EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignRoleRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Rôle introuvable"));
    }

    // ------------------------------------------------------------------
    // GET /projects/me
    // ------------------------------------------------------------------

    @Test
    void getMyProjects_returnsProjectsOfCurrentUser() throws Exception {
        when(projectService.getProjectsForUser(EMAIL)).thenReturn(List.of(mock(Project.class)));

        mockMvc.perform(get("/projects/me").principal(() -> EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ------------------------------------------------------------------
    // GET /projects/{projectId}/members
    // ------------------------------------------------------------------

    @Test
    void getProjectMembers_returnsMembers_whenAllowed() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectMemberResponse member = new ProjectMemberResponse(UUID.randomUUID(), "Bob", "bob@example.com", "member");
        when(projectService.getProjectMembers(projectId, EMAIL)).thenReturn(List.of(member));

        mockMvc.perform(get("/projects/{projectId}/members", projectId).principal(() -> EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getProjectMembers_returnsForbidden_whenAccessDenied() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(projectService.getProjectMembers(projectId, EMAIL))
                .thenThrow(new RuntimeException("Accès refusé"));

        mockMvc.perform(get("/projects/{projectId}/members", projectId).principal(() -> EMAIL))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Accès refusé"));
    }
}
