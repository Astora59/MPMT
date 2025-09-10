package com.projectManager.pmt.services;

import com.projectManager.pmt.dto.InviteRequest;
import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Users;

import java.util.UUID;

public interface ProjectService {

    Project createProject(ProjectRequest projectRequest);


//    void inviteUser(UUID projectId, String email, String roleName);
}
