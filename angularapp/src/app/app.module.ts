import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { AuthInterceptor } from './interceptors/auth.interceptor';

import { AdminnavComponent } from './components/adminnav/adminnav.component';
import { UsernavComponent } from './components/usernav/usernav.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { HomePageComponent } from './components/home-page/home-page.component';
import { ErrorComponent } from './components/error/error.component';
import { SiteFooterComponent } from './components/site-footer/site-footer.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { ArchitectPortfolioComponent } from './components/architect-portfolio/architect-portfolio.component';
import { ArchitectAvatarComponent } from './components/architect-avatar/architect-avatar.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { PasswordIllustrationComponent } from './components/password-illustration/password-illustration.component';
import { BarChartComponent } from './components/bar-chart/bar-chart.component';
import { OtpVerificationComponent } from './components/otp-verification/otp-verification.component';
import { HeroIllustrationComponent } from './components/hero-illustration/hero-illustration.component';
import { LoginIllustrationComponent } from './components/login-illustration/login-illustration.component';
import { SignupIllustrationComponent } from './components/signup-illustration/signup-illustration.component';
import { DesignArtComponent } from './components/design-art/design-art.component';
import { AiAssistantComponent } from './components/ai-assistant/ai-assistant.component';

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

@NgModule({
  declarations: [
    AppComponent,
    AdminnavComponent,
    UsernavComponent,
    LoginComponent,
    SignupComponent,
    HomePageComponent,
    ErrorComponent,
    SiteFooterComponent,
    AboutUsComponent,
    ArchitectPortfolioComponent,
    ArchitectAvatarComponent,
    ChangePasswordComponent,
    PasswordIllustrationComponent,
    BarChartComponent,
    OtpVerificationComponent,
    HeroIllustrationComponent,
    LoginIllustrationComponent,
    SignupIllustrationComponent,
    DesignArtComponent,
    AiAssistantComponent,
    AdminControlPanelComponent,
    AdminViewArchitectComponent,
    AdminAddArchitectComponent,
    AdminEditArchitectComponent,
    AdminViewInquiryComponent,
    AdminViewFeedbackComponent,
    UserViewArchitectComponent,
    UserAddInquiryComponent,
    UserViewInquiriesComponent,
    UserAddFeedbackComponent,
    UserViewFeedbackComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    {
      provide: APP_INITIALIZER,
      multi: true,
      deps: [AuthService],
      useFactory: (auth: AuthService) => () => auth.restoreSession()
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }