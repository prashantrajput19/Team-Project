import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, tap } from 'rxjs';

import { User } from '../models/user.model';
import { Login } from '../models/login.model';
import { environment } from 'src/enviornments/enviornment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/api`;

  // The logged-in user. Only the token is kept in localStorage;
  // the user details are loaded again from /api/me on page refresh.
  private currentUser: Partial<User> | null = null;

  constructor(private http: HttpClient) { }

  register(user: User): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, user);
  }

  sendOtp(email: string): Observable<{ cooldownSeconds: number }> {
    return this.http.post<{ cooldownSeconds: number }>(`${this.apiUrl}/otp/send`, { email });
  }

  verifyOtp(email: string, code: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/otp/verify`, { email, code });
  }

  login(login: Login): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, login).pipe(
      tap(response => {
        if (response?.token) {
          localStorage.clear();
          localStorage.setItem('token', response.token);
          this.currentUser = {
            userId: response.userId,
            username: response.username,
            userRole: response.userRole
          };
        }
      })
    );
  }

  // Called once when the app starts (see app.module.ts).
  restoreSession(): Observable<Partial<User> | null> {
    if (!this.getToken()) {
      return of(null);
    }

    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      tap(user => this.currentUser = user),
      catchError(() => {
        this.logout();
        return of(null);
      })
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me/password`, { currentPassword, newPassword });
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUserRole(): string | null {
    return this.currentUser?.userRole ?? null;
  }

  getUserId(): number | null {
    return this.currentUser?.userId ?? null;
  }

  getUserName(): string {
    return this.currentUser?.username ?? '';
  }

  isAdmin(): boolean {
    return this.getUserRole() === 'Admin';
  }

  isUser(): boolean {
    return this.getUserRole() === 'User';
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && !!this.currentUser;
  }

  logout(): void {
    localStorage.clear();
    this.currentUser = null;
  }
}
