import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';

import { AuthService } from '../../services/auth.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-otp-verification',
  templateUrl: './otp-verification.component.html',
  styleUrls: ['./otp-verification.component.css']
})
export class OtpVerificationComponent implements OnChanges, OnDestroy {
  @Input() email = '';
  @Input() emailValid = false;
  @Output() verified = new EventEmitter<void>();

  code = '';
  isSent = false;
  isVerified = false;
  isSending = false;
  isVerifying = false;
  secondsLeft = 0;
  errorMessage = '';

  private timer?: ReturnType<typeof setInterval>;

  constructor(private authService: AuthService) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['email'] && !changes['email'].firstChange && !this.isVerified) {
      this.reset();
    }
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  sendOtp(): void {
    this.errorMessage = '';
    this.isSending = true;

    this.authService.sendOtp(this.email).subscribe({
      next: (response) => {
        this.isSending = false;
        this.isSent = true;
        this.startTimer(response.cooldownSeconds);
      },
      error: (error) => {
        this.isSending = false;
        this.errorMessage = describeError(error, 'Could not send the OTP');
      }
    });
  }

  verifyOtp(): void {
    this.errorMessage = '';
    this.isVerifying = true;

    this.authService.verifyOtp(this.email, this.code).subscribe({
      next: () => {
        this.isVerifying = false;
        this.isVerified = true;
        clearInterval(this.timer);
        this.verified.emit();
      },
      error: (error) => {
        this.isVerifying = false;
        this.errorMessage = describeError(error, 'Could not verify the OTP');
      }
    });
  }

  private reset(): void {
    clearInterval(this.timer);
    this.isSent = false;
    this.code = '';
    this.secondsLeft = 0;
    this.errorMessage = '';
  }

  private startTimer(seconds: number): void {
    clearInterval(this.timer);
    this.secondsLeft = seconds;
    this.timer = setInterval(() => {
      this.secondsLeft--;
      if (this.secondsLeft <= 0) {
        clearInterval(this.timer);
      }
    }, 1000);
  }
}