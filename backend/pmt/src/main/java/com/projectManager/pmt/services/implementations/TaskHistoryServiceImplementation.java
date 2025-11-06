package com.projectManager.pmt.services.implementations;

import com.projectManager.pmt.models.Task;
import com.projectManager.pmt.models.TaskHistory;
import com.projectManager.pmt.models.Users;
import com.projectManager.pmt.repositories.TaskHistoryRepository;
import com.projectManager.pmt.services.TaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskHistoryServiceImplementation implements TaskHistoryService {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;


    @Override
    public void logChange(Task task, Users modifiedBy, String oldStatus, String newStatus, String oldPriority, String newPriority, String description) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setModifiedBy(modifiedBy);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangeDescription(description);


        taskHistoryRepository.save(history);
    }

    @Override
    public List<TaskHistory> getHistoryByTaskId(UUID taskId) {
        return taskHistoryRepository.findByTask_TaskId(taskId);
    }
}
