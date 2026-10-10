import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.css']
})
export class ErrorComponent {
  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) { }

  get isForbidden(): boolean {
    return this.route.snapshot.queryParamMap.get('reason') === 'forbidden';
  }

  get neededRole(): string {
    return this.route.snapshot.queryParamMap.get('need') ?? '';
  }

  get currentRole(): string {
    return this.authService.getUserRole() ?? '';
  }

  get homeLink(): string {
    return this.authService.isAdmin() ? '/admin' : '/home';
  }

  switchAccount(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  goBack(): void {
    window.history.back();
  }
}