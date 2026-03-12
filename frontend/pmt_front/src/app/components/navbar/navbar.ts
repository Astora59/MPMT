import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  imports: [],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss'
})
export class Navbar {


      logout(): void {
        localStorage.removeItem('token'); // supprime le JWT
        window.location.reload();         // recharge la page
      }
}
