import { Component, ElementRef, HostListener } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-usernav',
  templateUrl: './usernav.component.html',
  styleUrls: ['./usernav.component.css']
})
export class UsernavComponent {
  isLight = document.body.classList.contains('light');
  isMenuOpen = false;
  isProfileOpen = false;
  showLogoutModal = false;

  constructor(private authService: AuthService, private router: Router, private host: ElementRef) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.isMenuOpen = false;
        this.isProfileOpen = false;
      }
    });
  }

  get username(): string {
    return this.authService.getUserName();
  }

  get role(): string {
    return this.authService.getUserRole() ?? '';
  }

  @HostListener('document:click', ['$event'])
  closeProfileOnOutsideClick(event: Event): void {
    const profile = this.host.nativeElement.querySelector('.profile');
    if (this.isProfileOpen && profile && !profile.contains(event.target as Node)) {
      this.isProfileOpen = false;
    }
  }

  toggleTheme(): void {
    this.isLight = !this.isLight;
    document.body.classList.toggle('light', this.isLight);
  }

  askLogout(): void {
    this.isProfileOpen = false;
    this.showLogoutModal = true;
  }

  cancelLogout(): void {
    this.showLogoutModal = false;
  }

  confirmLogout(): void {
    this.showLogoutModal = false;
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.cancelLogout();
    }
  }
}