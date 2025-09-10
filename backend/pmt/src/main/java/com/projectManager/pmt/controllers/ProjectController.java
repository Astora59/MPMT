package com.projectManager.pmt.controllers;

import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequest projectRequest) {
        // On passe directement l'email fourni dans le DTO comme "admin"
        Project newProject = projectService.createProject(projectRequest);
        return ResponseEntity.ok(newProject);
    }

//    @PostMapping("/{projectId}/invite")
//    public ResponseEntity<String> inviteUser(
//            @PathVariable UUID projectId,
//            @RequestBody InviteRequest inviteRequest) {
//
//        projectService.inviteUser(projectId, inviteRequest.getEmail(), inviteRequest.getRoleName());
//        return ResponseEntity.ok("Invitation envoyée !");
//    }

}
