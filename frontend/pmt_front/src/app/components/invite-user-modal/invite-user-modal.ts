import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ProjectService } from '../../services/project-service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-invite-user-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invite-user-modal.html',
  styleUrls: ['./invite-user-modal.scss']
})
export class InviteUserModalComponent {

  @Input() projectId!: string;
  @Output() close = new EventEmitter<void>();
  @Output() invited = new EventEmitter<void>();

  email: string = '';
  loading = false;
  errorMessage = '';

  constructor(private projectService: ProjectService) {}

  onClose() {
    this.close.emit();
  }

  inviteUser() {
    if (!this.email) return;

    this.loading = true;
    this.errorMessage = '';

    this.projectService.inviteUser(this.projectId, this.email)
      .subscribe({
        next: () => {
          this.loading = false;
          this.invited.emit();
          this.onClose();
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = "Impossible d'inviter cet utilisateur.";
          console.error(err);
        }
      });
  }
}