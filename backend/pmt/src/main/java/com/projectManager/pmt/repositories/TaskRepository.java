package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.Project;
import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t FROM Task t WHERE t.project.project_id = :projectId AND UPPER(t.taskStatus) = UPPER(:status)")
    List<Task> findByProjectAndStatusIgnoreCase(@Param("projectId") UUID projectId, @Param("status") String status);


}



