import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ProjectService } from '../../services/project-service';
import { Navbar } from '../../components/navbar/navbar';
import { InviteUserModalComponent } from "../../components/invite-user-modal/invite-user-modal";
import { CreateProjectModalComponent } from "../../components/create-project-modal/create-project-modal";
import { Project } from '../../models/project.model';
// export interface Project {
//   project_id: string;
//   project_name: string;
//   project_description: string;
//   project_creation_date: string;
//   project_admin: string;
//   email: string;
//   isAdmin: boolean;
// }


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, Navbar, CreateProjectModalComponent, RouterModule, InviteUserModalComponent],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss']
})
export class DashboardComponent implements OnInit {

  projects: Project[] = [];
  showModal = false;
  loading = false;
  currentUserId: string | null = null;
  

  constructor(private projectService: ProjectService) {}

  ngOnInit(): void {
    this.currentUserId = localStorage.getItem("userId");
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;

    this.projectService.getMyProjects().subscribe({
      next: (projects: Project[]) => {
        console.log("Projets chargés :", projects);
        this.projects = projects;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur chargement projets :", err);
        this.loading = false;
      }
    });
  }

  openModal(): void {
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.loadProjects(); // 🔥 Recharge les projets après création
  }


  selectedProjectId: string | null = null;
  showInviteModal = false;

  openInviteModal(projectId: string) {
    this.selectedProjectId = projectId;
    this.showInviteModal = true;
  }

  closeInviteModal() {
    this.showInviteModal = false;
    this.selectedProjectId = null;
  }


}
