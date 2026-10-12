import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { Feedback } from '../../models/feedback.model';
import { ArchitectService } from '../../services/architect.service';
import { FeedbackService } from '../../services/feedback.service';
import { AuthService } from '../../services/auth.service';
import { AiService } from '../../services/ai.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-user-add-feedback',
  templateUrl: './user-add-feedback.component.html',
  styleUrls: ['./user-add-feedback.component.css']
})
export class UserAddFeedbackComponent implements OnInit {
  architectId = 0;
  inquiryId = 0;
  architect?: Architect;

  // Form fields
  feedbackText = '';
  category = 'Service';
  date = new Date().toISOString().substring(0, 10);
  categoryOptions = ['Service', 'Design', 'Communication', 'Pricing', 'Timeliness'];

  // Star rating: hoverRating is the star under the mouse, rating is the one clicked.
  rating = 0;
  hoverRating = 0;
  ratingLabels = ['', 'Poor', 'Fair', 'Good', 'Very good', 'Excellent'];

  isLoading = false;
  isSaving = false;
  isWriting = false; // true while Archie (AI) is writing
  errorMessage = '';

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(
    private architectService: ArchitectService,
    private feedbackService: FeedbackService,
    private authService: AuthService,
    private aiService: AiService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  get shownRating(): number {
    return this.hoverRating || this.rating;
  }

  ngOnInit(): void {
    // The architect is passed in the URL: /user/feedback/add?architectId=5
    this.architectId = Number(this.route.snapshot.queryParamMap.get('architectId'));
    this.inquiryId = Number(this.route.snapshot.queryParamMap.get('inquiryId'));

    if (!this.architectId) {
      this.router.navigate(['/user/inquiries']);
      return;
    }

    this.loadArchitect();
  }

  loadArchitect(): void {
    this.isLoading = true;

    this.architectService.getAllArchitects().subscribe({
      next: (architects) => {
        this.isLoading = false;
        this.architect = architects.find(a => a.architectId === this.architectId);

        if (!this.architect) {
          this.errorMessage = 'Architect not found';
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to load architects');
      }
    });
  }

  // Archie (AI) writes the feedback from the stars, category and whatever the user typed
  writeWithAi(): void {
    this.isWriting = true;

    this.aiService.write({
      type: 'feedback',
      notes: this.feedbackText,
      architectId: this.architectId,
      rating: this.rating,
      category: this.category
    }).subscribe({
      next: (response) => {
        this.isWriting = false;
        this.feedbackText = response.text;
      },
      error: (error) => {
        this.isWriting = false;
        this.showAlert('error', 'Archie', describeError(error, 'Archie could not write it. Please try again.'));
      }
    });
  }

  save(): void {
    const userId = this.authService.getUserId();

    const feedback: Feedback = {
      feedbackText: this.feedbackText,
      date: this.date,
      category: this.category,
      rating: this.rating,
      user: { userId },
      architect: { architectId: this.architectId },
      inquiryId: this.inquiryId
    };

    this.isSaving = true;

    this.feedbackService.sendFeedback(feedback).subscribe({
      next: () => {
        this.isSaving = false;
        this.showAlert('success', 'Thank you', 'Feedback submitted successfully!');
      },
      error: (error) => {
        this.isSaving = false;
        this.showAlert('error', 'Failed', describeError(error, 'Failed to submit feedback. Please try again.'));
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/user/feedback']);
  }

  showAlert(type: 'success' | 'error', title: string, message: string): void {
    this.alert = { show: true, type, title, message };
  }

  closeAlert(): void {
    this.alert.show = false;
    if (this.alert.type === 'success') {
      this.router.navigate(['/user/feedback']);
    }
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.closeAlert();
    }
  }
}
