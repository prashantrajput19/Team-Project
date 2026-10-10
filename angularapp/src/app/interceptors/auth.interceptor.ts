import { Injectable, Injector } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { environment } from 'src/enviornments/enviornment';

// URLs that work without being logged in, so no token is sent to them.
const PUBLIC_PATHS = ['/api/login', '/api/register', '/api/otp/'];

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  // AuthService is fetched through the Injector (not the constructor)
  // because AuthService itself uses HttpClient, which uses this interceptor.
  constructor(private injector: Injector, private router: Router) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const isBackend = request.url.startsWith(environment.apiUrl);
    const isPublic = PUBLIC_PATHS.some(path => request.url.includes(path));
    const token = localStorage.getItem('token');

    if (isBackend && !isPublic && token) {
      request = request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Token expired or invalid: log out and go to the login page.
        if (error.status === 401 && isBackend && !isPublic && !request.url.endsWith('/api/me')) {
          this.injector.get(AuthService).logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}
