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
    public Task createTask(UUID projectId, UUID userId, TaskCreationRequest taskCreationRequest) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        if (!project.getProject_admin().equals(userId)) {
            throw new RuntimeException("Seul l'admin du projet peut créer une tâche");
        }



        Task task = new Task();
        task.setTaskTitle(taskCreationRequest.getTaskTitle());
        task.setTaskDescription(taskCreationRequest.getTaskDescription());
        task.setTaskDeadline(taskCreationRequest.getTaskDeadline());
        task.setTaskStatus(taskCreationRequest.getTaskStatus());
        task.setTaskPriority(taskCreationRequest.getTaskPriority());
        task.setProject(project);

        return taskRepository.save(task);
    }


}
