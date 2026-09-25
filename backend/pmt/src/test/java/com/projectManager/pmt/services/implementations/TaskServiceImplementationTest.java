package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.dto.TaskUpdateRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Role;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.RoleRepository;
import com.projectManager.pmt.repositories.TaskHistoryRepository;
import com.projectManager.pmt.repositories.TaskRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceImplementationTest {

    private static final String EMAIL = "user@example.com";
    private static final String TARGET_EMAIL = "target@example.com";

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UsersRepository usersRepository;
    @Mock private EmailService emailService;
    @Mock private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskServiceImplementation service;

    private UUID projectId;
    private UUID taskId;
    private UUID userId;
    private Project project;
    private Users user;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        project = mock(Project.class);
        when(project.getProject_id()).thenReturn(projectId);
        when(project.getProject_name()).thenReturn("Projet PMT");
        // Par défaut, l'admin du projet n'est PAS l'utilisateur testé
        when(project.getProject_admin()).thenReturn(UUID.randomUUID());

        user = mock(Users.class);
        when(user.getUsers_id()).thenReturn(userId);
        when(user.getEmail()).thenReturn(EMAIL);
        when(user.getUsername()).thenReturn("user");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void projectExists() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
    }

    private void userExists() {
        when(usersRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void userIsProjectAdmin() {
        when(project.getProject_admin()).thenReturn(userId);
    }

    private void roleIs(String roleName) {
        Role role = mock(Role.class);
        when(role.getRoleName()).thenReturn(roleName);
        when(roleRepository.findRoleByUserAndProject(userId, projectId)).thenReturn(Optional.of(role));
    }

    private Users targetExists(UUID targetId) {
        Users target = mock(Users.class);
        when(target.getUsers_id()).thenReturn(targetId);
        when(target.getEmail()).thenReturn(TARGET_EMAIL);
        when(usersRepository.findByEmail(TARGET_EMAIL)).thenReturn(Optional.of(target));
        return target;
    }

    private Task taskOfProject(UUID ownerProjectId) {
        Project owner = mock(Project.class);
        when(owner.getProject_id()).thenReturn(ownerProjectId);
        Task task = new Task();
        task.setTaskId(taskId);
        task.setProject(owner);
        return task;
    }

    private static void assertFails(String expectedMessagePart, Executable action) {
        RuntimeException ex = assertThrows(RuntimeException.class, action);
        assertTrue(ex.getMessage().contains(expectedMessagePart), "Message reçu : " + ex.getMessage());
    }

    // ------------------------------------------------------------------
    // createTask
    // ------------------------------------------------------------------

    @Test
    void createTask_savesTask_whenProjectAndUserExist() {
        projectExists();
        userExists();
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskCreationRequest request = mock(TaskCreationRequest.class);
        when(request.getTaskTitle()).thenReturn("Titre");
        when(request.getTaskDescription()).thenReturn("Description");
        when(request.getTaskStatus()).thenReturn("TODO");
        when(request.getTaskPriority()).thenReturn("HIGH");

        Task result = service.createTask(projectId, EMAIL, request);

        assertEquals("Titre", result.getTaskTitle());
        assertEquals("Description", result.getTaskDescription());
        assertEquals("TODO", result.getTaskStatus());
        assertEquals("HIGH", result.getTaskPriority());
        assertSame(project, result.getProject());
        assertSame(user, result.getCreatedBy());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_throws_whenProjectNotFound() {
        assertFails("Projet introuvable",
                () -> service.createTask(projectId, EMAIL, mock(TaskCreationRequest.class)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_throws_whenUserNotFound() {
        projectExists();

        assertFails("Utilisateur introuvable",
                () -> service.createTask(projectId, EMAIL, mock(TaskCreationRequest.class)));
    }

    // ------------------------------------------------------------------
    // assignTaskToUser
    // ------------------------------------------------------------------

    @Test
    void assignTask_assignsUserAndSendsEmail() {
        projectExists();
        userExists();
        UUID targetId = UUID.randomUUID();
        Users target = targetExists(targetId);
        roleIs("admin");
        when(roleRepository.findRoleByUserAndProject(targetId, projectId))
                .thenReturn(Optional.of(mock(Role.class)));
        Task task = taskOfProject(projectId);
        task.setTaskTitle("Ma tâche");
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL);

        assertSame(target, result.getAssignedUser());
        verify(emailService).sendTaskAssignedEmail(eq(TARGET_EMAIL), eq("Ma tâche"), eq("Projet PMT"), any());
        verify(taskRepository).save(task);
    }

    @Test
    void assignTask_throws_whenProjectNotFound() {
        assertFails("Projet introuvable",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenCurrentUserNotFound() {
        projectExists();

        assertFails("Utilisateur actuel introuvable",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenTargetUserNotFound() {
        projectExists();
        userExists();

        assertFails("Utilisateur cible introuvable",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenCurrentUserHasNoRole() {
        projectExists();
        userExists();
        targetExists(UUID.randomUUID());

        assertFails("Accès refusé",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenCurrentUserIsObserver() {
        projectExists();
        userExists();
        targetExists(UUID.randomUUID());
        roleIs("observer");

        assertFails("Accès refusé",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenTargetIsNotMember() {
        projectExists();
        userExists();
        targetExists(UUID.randomUUID());
        roleIs("member");

        assertFails("pas membre du projet",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenTaskNotFound() {
        projectExists();
        userExists();
        UUID targetId = UUID.randomUUID();
        targetExists(targetId);
        roleIs("member");
        when(roleRepository.findRoleByUserAndProject(targetId, projectId))
                .thenReturn(Optional.of(mock(Role.class)));

        assertFails("Tâche introuvable",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
    }

    @Test
    void assignTask_throws_whenTaskBelongsToAnotherProject() {
        projectExists();
        userExists();
        UUID targetId = UUID.randomUUID();
        targetExists(targetId);
        roleIs("member");
        when(roleRepository.findRoleByUserAndProject(targetId, projectId))
                .thenReturn(Optional.of(mock(Role.class)));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOfProject(UUID.randomUUID())));

        assertFails("n'appartient pas",
                () -> service.assignTaskToUser(projectId, taskId, EMAIL, TARGET_EMAIL));
        verify(emailService, never()).sendTaskAssignedEmail(any(), any(), any(), any());
    }

    // ------------------------------------------------------------------
    // updateTask
    // ------------------------------------------------------------------

    @Test
    void updateTask_updatesFieldsAndWritesHistory_whenUserIsAdmin() {
        projectExists();
        userExists();
        userIsProjectAdmin();

        Task task = taskOfProject(projectId);
        task.setTaskTitle("Ancien titre");
        task.setTaskDescription("Ancienne description");
        task.setTaskStatus("todo");
        task.setTaskPriority("low");
        task.setTaskDeadline(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime newDeadline = LocalDateTime.of(2026, 2, 2, 12, 0);
        TaskUpdateRequest request = mock(TaskUpdateRequest.class);
        when(request.getTaskTitle()).thenReturn("Nouveau titre");
        when(request.getTaskDescription()).thenReturn("Nouvelle description");
        when(request.getTaskStatus()).thenReturn("done");
        when(request.getTaskPriority()).thenReturn("high");
        when(request.getTaskDeadline()).thenReturn(newDeadline);

        Task result = service.updateTask(projectId, taskId, EMAIL, request);

        assertEquals("Nouveau titre", result.getTaskTitle());
        assertEquals("Nouvelle description", result.getTaskDescription());
        assertEquals("done", result.getTaskStatus());
        assertEquals("high", result.getTaskPriority());
        assertEquals(newDeadline, result.getTaskDeadline());

        ArgumentCaptor<TaskHistory> captor = ArgumentCaptor.forClass(TaskHistory.class);
        verify(taskHistoryRepository).save(captor.capture());
        TaskHistory history = captor.getValue();
        assertEquals("TODO", history.getOldStatus());
        assertEquals("DONE", history.getNewStatus());
        assertEquals("LOW", history.getOldPriority());
        assertEquals("HIGH", history.getNewPriority());
        assertEquals("Ancien titre", history.getOldTitle());
        assertEquals("Nouveau titre", history.getNewTitle());
        assertSame(user, history.getModifiedBy());
    }

    @Test
    void updateTask_keepsExistingValues_whenRequestFieldsAreNull() {
        projectExists();
        userExists();
        userIsProjectAdmin();

        Task task = taskOfProject(projectId);
        task.setTaskTitle("Titre");
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        // Un mock sans stubbing renvoie null pour tous les getters
        Task result = service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class));

        assertEquals("Titre", result.getTaskTitle());
        assertNull(result.getTaskStatus());
        verify(taskHistoryRepository).save(any(TaskHistory.class));
    }

    @Test
    void updateTask_allowed_whenUserIsMember() {
        projectExists();
        userExists();
        roleIs("member");
        Task task = taskOfProject(projectId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class));

        assertSame(task, result);
    }

    @Test
    void updateTask_throws_whenUserIsObserver() {
        projectExists();
        userExists();
        roleIs("observer");

        assertFails("Accès refusé",
                () -> service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_throws_whenProjectNotFound() {
        assertFails("Projet introuvable",
                () -> service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class)));
    }

    @Test
    void updateTask_throws_whenUserNotFound() {
        projectExists();

        assertFails("Utilisateur introuvable",
                () -> service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class)));
    }

    @Test
    void updateTask_throws_whenTaskNotFound() {
        projectExists();
        userExists();
        userIsProjectAdmin();

        assertFails("Tâche introuvable",
                () -> service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class)));
    }

    @Test
    void updateTask_throws_whenTaskBelongsToAnotherProject() {
        projectExists();
        userExists();
        userIsProjectAdmin();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOfProject(UUID.randomUUID())));

        assertFails("n'appartient pas",
                () -> service.updateTask(projectId, taskId, EMAIL, mock(TaskUpdateRequest.class)));
    }

    // ------------------------------------------------------------------
    // getTaskById
    // ------------------------------------------------------------------

    @Test
    void getTaskById_returnsTask_whenUserIsAdmin() {
        projectExists();
        userExists();
        userIsProjectAdmin();
        Task task = taskOfProject(projectId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertSame(task, service.getTaskById(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskById_returnsTask_whenUserIsObserver() {
        projectExists();
        userExists();
        roleIs("Observer");
        Task task = taskOfProject(projectId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertSame(task, service.getTaskById(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskById_throws_whenRoleIsUnknown() {
        projectExists();
        userExists();
        roleIs("guest");

        assertFails("Accès refusé", () -> service.getTaskById(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskById_throws_whenProjectNotFound() {
        assertFails("Projet introuvable", () -> service.getTaskById(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskById_throws_whenUserNotFound() {
        projectExists();

        assertFails("Utilisateur introuvable", () -> service.getTaskById(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskById_throws_whenTaskNotFound() {
        projectExists();
        userExists();
        userIsProjectAdmin();

        assertFails("Tâche introuvable", () -> service.getTaskById(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskById_throws_whenTaskBelongsToAnotherProject() {
        projectExists();
        userExists();
        userIsProjectAdmin();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOfProject(UUID.randomUUID())));

        assertFails("n'appartient pas", () -> service.getTaskById(projectId, taskId, EMAIL));
    }

    // ------------------------------------------------------------------
    // getTasksByStatus
    // ------------------------------------------------------------------

    @Test
    void getTasksByStatus_returnsTasks_whenUserHasRole() {
        projectExists();
        userExists();
        roleIs("member");
        List<Task> tasks = List.of(new Task());
        when(taskRepository.findByProjectAndStatusIgnoreCase(projectId, "TODO")).thenReturn(tasks);

        assertSame(tasks, service.getTasksByStatus(projectId, EMAIL, "TODO"));
    }

    @Test
    void getTasksByStatus_returnsTasks_whenUserIsAdminWithoutRole() {
        projectExists();
        userExists();
        userIsProjectAdmin();
        List<Task> tasks = List.of(new Task());
        when(taskRepository.findByProjectAndStatusIgnoreCase(projectId, "DONE")).thenReturn(tasks);

        assertSame(tasks, service.getTasksByStatus(projectId, EMAIL, "DONE"));
    }

    @Test
    void getTasksByStatus_throws_whenNoAccess() {
        projectExists();
        userExists();

        assertFails("Accès refusé", () -> service.getTasksByStatus(projectId, EMAIL, "TODO"));
    }

    @Test
    void getTasksByStatus_throws_whenProjectNotFound() {
        assertFails("Projet introuvable", () -> service.getTasksByStatus(projectId, EMAIL, "TODO"));
    }

    @Test
    void getTasksByStatus_throws_whenUserNotFound() {
        projectExists();

        assertFails("Utilisateur introuvable", () -> service.getTasksByStatus(projectId, EMAIL, "TODO"));
    }

    // ------------------------------------------------------------------
    // getTaskHistory
    // ------------------------------------------------------------------

    @Test
    void getTaskHistory_returnsHistory_whenUserIsMember() {
        userExists();
        projectExists();
        roleIs("member");
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOfProject(projectId)));
        List<TaskHistory> history = List.of(new TaskHistory());
        when(taskHistoryRepository.findByTask_TaskId(taskId)).thenReturn(history);

        assertSame(history, service.getTaskHistory(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskHistory_throws_whenUserNotFound() {
        assertFails("Utilisateur introuvable", () -> service.getTaskHistory(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskHistory_throws_whenProjectNotFound() {
        userExists();

        assertFails("Projet introuvable", () -> service.getTaskHistory(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskHistory_throws_whenTaskNotFound() {
        userExists();
        projectExists();

        assertFails("Tâche introuvable", () -> service.getTaskHistory(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskHistory_throws_whenTaskBelongsToAnotherProject() {
        userExists();
        projectExists();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOfProject(UUID.randomUUID())));

        assertFails("n'appartient pas", () -> service.getTaskHistory(projectId, taskId, EMAIL));
    }

    @Test
    void getTaskHistory_throws_whenUserIsNotMember() {
        userExists();
        projectExists();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskOfProject(projectId)));

        assertFails("Accès refusé", () -> service.getTaskHistory(projectId, taskId, EMAIL));
    }

    // ------------------------------------------------------------------
    // getAllTasksByProject
    // ------------------------------------------------------------------

    @Test
    void getAllTasksByProject_returnsTasks_whenUserIsMember() {
        projectExists();
        userExists();
        roleIs("member");
        List<Task> tasks = List.of(new Task(), new Task());
        when(taskRepository.findByProject(project)).thenReturn(tasks);

        assertSame(tasks, service.getAllTasksByProject(projectId, EMAIL));
    }

    @Test
    void getAllTasksByProject_throws_whenProjectNotFound() {
        assertFails("Projet introuvable", () -> service.getAllTasksByProject(projectId, EMAIL));
    }

    @Test
    void getAllTasksByProject_throws_whenUserNotFound() {
        projectExists();

        assertThrows(NoSuchElementException.class, () -> service.getAllTasksByProject(projectId, EMAIL));
    }

    @Test
    void getAllTasksByProject_throws_whenUserIsNotMember() {
        projectExists();
        userExists();

        assertFails("Accès refusé", () -> service.getAllTasksByProject(projectId, EMAIL));
    }
}
