import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TaskService, Task } from '../../services/task.service';
import { Navbar } from '../../components/navbar/navbar'; 
import { ProjectService } from '../../services/project-service';
import { ProjectMember } from '../../models/project-member';
import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-project-page',
  imports: [CommonModule, Navbar, FormsModule],
  templateUrl: './project-page.html',
  styleUrl: './project-page.scss'
})
export class ProjectPage implements OnInit {

   projectId!: string;
  tasks: Task[] = [];
  pendingTasks: Task[] = [];
  inProgressTasks: Task[] = [];
  completedTasks: Task[] = [];
  members: ProjectMember[] = [];


  showModal = false;

  selectedTask: any = null;
  editMode = false;


  taskHistory: any[] = [];

  newTask = {
  taskTitle: '',
  taskDescription: '',
  taskDeadline: '',
  taskStatus: 'pending',
  taskPriority: 'Medium'
}

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadTasks();


    this.projectService.getProjectMembers(this.projectId).subscribe({
      next: (data) => {
        this.members = data
        console.log("Membres chargés :", data)
      },
      error: (err) => {
        console.error("Erreur chargement membres", err)
      }
    })

    this.projectService.getProjectMembers(this.projectId).subscribe({
  next: (data) => {
    this.members = data
    console.log("Membres chargés :", data)
  },
  error: (err) => {
    console.error("Erreur chargement membres", err)
  }
})
  }

  loadTasks() {
    this.taskService.getTasksByProject(this.projectId).subscribe({
      next: (data) => {
        this.tasks = data;


    this.pendingTasks = this.tasks.filter(t => t.taskStatus === 'pending');
    this.inProgressTasks = this.tasks.filter(t => t.taskStatus === 'in_progress');
    this.completedTasks = this.tasks.filter(t => t.taskStatus === 'completed');
      },
      error: (err) => {
        console.error("Erreur chargement tâches", err);
      }
    });
  }



changeRole(member: ProjectMember, newRole: string) {

  if (member.roleName === newRole) return;

  const body = {
    email: member.email,
    roleName: newRole
  }

  this.projectService.updateUserRole(this.projectId, body).subscribe({
    next: () => {
      member.roleName = newRole
      console.log("Rôle mis à jour")
    },
    error: (err: any) => {
      console.error("Erreur mise à jour rôle", err)
    }
  })
}

assignTask(task: any, email: string) {

  if (!email) return

  this.projectService.assignTask(
    this.projectId,
    task.taskId,
    email
  ).subscribe({

    next: (updatedTask: any) => {

      console.log("Tâche assignée", updatedTask)

      // met à jour la tâche localement
      task.assignedUser = updatedTask.assignedUser
    },

    error: (err: any) => {
      console.error("Erreur assignation tâche", err)
    }

  })

}

openModal() {
  this.showModal = true;
}

closeModal() {
  this.showModal = false;
}

  createTask() {

    const formattedDate = this.newTask.taskDeadline
    ? this.newTask.taskDeadline + "T00:00:00"
    : null

  const body = {
    taskTitle: this.newTask.taskTitle,
    taskDescription: this.newTask.taskDescription,
    taskDeadline: formattedDate,
    taskStatus: this.newTask.taskStatus,
    taskPriority: this.newTask.taskPriority
  }

  this.projectService.createTask(this.projectId, body).subscribe({

    next: (data: any) => {

      console.log("Tâche créée", data)

      // recharge les tâches
      this.loadTasks()
      this.closeModal()

      // reset formulaire
      this.newTask = {
        taskTitle: '',
        taskDescription: '',
        taskDeadline: '',
        taskStatus: 'pending',
        taskPriority: 'Medium'
      }

    },

    error: (err: any) => {
      console.error("Erreur création tâche", err)
    }

  })

}


openTask(task: any) {
  this.selectedTask = { ...task }; // clone
  this.editMode = false;

  this.projectService.getTaskHistory(this.projectId, task.taskId).subscribe({
    next: (data) => {
      this.taskHistory = data;
      console.log("Historique :", data);
    },
    error: (err) => {
      console.error("Erreur historique", err);
    }
  });
}


closeTaskModal() {
  this.selectedTask = null;
  this.editMode = false;
}

enableEdit() {
  this.editMode = true;
}

 
updateTask() {

  const formattedDate = this.selectedTask.taskDeadline
    ? this.selectedTask.taskDeadline + "T00:00:00"
    : null;

  const body = {
    taskTitle: this.selectedTask.taskTitle,
    taskDescription: this.selectedTask.taskDescription,
    taskDeadline: formattedDate,
    taskStatus: this.selectedTask.taskStatus,
    taskPriority: this.selectedTask.taskPriority
  };

  

  this.projectService.updateTask(
    this.projectId,
    this.selectedTask.taskId,
    body
  ).subscribe({

    next: () => {
      this.loadTasks();
      this.closeTaskModal();

      // 🔥 recharge historique direct
  this.projectService.getTaskHistory(this.projectId, this.selectedTask.taskId)
    .subscribe(data => this.taskHistory = data);

  this.editMode = false;
    },

    error: (err: any) => {
      console.error("Erreur update tâche", err);
    }

  });
}


}
