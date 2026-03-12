import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProjectService } from '../../services/project-service';

@Component({
  selector: 'app-create-project-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-project-modal.html',
  styleUrls: ['./create-project-modal.scss']
})
export class CreateProjectModalComponent {

  @Output() close = new EventEmitter<void>();
  @Output() created = new EventEmitter<void>();

  project = {
    projectName: '',
    projectDescription: ''
  };

  constructor(private projectService: ProjectService) {}

  submit() {
     console.log(this.project);
    this.projectService.createProject(this.project).subscribe({
      next: () => {
        this.created.emit();
        this.close.emit();
      }
    });
  }
}
