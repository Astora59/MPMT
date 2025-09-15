package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Role;
import com.projectManager.pmt.repositories.RoleRepository;
import com.projectManager.pmt.services.ProjectService;

import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectServiceImplementation implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

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
        project.setProject_admin(adminUser.getUsers_id());
        project.setEmail(adminUser.getEmail());

        // Sauvegarder le projet
        Project savedProject = projectRepository.save(project);


        Optional<Role> existingRole = roleRepository.findRoleByUserAndProject(
                adminUser.getUsers_id(),
                savedProject.getProject_id()
        );

        if (existingRole.isEmpty()) {
            Role adminRole = new Role();
            adminRole.setProject(savedProject);
            adminRole.setUser(adminUser);
            adminRole.setRoleName("admin"); // rôle admin
            roleRepository.save(adminRole);
        }

        return savedProject;
    }

//    public void inviteUser(UUID projectId, String email, String roleName) {
//
//
//        // 1. Récupérer le projet
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new RuntimeException("Projet introuvable"));
//
//        // 2. Récupérer l'utilisateur
//        Users user = usersRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));
//
//        // 3. Vérifier s’il a déjà un rôle sur ce projet
//        if (roleRepository.findByUser_Users_idAndProject_Project_id(user.getUsers_id(), project.getProject_id()).isPresent()) {
//            throw new RuntimeException("Utilisateur déjà membre du projet");
//        }
//
//        // 4. Créer le rôle
//        Role role = new Role();
//        role.setProject(project);
//        role.setUser(user);
//        role.setRoleName(roleName != null ? roleName : "member"); // par défaut member
//
//        roleRepository.save(role);
//    }
}
