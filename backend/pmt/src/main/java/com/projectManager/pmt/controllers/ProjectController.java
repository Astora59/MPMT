package com.projectManager.pmt.controllers;

import com.projectManager.pmt.dto.AssignRoleRequest;
import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequest projectRequest) {

        // Récupérer les infos du user connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = (String) authentication.getPrincipal();


        Project newProject = projectService.createProject(projectRequest, email);
        return ResponseEntity.ok(newProject);
    }

//    @PostMapping("/{projectId}/invite")
//    public String inviteUser(@PathVariable UUID projectId,
//                             @RequestBody InviteRequest inviteRequest,
//                             @AuthenticationPrincipal String email) {
//        projectService.inviteUserToProject(projectId, email, inviteRequest);
//        return "Utilisateur invité avec succès";
//    }

//    @PostMapping("/{projectId}/invite")
//    public ResponseEntity<String> inviteUser(@PathVariable UUID projectId,
//                                             @RequestBody InviteRequest inviteRequest,
//                                             @AuthenticationPrincipal String email) {
//        try {
//            projectService.inviteUserToProject(projectId, email, inviteRequest);
//            return ResponseEntity.ok("Utilisateur invité avec succès");
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(500).body(e.getMessage());
//        }
//    }



    @PostMapping("/{projectId}/invite")
    public ResponseEntity<String> inviteUser(
            @PathVariable UUID projectId,
            @RequestBody InviteRequest inviteRequest,
            Principal principal
    ) {
        try {
            projectService.inviteUserToProject(projectId, principal.getName(), inviteRequest);
            return ResponseEntity.ok("Utilisateur invité avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


//    @PutMapping("/{projectId}/role")
//    public String updateUserRole(@PathVariable UUID projectId,
//                                 @RequestBody AssignRoleRequest request,
//                                 @AuthenticationPrincipal String adminEmail) {
//        projectService.updateUserRole(projectId, adminEmail, request);
//        return "Rôle mis à jour avec succès pour " + request.getEmail();
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Project>> getAllProjects() {
//        List<Project> projects = projectService.getAllProjects();
//        return ResponseEntity.ok(projects);
//    }

    @PutMapping("/{projectId}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable UUID projectId,
            @RequestBody AssignRoleRequest request,
            Principal principal
    ) {
        try {
            projectService.updateUserRole(projectId, principal.getName(), request);
            return ResponseEntity.ok("Rôle mis à jour avec succès pour " + request.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }



}
