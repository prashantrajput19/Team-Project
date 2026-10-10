import { Component, OnInit } from '@angular/core';

import { Feedback } from '../../models/feedback.model';
import { FeedbackService } from '../../services/feedback.service';
import { AuthService } from '../../services/auth.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-user-view-feedback',
  templateUrl: './user-view-feedback.component.html',
  styleUrls: ['./user-view-feedback.component.css']
})
export class UserViewFeedbackComponent implements OnInit {
  feedbacks: Feedback[] = [];
  isLoading = false;
  errorMessage = '';

  // Filters
  search = '';
  categoryFilter = '';

  // Delete popup
  feedbackToDelete?: Feedback;
  showDeleteModal = false;
  isDeleting = false;

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(
    private feedbackService: FeedbackService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  loadFeedbacks(): void {
    const userId = this.authService.getUserId();

    if (!userId) {
      this.errorMessage = 'Please login again to see your feedback';
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.feedbackService.getAllFeedbacksByUserId(userId).subscribe({
      next: (feedbacks) => {
        this.isLoading = false;
        this.feedbacks = feedbacks || [];
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve your feedback');
      }
    });
  }

  get categoryOptions(): string[] {
    const categories = this.feedbacks.map(f => f.category).filter(c => !!c);
    return Array.from(new Set(categories)).sort();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.categoryFilter);
  }

  get filteredFeedbacks(): Feedback[] {
    const search = this.search.trim().toLowerCase();

    return this.feedbacks.filter(feedback =>
      (!search ||
        (feedback.feedbackText || '').toLowerCase().includes(search) ||
        this.architectName(feedback).toLowerCase().includes(search)) &&
      (!this.categoryFilter || feedback.category === this.categoryFilter)
    );
  }

  clearFilters(): void {
    this.search = '';
    this.categoryFilter = '';
  }

  architectName(feedback: Feedback): string {
    return feedback.architect?.name ?? '';
  }

  // Opens the delete popup.
  deleteFeedback(feedback: Feedback): void {
    this.feedbackToDelete = feedback;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    const feedbackId = this.feedbackToDelete?.feedbackId;

    if (feedbackId === undefined) {
      return;
    }

    this.isDeleting = true;

    this.feedbackService.deleteFeedback(feedbackId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.showAlert('success', 'Deleted', 'Your feedback was deleted successfully.');
        this.loadFeedbacks();
      },
      error: (error) => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.showAlert('error', 'Delete Failed', describeError(error, 'Failed to delete this feedback.'));
      }
    });
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.feedbackToDelete = undefined;
  }

  showAlert(type: 'success' | 'error', title: string, message: string): void {
    this.alert = { show: true, type, title, message };
  }

  closeAlert(): void {
    this.alert.show = false;
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      if (!this.isDeleting) {
        this.closeDeleteModal();
      }
      this.closeAlert();
    }
  }
}
