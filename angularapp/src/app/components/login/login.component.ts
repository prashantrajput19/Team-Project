import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { Login } from '../../models/login.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginData: Login = {
    email: '',
    password: ''
  };

  errorMessage = '';
  isLoading = false;

  isLight = document.body.classList.contains('light');

  emailPattern = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$';

  constructor(private authService: AuthService, private router: Router) { }

  toggleTheme(): void {
    this.isLight = !this.isLight;
    document.body.classList.toggle('light', this.isLight);
  }

  login(): void {
    this.errorMessage = '';
    this.isLoading = true;

    this.authService.login(this.loginData).subscribe({
      next: (response) => {
        this.authService.saveLogin(response);
        this.isLoading = false;

        if (this.authService.isAdmin()) {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/home']);
        }
      },

      error: (error) => {
        this.isLoading = false;

        if (error.status === 0) {
          this.errorMessage =
            'Unable to connect to the backend. Check backend URL, server and CORS.';
        } else {
          this.errorMessage = 'Invalid Email or Password';
        }
      }
    });
  }
}