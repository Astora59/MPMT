package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.AssignRoleRequest;
import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.dto.ProjectMemberResponse;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Role;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.RoleRepository;
import com.projectManager.pmt.repositories.UsersRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceImplementationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String TARGET_EMAIL = "target@example.com";

    @Mock private ProjectRepository projectRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UsersRepository usersRepository;

    @InjectMocks
    private ProjectServiceImplementation service;

    private UUID projectId;
    private UUID adminId;
    private Project project;
    private Users admin;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        project = mock(Project.class);
        when(project.getProject_id()).thenReturn(projectId);
        when(project.getProject_admin()).thenReturn(adminId);

        admin = mock(Users.class);
        when(admin.getUsers_id()).thenReturn(adminId);
        when(admin.getEmail()).thenReturn(ADMIN_EMAIL);
    }

    private static void assertFails(String expectedMessagePart, Executable action) {
        RuntimeException ex = assertThrows(RuntimeException.class, action);
        assertTrue(ex.getMessage().contains(expectedMessagePart), "Message reçu : " + ex.getMessage());
    }

    // ------------------------------------------------------------------
    // createProject
    // ------------------------------------------------------------------

    @Test
    void createProject_savesProjectAndCreatesAdminRole_whenNoRoleExists() {
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));
        when(roleRepository.findRoleByUserAndProject(eq(adminId), any())).thenReturn(Optional.empty());

        ProjectRequest request = mock(ProjectRequest.class);
        when(request.getProjectName()).thenReturn("Nouveau projet");
        when(request.getProjectDescription()).thenReturn("Description");

        Project result = service.createProject(request, ADMIN_EMAIL);

        assertEquals("Nouveau projet", result.getProject_name());
        assertEquals("Description", result.getProject_description());
        assertEquals(adminId, result.getProject_admin());
        assertEquals(ADMIN_EMAIL, result.getEmail());

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertEquals("admin", roleCaptor.getValue().getRoleName());
    }

    @Test
    void createProject_doesNotDuplicateAdminRole_whenRoleAlreadyExists() {
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));
        when(roleRepository.findRoleByUserAndProject(eq(adminId), any())).thenReturn(Optional.of(mock(Role.class)));

        ProjectRequest request = mock(ProjectRequest.class);

        service.createProject(request, ADMIN_EMAIL);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void createProject_throws_whenUserNotFound() {
        assertFails("Utilisateur non trouvé",
                () -> service.createProject(mock(ProjectRequest.class), ADMIN_EMAIL));
        verify(projectRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // inviteUserToProject
    // ------------------------------------------------------------------

    @Test
    void inviteUser_createsRole_withDefaultMemberRole_whenNoneSpecified() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        Users invited = mock(Users.class);
        when(usersRepository.findByEmail(TARGET_EMAIL)).thenReturn(Optional.of(invited));

        InviteRequest request = mock(InviteRequest.class);
        when(request.getEmail()).thenReturn(TARGET_EMAIL);
        when(request.getRoleName()).thenReturn(null);

        Project result = service.inviteUserToProject(projectId, ADMIN_EMAIL, request);

        assertSame(project, result);
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertEquals("member", roleCaptor.getValue().getRoleName());
    }

    @Test
    void inviteUser_usesRequestedRole_whenSpecified() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        Users invited = mock(Users.class);
        when(usersRepository.findByEmail(TARGET_EMAIL)).thenReturn(Optional.of(invited));

        InviteRequest request = mock(InviteRequest.class);
        when(request.getEmail()).thenReturn(TARGET_EMAIL);
        when(request.getRoleName()).thenReturn("observer");

        service.inviteUserToProject(projectId, ADMIN_EMAIL, request);

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        assertEquals("observer", roleCaptor.getValue().getRoleName());
    }

    @Test
    void inviteUser_throws_whenProjectNotFound() {
        assertFails("Projet non trouvé",
                () -> service.inviteUserToProject(projectId, ADMIN_EMAIL, mock(InviteRequest.class)));
    }

    @Test
    void inviteUser_throws_whenAdminNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertFails("Utilisateur non trouvé",
                () -> service.inviteUserToProject(projectId, ADMIN_EMAIL, mock(InviteRequest.class)));
    }

    @Test
    void inviteUser_throws_whenCallerIsNotAdmin() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Users notAdmin = mock(Users.class);
        when(notAdmin.getUsers_id()).thenReturn(UUID.randomUUID());
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(notAdmin));

        assertFails("Seul l",
                () -> service.inviteUserToProject(projectId, ADMIN_EMAIL, mock(InviteRequest.class)));
    }

    @Test
    void inviteUser_throws_whenInvitedUserNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

        InviteRequest request = mock(InviteRequest.class);
        when(request.getEmail()).thenReturn(TARGET_EMAIL);

        assertFails("Utilisateur invité non trouvé",
                () -> service.inviteUserToProject(projectId, ADMIN_EMAIL, request));
    }

    // ------------------------------------------------------------------
    // updateUserRole
    // ------------------------------------------------------------------

    @Test
    void updateUserRole_updatesExistingRole_whenCallerIsAdmin() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

        Role adminRole = mock(Role.class);
        when(adminRole.getRoleName()).thenReturn("admin");
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.of(adminRole));

        Users target = mock(Users.class);
        UUID targetId = UUID.randomUUID();
        when(target.getUsers_id()).thenReturn(targetId);
        when(usersRepository.findByEmail(TARGET_EMAIL)).thenReturn(Optional.of(target));

        Role existingTargetRole = mock(Role.class);
        when(roleRepository.findRoleByUserAndProject(targetId, projectId)).thenReturn(Optional.of(existingTargetRole));

        AssignRoleRequest request = mock(AssignRoleRequest.class);
        when(request.getEmail()).thenReturn(TARGET_EMAIL);
        when(request.getRoleName()).thenReturn("member");

        Project result = service.updateUserRole(projectId, ADMIN_EMAIL, request);

        assertSame(project, result);
        verify(existingTargetRole).setRoleName("member");
        verify(roleRepository).save(existingTargetRole);
    }

    @Test
    void updateUserRole_createsNewRole_whenTargetHasNoRoleYet() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));

        Role adminRole = mock(Role.class);
        when(adminRole.getRoleName()).thenReturn("admin");
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.of(adminRole));

        Users target = mock(Users.class);
        UUID targetId = UUID.randomUUID();
        when(target.getUsers_id()).thenReturn(targetId);
        when(usersRepository.findByEmail(TARGET_EMAIL)).thenReturn(Optional.of(target));
        when(roleRepository.findRoleByUserAndProject(targetId, projectId)).thenReturn(Optional.empty());

        AssignRoleRequest request = mock(AssignRoleRequest.class);
        when(request.getEmail()).thenReturn(TARGET_EMAIL);
        when(request.getRoleName()).thenReturn("observer");

        service.updateUserRole(projectId, ADMIN_EMAIL, request);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals("observer", captor.getValue().getRoleName());
    }

    @Test
    void updateUserRole_throws_whenProjectNotFound() {
        assertFails("Projet non trouvé",
                () -> service.updateUserRole(projectId, ADMIN_EMAIL, mock(AssignRoleRequest.class)));
    }

    @Test
    void updateUserRole_throws_whenAdminUserNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertFails("admin introuvable",
                () -> service.updateUserRole(projectId, ADMIN_EMAIL, mock(AssignRoleRequest.class)));
    }

    @Test
    void updateUserRole_throws_whenCallerHasNoRole() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.empty());

        assertFails("Rôle introuvable",
                () -> service.updateUserRole(projectId, ADMIN_EMAIL, mock(AssignRoleRequest.class)));
    }

    @Test
    void updateUserRole_throws_whenCallerIsNotAdmin() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        Role memberRole = mock(Role.class);
        when(memberRole.getRoleName()).thenReturn("member");
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.of(memberRole));

        assertFails("droits",
                () -> service.updateUserRole(projectId, ADMIN_EMAIL, mock(AssignRoleRequest.class)));
    }

    @Test
    void updateUserRole_throws_whenTargetUserNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        Role adminRole = mock(Role.class);
        when(adminRole.getRoleName()).thenReturn("admin");
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.of(adminRole));

        AssignRoleRequest request = mock(AssignRoleRequest.class);
        when(request.getEmail()).thenReturn(TARGET_EMAIL);

        assertFails("cible introuvable",
                () -> service.updateUserRole(projectId, ADMIN_EMAIL, request));
    }

    // ------------------------------------------------------------------
    // getAllProjects / getProjectsForUser
    // ------------------------------------------------------------------

    @Test
    void getAllProjects_returnsAllProjects() {
        List<Project> projects = List.of(mock(Project.class), mock(Project.class));
        when(projectRepository.findAll()).thenReturn(projects);

        assertSame(projects, service.getAllProjects());
    }

    @Test
    void getProjectsForUser_returnsProjectsOfUser() {
        List<Project> projects = List.of(mock(Project.class));
        when(projectRepository.findProjectsByUserEmail(ADMIN_EMAIL)).thenReturn(projects);

        assertSame(projects, service.getProjectsForUser(ADMIN_EMAIL));
    }

    // ------------------------------------------------------------------
    // getProjectMembers
    // ------------------------------------------------------------------

    @Test
    void getProjectMembers_returnsMembers_whenUserBelongsToProject() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.of(mock(Role.class)));

        Role memberRole = mock(Role.class);
        Users member = mock(Users.class);
        UUID memberId = UUID.randomUUID();
        when(member.getUsers_id()).thenReturn(memberId);
        when(member.getUsername()).thenReturn("Bob");
        when(member.getEmail()).thenReturn("bob@example.com");
        when(memberRole.getUser()).thenReturn(member);
        when(memberRole.getRoleName()).thenReturn("member");
        when(roleRepository.findRolesByProjectId(projectId)).thenReturn(List.of(memberRole));

        List<ProjectMemberResponse> result = service.getProjectMembers(projectId, ADMIN_EMAIL);

        assertEquals(1, result.size());
    }

    @Test
    void getProjectMembers_throws_whenProjectNotFound() {
        assertFails("Projet introuvable", () -> service.getProjectMembers(projectId, ADMIN_EMAIL));
    }

    @Test
    void getProjectMembers_throws_whenUserNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertFails("Utilisateur introuvable", () -> service.getProjectMembers(projectId, ADMIN_EMAIL));
    }

    @Test
    void getProjectMembers_throws_whenUserHasNoRoleInProject() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(usersRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(roleRepository.findRoleByUserAndProject(adminId, projectId)).thenReturn(Optional.empty());

        assertFails("Accès refusé", () -> service.getProjectMembers(projectId, ADMIN_EMAIL));
    }
}
