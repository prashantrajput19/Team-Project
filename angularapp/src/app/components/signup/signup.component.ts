import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  user: User = {
    email: '',
    password: '',
    username: '',
    mobileNumber: '',
    userRole: 'User'
  };

  confirmPassword = '';

  errorMessage = '';
  showSuccessModal = false;
  isLoading = false;

  isLight = document.body.classList.contains('light');

  emailPattern = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$';

  emailVerified = false;

  constructor(private authService: AuthService, private router: Router) { }

  toggleTheme(): void {
    this.isLight = !this.isLight;
    document.body.classList.toggle('light', this.isLight);
  }

  register(): void {
    this.errorMessage = '';

    if (this.user.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    this.isLoading = true;

    this.authService.register(this.user).subscribe({
      next: () => {
        this.isLoading = false;
        this.showSuccessModal = true;
      },

      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Registration failed');
      }
    });
  }

  closeModal(): void {
    this.showSuccessModal = false;
    this.router.navigate(['/login']);
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === 'Escape') {
      this.closeModal();
    }
  }
}