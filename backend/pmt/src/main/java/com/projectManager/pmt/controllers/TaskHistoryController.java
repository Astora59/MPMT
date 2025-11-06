package com.projectManager.pmt.controllers;

import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.ProjectRepository;
import com.projectManager.pmt.repositories.RoleRepository;
import com.projectManager.pmt.repositories.UsersRepository;
import com.projectManager.pmt.services.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class TaskHistoryController {

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsersRepository usersRepository;

    private boolean hasAccess(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return false;

        if (project.getProject_admin().equals(userId)) return true;
        return roleRepository.findRoleByUserAndProject(userId, projectId).isPresent();
    }



}
