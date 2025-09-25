package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.dto.AssignRoleRequest;
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
    public Project createProject(ProjectRequest projectRequest, String email) {

        Users adminUser = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l’email : " + email));


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

    @Override
    public Project inviteUserToProject(UUID project_id, String adminEmail, InviteRequest inviteRequest) {
        // Vérifier que le projet existe
        Project project = projectRepository.findById(project_id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // Vérifier que l’utilisateur qui invite est bien l’admin
        Users adminUser = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l’email : " + adminEmail));


        if (!project.getProject_admin().equals(adminUser.getUsers_id())) {
            throw new RuntimeException("Seul l’admin peut inviter des utilisateurs dans ce projet");
        }


        // Chercher l’utilisateur invité
        Users invitedUser = usersRepository.findByEmail(inviteRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur invité non trouvé"));


        // Créer un rôle (member par défaut si pas spécifié)
        Role role = new Role();
        role.setProject(project);
        role.setUser(invitedUser);
        role.setRoleName(
                (inviteRequest.getRoleName() == null || inviteRequest.getRoleName().isEmpty())
                        ? "member"
                        : inviteRequest.getRoleName()
        );

        roleRepository.save(role);

        return project;

    }


    @Override
    public Project updateUserRole(UUID projectId, String adminEmail, AssignRoleRequest request) {
        // Vérifier que le projet existe
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        // Vérifier que l’adminEmail correspond bien à un admin du projet
        Users adminUser = usersRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur admin introuvable"));

        Role adminRole = roleRepository.findRoleByUserAndProject(adminUser.getUsers_id(), project.getProject_id())
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        if (!"admin".equals(adminRole.getRoleName())) {
            throw new RuntimeException("Vous n’avez pas les droits pour attribuer des rôles dans ce projet");
        }

        // Vérifier que l’utilisateur cible existe
        Users targetUser = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur cible introuvable"));

        // Vérifier si un rôle existe déjà pour cet utilisateur dans ce projet
        Optional<Role> existingRole = roleRepository.findRoleByUserAndProject(
                targetUser.getUsers_id(), project.getProject_id()
        );

        Role role;
        if (existingRole.isPresent()) {
            // mettre à jour le rôle
            role = existingRole.get();
            role.setRoleName(request.getRoleName());
        } else {
            // créer un nouveau rôle
            role = new Role();
            role.setProject(project);
            role.setUser(targetUser);
            role.setRoleName(request.getRoleName() != null ? request.getRoleName() : "member");
        }

        roleRepository.save(role);
        return project;
    }



}
