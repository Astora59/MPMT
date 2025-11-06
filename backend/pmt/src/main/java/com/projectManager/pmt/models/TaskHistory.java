package com.projectManager.pmt.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "history_id")
    private UUID historyId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = false)
    private Users modifiedBy;

    @Column(name = "modification_date")
    private LocalDateTime modificationDate = LocalDateTime.now();

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "old_priority")
    private String oldPriority;

    @Column(name = "new_priority")
    private String newPriority;

    @Column(name = "change_description", columnDefinition = "TEXT")
    private String changeDescription;

    // Getters et setters
    public UUID getHistoryId() { return historyId; }
    public void setHistoryId(UUID historyId) { this.historyId = historyId; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Users getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Users modifiedBy) { this.modifiedBy = modifiedBy; }

    public LocalDateTime getModificationDate() { return modificationDate; }
    public void setModificationDate(LocalDateTime modificationDate) { this.modificationDate = modificationDate; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getOldPriority() { return oldPriority; }
    public void setOldPriority(String oldPriority) { this.oldPriority = oldPriority; }

    public String getNewPriority() { return newPriority; }
    public void setNewPriority(String newPriority) { this.newPriority = newPriority; }

    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}
