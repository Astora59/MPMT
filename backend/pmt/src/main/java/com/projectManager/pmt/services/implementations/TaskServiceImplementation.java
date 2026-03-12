package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.TaskCreationRequest;
import com.projectManager.pmt.dto.TaskUpdateRequest;
import com.projectManager.pmt.models.*;
import com.projectManager.pmt.repositories.*;
import com.projectManager.pmt.services.EmailService;
import com.projectManager.pmt.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImplementation implements TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private EmailService emailService;
    @Autowired private TaskHistoryRepository taskHistoryRepository;


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

        // 🔔 Envoie un mail de notification
        emailService.sendTaskAssignedEmail(
                targetUser.getEmail(),
                task.getTaskTitle(),
                project.getProject_name(),
                currentUser.getUsername() // ou currentUser.getEmail() selon ton modèle
        );



        return taskRepository.save(task);
    }



    @Override
    public Task updateTask(UUID projectId, UUID taskId, String userEmail, TaskUpdateRequest updateRequest) {

        // Récupère le projet
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Récupère l’utilisateur
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifie si l’utilisateur est admin ou membre du projet
        boolean hasAccess = project.getProject_admin().equals(user.getUsers_id())
                || roleRepository.findRoleByUserAndProject(user.getUsers_id(), projectId)
                .map(role -> role.getRoleName().equalsIgnoreCase("member"))
                .orElse(false);

        if (!hasAccess) {
            throw new RuntimeException("Accès refusé : seuls les admins ou membres peuvent modifier une tâche.");
        }

        // Récupère la tâche
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

        // Vérifie que la tâche appartient bien au projet
        if (!task.getProject().getProject_id().equals(projectId)) {
            throw new RuntimeException("Cette tâche n'appartient pas à ce projet.");
        }

        // Sauvegarde les anciennes valeurs avant modification
        String oldStatus = task.getTaskStatus();
        String oldPriority = task.getTaskPriority();

        // Met à jour les champs si présents
        if (updateRequest.getTaskTitle() != null) task.setTaskTitle(updateRequest.getTaskTitle());
        if (updateRequest.getTaskDescription() != null) task.setTaskDescription(updateRequest.getTaskDescription());
        if (updateRequest.getTaskStatus() != null) task.setTaskStatus(updateRequest.getTaskStatus());
        if (updateRequest.getTaskPriority() != null) task.setTaskPriority(updateRequest.getTaskPriority());
        if (updateRequest.getTaskDeadline() != null) task.setTaskDeadline(updateRequest.getTaskDeadline());

        Task updatedTask = taskRepository.save(task);

        // ✅ Enregistre l’historique de modification
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setModifiedBy(user);
        history.setOldStatus(oldStatus);
        history.setNewStatus(updatedTask.getTaskStatus());
        history.setOldPriority(oldPriority);
        history.setNewPriority(updatedTask.getTaskPriority());
        history.setChangeDescription("Tâche mise à jour par " + user.getEmail());

        if (history.getOldPriority() != null) history.setOldPriority(history.getOldPriority().toUpperCase());
        if (history.getNewPriority() != null) history.setNewPriority(history.getNewPriority().toUpperCase());
        if (history.getOldStatus() != null) history.setOldStatus(history.getOldStatus().toUpperCase());
        if (history.getNewStatus() != null) history.setNewStatus(history.getNewStatus().toUpperCase());


        taskHistoryRepository.save(history);

        return updatedTask;
    }


    @Override
    public Task getTaskById(UUID projectId, UUID taskId, String userEmail) {

        // Récupération du projet
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Récupération de l’utilisateur
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifie si l’utilisateur a accès au projet (admin, member ou observer)
        boolean hasAccess = project.getProject_admin().equals(user.getUsers_id())
                || roleRepository.findRoleByUserAndProject(user.getUsers_id(), projectId)
                .map(role -> {
                    String roleName = role.getRoleName().toLowerCase();
                    return roleName.equals("member") || roleName.equals("observer");
                })
                .orElse(false);

        if (!hasAccess) {
            throw new RuntimeException("Accès refusé : vous n'avez pas les droits pour voir cette tâche.");
        }

        // Récupération de la tâche
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

        // Vérifie que la tâche appartient bien au projet demandé
        if (!task.getProject().getProject_id().equals(projectId)) {
            throw new RuntimeException("Cette tâche n'appartient pas à ce projet.");
        }

        return task;

    }

    @Override
    public List<Task> getTasksByStatus(UUID projectId, String userEmail, String status) {
        // Vérifie si le projet existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Vérifie que l’utilisateur existe
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifie si l’utilisateur a accès au projet
        boolean hasAccess = roleRepository.findRoleByUserAndProject(user.getUsers_id(), project.getProject_id()).isPresent()
                || project.getProject_admin().equals(user.getUsers_id());

        if (!hasAccess) {
            throw new RuntimeException("Accès refusé : seuls les membres, observateurs ou admins peuvent voir les tâches.");
        }

        // Retourne les tâches selon le statut
        return taskRepository.findByProjectAndStatusIgnoreCase(projectId, status);
    }

    @Override
    public List<TaskHistory> getTaskHistory(UUID projectId, UUID taskId, String userEmail) {

        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Vérifie que la tâche appartient au projet
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

        if (!task.getProject().getProject_id().equals(projectId)) {
            throw new RuntimeException("Erreur : cette tâche n'appartient pas à ce projet.");
        }

        boolean hasAccess = roleRepository
                .findRoleByUserAndProject(user.getUsers_id(), project.getProject_id())
                .isPresent();

        if (!hasAccess) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas membre de ce projet.");
        }

        return taskHistoryRepository.findByTask_TaskId(taskId);
    }

    @Override
    public List<Task> getAllTasksByProject(UUID projectId, String userEmail) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Vérifier que l'utilisateur appartient au projet
        roleRepository.findRoleByUserAndProject(
                usersRepository.findByEmail(userEmail).orElseThrow().getUsers_id(),
                projectId
        ).orElseThrow(() -> new RuntimeException("Accès refusé"));

        return taskRepository.findByProject(project);
    }


}
