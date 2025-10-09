package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Role;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.RoleRepository;
import com.projectManager.pmt.repositories.TaskRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskServiceImplementation implements TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UsersRepository usersRepository;


    @Override
    public Task createTask(UUID projectId, String userEmail, TaskCreationRequest taskCreationRequest) {
        System.out.println("Email extrait du token = " + userEmail);


        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Vérifier si l’utilisateur est admin ou membre
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        boolean hasAccess = project.getProject_admin().equals(user.getUsers_id())
                || /* si tu as une table ProjectRole */ true; // Ici tu ajoutes la logique membre/admin

        if (!hasAccess) {
            throw new RuntimeException("Accès refusé : seuls les admins ou membres peuvent créer une tâche.");
        }

        Task task = new Task();
        task.setTaskTitle(taskCreationRequest.getTaskTitle());
        task.setTaskDescription(taskCreationRequest.getTaskDescription());
        task.setTaskDeadline(taskCreationRequest.getTaskDeadline());
        task.setTaskStatus(taskCreationRequest.getTaskStatus());
        task.setTaskPriority(taskCreationRequest.getTaskPriority());
        task.setProject(project);
        task.setCreatedBy(user);

        return taskRepository.save(task);
    }

    @Override
    public Task assignTaskToUser(UUID projectId, UUID taskId, String currentUserEmail, String targetUserEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        Users currentUser = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur actuel introuvable"));

        Users targetUser = usersRepository.findByEmail(targetUserEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));

        // Vérifie si currentUser est admin ou membre
        boolean hasAccess = roleRepository.findRoleByUserAndProject(currentUser.getUsers_id(), project.getProject_id())
                .map(role -> role.getRoleName().equals("admin") || role.getRoleName().equals("member"))
                .orElse(false);

        if (!hasAccess) {
            throw new RuntimeException("Accès refusé : seuls les admins ou membres peuvent assigner une tâche.");
        }

        // Vérifie que targetUser appartient au projet
        boolean targetIsMember = roleRepository.findRoleByUserAndProject(targetUser.getUsers_id(), project.getProject_id()).isPresent();
        if (!targetIsMember) {
            throw new RuntimeException("L’utilisateur cible n’est pas membre du projet.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

        // Vérifie que la tâche appartient bien au projet
        if (!task.getProject().getProject_id().equals(projectId)) {
            throw new RuntimeException("Cette tâche n'appartient pas au projet.");
        }

        // Assigne la tâche
        task.setAssignedUser(targetUser);

        return taskRepository.save(task);
    }


}
