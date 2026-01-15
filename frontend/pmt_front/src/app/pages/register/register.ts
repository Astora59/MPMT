import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrls: ['./register.scss']
})
export class RegisterComponent {

  form = {
    username: '',
    email: '',
    password: ''
  };

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(registerForm: NgForm) {
    if (registerForm.invalid) {
      return;
    }

    this.authService.register(this.form).subscribe({

      next: () => {
        console.log('Inscription réussie');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Erreur inscription', err);
      }
    });
  }
}
