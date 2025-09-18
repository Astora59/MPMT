package com.projectManager.pmt.controllers;

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

    @PostMapping("/{projectId}/invite")
    public String inviteUser(@PathVariable UUID projectId,
                             @RequestBody InviteRequest inviteRequest,
                             @AuthenticationPrincipal String email) {
        projectService.inviteUserToProject(projectId, email, inviteRequest);
        return "Utilisateur invité avec succès";
    }

}
