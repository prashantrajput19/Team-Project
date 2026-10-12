import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { HomePageComponent } from './components/home-page/home-page.component';
import { ErrorComponent } from './components/error/error.component';

import { AdminControlPanelComponent } from './components/admin-control-panel/admin-control-panel.component';
import { AdminViewArchitectComponent } from './components/admin-view-architect/admin-view-architect.component';
import { AdminAddArchitectComponent } from './components/admin-add-architect/admin-add-architect.component';
import { AdminEditArchitectComponent } from './components/admin-edit-architect/admin-edit-architect.component';
import { AdminViewInquiryComponent } from './components/admin-view-inquiry/admin-view-inquiry.component';
import { AdminViewFeedbackComponent } from './components/admin-view-feedback/admin-view-feedback.component';

import { UserViewArchitectComponent } from './components/user-view-architect/user-view-architect.component';
import { UserAddInquiryComponent } from './components/user-add-inquiry/user-add-inquiry.component';
import { UserViewInquiriesComponent } from './components/user-view-inquiries/user-view-inquiries.component';
import { UserAddFeedbackComponent } from './components/user-add-feedback/user-add-feedback.component';
import { UserViewFeedbackComponent } from './components/user-view-feedback/user-view-feedback.component';

import { ArchitectPortfolioComponent } from './components/architect-portfolio/architect-portfolio.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { authGuard, guestGuard } from './guards/auth.guard';

// authGuard: must be logged in (and have data.role, if given).
// guestGuard: only for logged-out users (login / signup pages).
const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'signup', component: SignupComponent, canActivate: [guestGuard] },

  { path: 'home', component: HomePageComponent, canActivate: [authGuard] },
  { path: 'portfolio/:id', component: ArchitectPortfolioComponent, canActivate: [authGuard] },
  { path: 'change-password', component: ChangePasswordComponent, canActivate: [authGuard] },
  { path: 'about', component: AboutUsComponent, canActivate: [authGuard] },

  // Admin pages
  { path: 'admin', component: AdminControlPanelComponent, canActivate: [authGuard], data: { role: 'Admin' } },
  { path: 'admin/architects', component: AdminViewArchitectComponent, canActivate: [authGuard], data: { role: 'Admin' } },
  { path: 'admin/architects/add', component: AdminAddArchitectComponent, canActivate: [authGuard], data: { role: 'Admin' } },
  { path: 'admin/architects/edit/:id', component: AdminEditArchitectComponent, canActivate: [authGuard], data: { role: 'Admin' } },
  { path: 'admin/inquiries', component: AdminViewInquiryComponent, canActivate: [authGuard], data: { role: 'Admin' } },
  { path: 'admin/feedback', component: AdminViewFeedbackComponent, canActivate: [authGuard], data: { role: 'Admin' } },

  // User pages
  { path: 'user/architects', component: UserViewArchitectComponent, canActivate: [authGuard], data: { role: 'User' } },
  { path: 'user/inquiry', component: UserAddInquiryComponent, canActivate: [authGuard], data: { role: 'User' } },
  { path: 'user/inquiries', component: UserViewInquiriesComponent, canActivate: [authGuard], data: { role: 'User' } },
  { path: 'user/feedback/add', component: UserAddFeedbackComponent, canActivate: [authGuard], data: { role: 'User' } },
  { path: 'user/feedback', component: UserViewFeedbackComponent, canActivate: [authGuard], data: { role: 'User' } },

  { path: 'error', component: ErrorComponent, canActivate: [authGuard] },
  { path: '**', component: ErrorComponent, canActivate: [authGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'top' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }