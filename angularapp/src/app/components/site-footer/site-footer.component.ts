import { Component } from '@angular/core';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-site-footer',
  templateUrl: './site-footer.component.html',
  styleUrls: ['./site-footer.component.css']
})
export class SiteFooterComponent {
  readonly year = new Date().getFullYear();

  readonly teamName = 'Spring Warriors';
  readonly leader = { name: 'Prashant', role: 'Team Leader' };
  readonly members = ['Aman', 'Aakansha', 'Siddarth', 'Gowtam', 'Pritam', 'Sarayu'];

  constructor(private authService: AuthService) { }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  scrollTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}