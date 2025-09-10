package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Role;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {



}
