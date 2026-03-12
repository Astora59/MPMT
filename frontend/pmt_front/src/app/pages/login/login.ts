import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
   form = {
    email: '',
    password: ''
  };

  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(form: NgForm) {
    if (form.invalid) return;

    this.authService.login(this.form).subscribe({
      next: (response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem("userId", response.users_id);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.errorMessage = 'Email ou mot de passe incorrect';
      }
    });
  }
}
