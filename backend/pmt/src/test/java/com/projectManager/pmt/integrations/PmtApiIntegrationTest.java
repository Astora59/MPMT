package com.projectManager.pmt.integrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.projectManager.pmt.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de l'API : chaque requête traverse toute la chaîne
 * (filtre JWT, contrôleur, service, repository, base H2 en mémoire).
 * Seuls les envois d'e-mails sont remplacés par des mocks.
 *
 * Chaque test crée ses propres utilisateurs avec des adresses uniques :
 * les tests sont donc indépendants les uns des autres.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PmtApiIntegrationTest {

    private static final String AUTH = "Authorization";
    private static final String PASSWORD = "Password123!";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private EmailService emailService;
    @MockitoBean private JavaMailSender mailSender;

    private record TestUser(String email, String token) {}

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    /** Adresse courte et unique (la colonne e-mail du projet est limitée à 50 caractères). */
    private static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@test.fr";
    }

    private void register(String username, String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "email", email, "password", PASSWORD))))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private TestUser registerAndLogin(String prefix) throws Exception {
        String email = uniqueEmail(prefix);
        register(prefix, email);
        return new TestUser(email, login(email));
    }

    private UUID createProject(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/projects/create")
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("projectName", name, "projectDescription", "Description"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(body, "$.project_id"));
    }

    private void invite(String adminToken, UUID projectId, String email, String roleName) throws Exception {
        mockMvc.perform(post("/projects/{projectId}/invite", projectId)
                        .header(AUTH, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "roleName", roleName))))
                .andExpect(status().isOk());
    }

    private void createTask(String token, UUID projectId, String title) throws Exception {
        mockMvc.perform(post("/projects/{projectId}/tasks", projectId)
                        .header(AUTH, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "taskTitle", title,
                                "taskDescription", "Description",
                                "taskStatus", "TODO",
                                "taskPriority", "HIGH",
                                "taskDeadline", "2026-12-31T10:00:00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskTitle").value(title))
                .andExpect(jsonPath("$.taskStatus").value("TODO"));
    }

    private UUID findTaskId(String token, UUID projectId, String title) throws Exception {
        String body = mockMvc.perform(get("/projects/{projectId}/tasks", projectId)
                        .header(AUTH, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(body, "$[?(@.taskTitle=='" + title + "')].taskId");
        return UUID.fromString(ids.get(0));
    }

    // ------------------------------------------------------------------
    // Authentification
    // ------------------------------------------------------------------

    @Test
    void register_thenLogin_returnsAToken() throws Exception {
        String email = uniqueEmail("alice");
        register("alice", email);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(not(emptyOrNullString())))
                .andExpect(jsonPath("$.users_id").exists());
    }

    @Test
    void login_returns401_whenPasswordIsWrong() throws Exception {
        String email = uniqueEmail("alice");
        register("alice", email);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "mauvais-mot-de-passe"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns401_whenUserIsUnknown() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", uniqueEmail("ghost"), "password", PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_isRejected_withoutToken() throws Exception {
        mockMvc.perform(get("/projects/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void protectedEndpoint_isRejected_withInvalidToken() throws Exception {
        mockMvc.perform(get("/projects/me").header(AUTH, bearer("ceci-n-est-pas-un-jwt")))
                .andExpect(status().is4xxClientError());
    }

    // ------------------------------------------------------------------
    // Projets et membres
    // ------------------------------------------------------------------

    @Test
    void createProject_returnsTheProject_andItAppearsInTheProjectList() throws Exception {
        TestUser admin = registerAndLogin("admin");

        mockMvc.perform(post("/projects/create")
                        .header(AUTH, bearer(admin.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("projectName", "Projet Alpha", "projectDescription", "Description"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project_name").value("Projet Alpha"))
                .andExpect(jsonPath("$.project_id").exists());

        mockMvc.perform(get("/projects").header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].project_name", hasItem("Projet Alpha")));
    }

    @Test
    void inviteMember_thenMemberSeesTheProject_andIsListedAmongMembers() throws Exception {
        TestUser admin = registerAndLogin("admin");
        TestUser member = registerAndLogin("member");
        UUID projectId = createProject(admin.token(), "Projet Beta");

        invite(admin.token(), projectId, member.email(), "member");

        mockMvc.perform(get("/projects/me").header(AUTH, bearer(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].project_name", hasItem("Projet Beta")));

        mockMvc.perform(get("/projects/{projectId}/members", projectId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(member.email())));
    }

    @Test
    void updateRole_changesTheRoleOfAMember() throws Exception {
        TestUser admin = registerAndLogin("admin");
        TestUser member = registerAndLogin("member");
        UUID projectId = createProject(admin.token(), "Projet Gamma");
        invite(admin.token(), projectId, member.email(), "member");

        mockMvc.perform(put("/projects/{projectId}/role", projectId)
                        .header(AUTH, bearer(admin.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", member.email(), "roleName", "observer"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects/{projectId}/members", projectId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='" + member.email() + "')].roleName", hasItem("observer")));
    }

    @Test
    void invite_fails_whenTheInvitedUserDoesNotExist() throws Exception {
        TestUser admin = registerAndLogin("admin");
        UUID projectId = createProject(admin.token(), "Projet Delta");

        mockMvc.perform(post("/projects/{projectId}/invite", projectId)
                        .header(AUTH, bearer(admin.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", uniqueEmail("ghost"), "roleName", "member"))))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void members_areForbidden_forSomeoneWhoIsNotInTheProject() throws Exception {
        TestUser admin = registerAndLogin("admin");
        TestUser outsider = registerAndLogin("outsider");
        UUID projectId = createProject(admin.token(), "Projet prive");

        mockMvc.perform(get("/projects/{projectId}/members", projectId)
                        .header(AUTH, bearer(outsider.token())))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // Tâches
    // ------------------------------------------------------------------

    @Test
    void taskLifecycle_create_read_update_history_andFilterByStatus() throws Exception {
        TestUser admin = registerAndLogin("admin");
        UUID projectId = createProject(admin.token(), "Projet Taches");

        // Création
        createTask(admin.token(), projectId, "Tache initiale");

        // Lecture de la liste puis d'une tâche seule
        mockMvc.perform(get("/projects/{projectId}/tasks", projectId).header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].taskTitle", hasItem("Tache initiale")));

        UUID taskId = findTaskId(admin.token(), projectId, "Tache initiale");

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskTitle").value("Tache initiale"))
                .andExpect(jsonPath("$.taskStatus").value("TODO"));

        // Filtre par statut (insensible à la casse) avant modification
        mockMvc.perform(get("/projects/{projectId}/tasks/status/todo", projectId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].taskTitle", hasItem("Tache initiale")));

        // Mise à jour
        mockMvc.perform(put("/projects/{projectId}/tasks/{taskId}", projectId, taskId)
                        .header(AUTH, bearer(admin.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "taskTitle", "Tache terminee",
                                "taskDescription", "Nouvelle description",
                                "taskStatus", "DONE",
                                "taskPriority", "LOW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskTitle").value("Tache terminee"))
                .andExpect(jsonPath("$.taskStatus").value("DONE"));

        // Historique des modifications
        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}/history", projectId, taskId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].oldStatus").value("TODO"))
                .andExpect(jsonPath("$[0].newStatus").value("DONE"))
                .andExpect(jsonPath("$[0].oldTitle").value("Tache initiale"))
                .andExpect(jsonPath("$[0].newTitle").value("Tache terminee"));

        // Filtre par statut après modification : la tâche a changé de colonne
        mockMvc.perform(get("/projects/{projectId}/tasks/status/done", projectId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].taskTitle", hasItem("Tache terminee")));

        mockMvc.perform(get("/projects/{projectId}/tasks/status/todo", projectId)
                        .header(AUTH, bearer(admin.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].taskTitle", not(hasItem("Tache terminee"))));
    }

    @Test
    void member_canAssignATask_andTheAssigneeIsNotifiedByEmail() throws Exception {
        TestUser admin = registerAndLogin("admin");
        TestUser member = registerAndLogin("member");
        UUID projectId = createProject(admin.token(), "Projet Assignation");
        invite(admin.token(), projectId, member.email(), "member");
        createTask(admin.token(), projectId, "Tache a assigner");
        UUID taskId = findTaskId(admin.token(), projectId, "Tache a assigner");

        mockMvc.perform(put("/projects/{projectId}/tasks/{taskId}/assign", projectId, taskId)
                        .header(AUTH, bearer(member.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userEmail", member.email()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUser.email").value(member.email()));

        verify(emailService).sendTaskAssignedEmail(eq(member.email()), any(), any(), any());
    }

    @Test
    void createTask_isRejected_withoutToken() throws Exception {
        TestUser admin = registerAndLogin("admin");
        UUID projectId = createProject(admin.token(), "Projet Securise");

        mockMvc.perform(post("/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("taskTitle", "Intrus", "taskStatus", "TODO"))))
                .andExpect(status().is4xxClientError());
    }
}
