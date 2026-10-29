package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Role;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.models.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration de la couche persistance : les entités JPA et les requêtes
 * des repositories sont exécutées contre une vraie base (H2 en mémoire).
 * Chaque test est exécuté dans une transaction annulée à la fin.
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoriesIntegrationTest {

    @Autowired private UsersRepository usersRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private TaskHistoryRepository taskHistoryRepository;

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Users saveUser(String email) {
        Users user = new Users();
        user.setUsername(email.substring(0, email.indexOf('@')));
        user.setEmail(email);
        user.setPasswordHash("hash");
        return usersRepository.saveAndFlush(user);
    }

    private Project saveProject(String name, Users admin) {
        Project project = new Project();
        project.setProject_name(name);
        project.setProject_description("Description de " + name);
        project.setProject_admin(admin.getUsers_id());
        project.setEmail(admin.getEmail());
        return projectRepository.saveAndFlush(project);
    }

    private Role saveRole(Users user, Project project, String roleName) {
        Role role = new Role();
        role.setUser(user);
        role.setProject(project);
        role.setRoleName(roleName);
        return roleRepository.saveAndFlush(role);
    }

    private Task saveTask(Project project, Users creator, String title, String status) {
        Task task = new Task();
        task.setProject(project);
        task.setCreatedBy(creator);
        task.setTaskTitle(title);
        task.setTaskStatus(status);
        task.setTaskPriority("HIGH");
        return taskRepository.saveAndFlush(task);
    }

    private TaskHistory saveHistory(Task task, Users author, String newStatus) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setModifiedBy(author);
        history.setOldStatus("TODO");
        history.setNewStatus(newStatus);
        return taskHistoryRepository.saveAndFlush(history);
    }

    // ------------------------------------------------------------------
    // UsersRepository
    // ------------------------------------------------------------------

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        Users saved = saveUser("alice@test.fr");

        Optional<Users> found = usersRepository.findByEmail("alice@test.fr");

        assertThat(found).isPresent();
        assertThat(found.get().getUsers_id()).isEqualTo(saved.getUsers_id());
        assertThat(found.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailIsUnknown() {
        saveUser("alice@test.fr");

        assertThat(usersRepository.findByEmail("inconnu@test.fr")).isEmpty();
    }

    // ------------------------------------------------------------------
    // ProjectRepository
    // ------------------------------------------------------------------

    @Test
    void findProjectsByUserEmail_returnsOnlyProjectsWhereUserHasARole() {
        Users alice = saveUser("alice@test.fr");
        Users bob = saveUser("bob@test.fr");
        Project shared = saveProject("Projet partage", alice);
        Project privateProject = saveProject("Projet prive", alice);
        saveRole(alice, shared, "admin");
        saveRole(alice, privateProject, "admin");
        saveRole(bob, shared, "member");

        List<Project> bobProjects = projectRepository.findProjectsByUserEmail("bob@test.fr");
        List<Project> aliceProjects = projectRepository.findProjectsByUserEmail("alice@test.fr");

        assertThat(bobProjects).extracting(Project::getProject_name).containsExactly("Projet partage");
        assertThat(aliceProjects).extracting(Project::getProject_name)
                .containsExactlyInAnyOrder("Projet partage", "Projet prive");
    }

    @Test
    void findProjectsByUserEmail_returnsEmpty_whenUserHasNoRole() {
        Users alice = saveUser("alice@test.fr");
        saveProject("Projet", alice);

        assertThat(projectRepository.findProjectsByUserEmail("alice@test.fr")).isEmpty();
    }

    @Test
    void save_setsCreationDateAutomatically() {
        Users alice = saveUser("alice@test.fr");

        Project project = saveProject("Projet", alice);

        assertThat(project.getProject_id()).isNotNull();
        assertThat(project.getProject_creation_date()).isNotNull();
    }

    // ------------------------------------------------------------------
    // RoleRepository
    // ------------------------------------------------------------------

    @Test
    void findRoleByUserAndProject_returnsTheRole() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet", alice);
        saveRole(alice, project, "observer");

        Optional<Role> role = roleRepository.findRoleByUserAndProject(alice.getUsers_id(), project.getProject_id());

        assertThat(role).isPresent();
        assertThat(role.get().getRoleName()).isEqualTo("observer");
    }

    @Test
    void findRoleByUserAndProject_returnsEmpty_forAnotherProject() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet 1", alice);
        Project other = saveProject("Projet 2", alice);
        saveRole(alice, project, "admin");

        assertThat(roleRepository.findRoleByUserAndProject(alice.getUsers_id(), other.getProject_id())).isEmpty();
        assertThat(roleRepository.findRoleByUserAndProject(UUID.randomUUID(), project.getProject_id())).isEmpty();
    }

    @Test
    void findRolesByProjectId_returnsAllRolesOfTheProject() {
        Users alice = saveUser("alice@test.fr");
        Users bob = saveUser("bob@test.fr");
        Project project = saveProject("Projet", alice);
        Project other = saveProject("Autre projet", alice);
        saveRole(alice, project, "admin");
        saveRole(bob, project, "member");
        saveRole(alice, other, "admin");

        List<Role> roles = roleRepository.findRolesByProjectId(project.getProject_id());

        assertThat(roles).extracting(Role::getRoleName).containsExactlyInAnyOrder("admin", "member");
    }

    // ------------------------------------------------------------------
    // TaskRepository
    // ------------------------------------------------------------------

    @Test
    void findByProjectAndStatusIgnoreCase_ignoresCaseAndFiltersByProject() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet 1", alice);
        Project other = saveProject("Projet 2", alice);
        saveTask(project, alice, "Tache A", "todo");
        saveTask(project, alice, "Tache B", "TODO");
        saveTask(project, alice, "Tache C", "DONE");
        saveTask(other, alice, "Tache D", "TODO");

        List<Task> todo = taskRepository.findByProjectAndStatusIgnoreCase(project.getProject_id(), "ToDo");

        assertThat(todo).extracting(Task::getTaskTitle).containsExactlyInAnyOrder("Tache A", "Tache B");
    }

    @Test
    void findByProjectAndStatusIgnoreCase_returnsEmpty_whenNoTaskHasThisStatus() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet", alice);
        saveTask(project, alice, "Tache A", "TODO");

        assertThat(taskRepository.findByProjectAndStatusIgnoreCase(project.getProject_id(), "DONE")).isEmpty();
    }

    @Test
    void findByProject_returnsOnlyTasksOfTheProject() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet 1", alice);
        Project other = saveProject("Projet 2", alice);
        saveTask(project, alice, "Tache A", "TODO");
        saveTask(project, alice, "Tache B", "DONE");
        saveTask(other, alice, "Tache C", "TODO");

        List<Task> tasks = taskRepository.findByProject(project);

        assertThat(tasks).extracting(Task::getTaskTitle).containsExactlyInAnyOrder("Tache A", "Tache B");
    }

    @Test
    void save_persistsAssignedUser() {
        Users alice = saveUser("alice@test.fr");
        Users bob = saveUser("bob@test.fr");
        Project project = saveProject("Projet", alice);
        Task task = saveTask(project, alice, "Tache A", "TODO");

        task.setAssignedUser(bob);
        taskRepository.saveAndFlush(task);

        Task reloaded = taskRepository.findById(task.getTaskId()).orElseThrow();
        assertThat(reloaded.getAssignedUser().getEmail()).isEqualTo("bob@test.fr");
        assertThat(reloaded.getCreatedBy().getEmail()).isEqualTo("alice@test.fr");
    }

    // ------------------------------------------------------------------
    // TaskHistoryRepository
    // ------------------------------------------------------------------

    @Test
    void findByTask_TaskId_returnsOnlyTheHistoryOfThisTask() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet", alice);
        Task task = saveTask(project, alice, "Tache A", "TODO");
        Task otherTask = saveTask(project, alice, "Tache B", "TODO");
        saveHistory(task, alice, "IN_PROGRESS");
        saveHistory(task, alice, "DONE");
        saveHistory(otherTask, alice, "DONE");

        List<TaskHistory> history = taskHistoryRepository.findByTask_TaskId(task.getTaskId());

        assertThat(history).hasSize(2);
        assertThat(history).extracting(TaskHistory::getNewStatus)
                .containsExactlyInAnyOrder("IN_PROGRESS", "DONE");
        assertThat(history).allSatisfy(h -> assertThat(h.getModificationDate()).isNotNull());
    }

    @Test
    void findByTask_TaskId_returnsEmpty_whenTaskHasNoHistory() {
        Users alice = saveUser("alice@test.fr");
        Project project = saveProject("Projet", alice);
        Task task = saveTask(project, alice, "Tache A", "TODO");

        assertThat(taskHistoryRepository.findByTask_TaskId(task.getTaskId())).isEmpty();
    }
}
