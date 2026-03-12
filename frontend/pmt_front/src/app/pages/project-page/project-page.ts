import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TaskService, Task } from '../../services/task.service';
import { Navbar } from '../../components/navbar/navbar'; 
import { ProjectMember } from '../../models/project-member';


@Component({
  selector: 'app-project-page',
  imports: [CommonModule, Navbar],
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

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService
  ) {}

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id')!;
    this.loadTasks();
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

  this.projectService.updateUserRole(
    this.projectId,
    member.email,
    newRole
  ).subscribe({

    next: () => {
      member.roleName = newRole;
    },

    error: (err) => {
      console.error("Erreur changement rôle", err);
    }

  });

}
}
