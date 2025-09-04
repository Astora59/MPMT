package com.projectManager.pmt.services;

import com.projectManager.pmt.dto.ProjectRequest;
import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Users;

public interface ProjectService {

    Project createProject(ProjectRequest projectRequest);

}
