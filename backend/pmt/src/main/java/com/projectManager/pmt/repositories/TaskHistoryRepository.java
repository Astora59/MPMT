package com.projectManager.pmt.repositories;

import com.projectManager.pmt.models.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {
    List<TaskHistory> findByTask_TaskId(UUID taskId);
}
