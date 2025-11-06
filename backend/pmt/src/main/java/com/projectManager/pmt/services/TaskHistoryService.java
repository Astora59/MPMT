package com.projectManager.pmt.services;

import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.models.Users;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryService {
    void logChange(Task task, Users modifiedBy, String oldStatus, String newStatus, String oldPriority, String newPriority, String description);
    List<TaskHistory> getHistoryByTaskId(UUID taskId);}
