package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.services.ProjectService;

import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProjectServiceImplementation implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UsersRepository usersRepository;


    @Override
    public Project createProject(ProjectRequest projectRequest) {

        Users adminUser = usersRepository.findByEmail(projectRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l’email : " + projectRequest.getUserEmail()));


        // Créer un nouveau projet
        Project project = new Project();
        project.setProject_name(projectRequest.getProjectName());
        project.setProject_description(projectRequest.getProjectDescription());
        project.setProject_creation_date(LocalDateTime.now());
        project.setProject_admin(adminUser  .getUsers_id());
        project.setEmail(adminUser.getEmail());


        // Sauvegarder en DB
        return projectRepository.save(project);

    }
}
