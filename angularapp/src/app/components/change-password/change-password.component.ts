import { Component } from '@angular/core';

import { AuthService } from '../../services/auth.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  showPasswords = false;

  isSaving = false;
  isDone = false;
  errorMessage = '';

  constructor(private authService: AuthService) { }

  get homeLink(): string {
    return this.authService.isAdmin() ? '/admin' : '/home';
  }

  get passwordsMatch(): boolean {
    return this.newPassword === this.confirmPassword;
  }

  save(): void {
    this.errorMessage = '';
    this.isSaving = true;

    this.authService.changePassword(this.currentPassword, this.newPassword).subscribe({
      next: () => {
        this.isSaving = false;
        this.isDone = true;
      },
      error: error => {
        this.isSaving = false;
        this.errorMessage = describeError(error, 'Could not change your password. Please try again.');
      }
    });
  }
}