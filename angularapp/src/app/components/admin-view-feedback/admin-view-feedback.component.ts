import { Component, OnInit } from '@angular/core';

import { Feedback } from '../../models/feedback.model';
import { FeedbackService } from '../../services/feedback.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-admin-view-feedback',
  templateUrl: './admin-view-feedback.component.html',
  styleUrls: ['./admin-view-feedback.component.css']
})
export class AdminViewFeedbackComponent implements OnInit {
  feedbacks: Feedback[] = [];
  isLoading = false;
  errorMessage = '';

  // Filters and sorting
  search = '';
  categoryFilter = '';
  sortBy = '';

  constructor(private feedbackService: FeedbackService) { }

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  loadFeedbacks(): void {
    this.errorMessage = '';
    this.isLoading = true;

    this.feedbackService.getFeedbacks().subscribe({
      next: (feedbacks) => {
        this.isLoading = false;
        this.feedbacks = feedbacks || [];
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve feedback');
      }
    });
  }

  get categoryOptions(): string[] {
    const categories = this.feedbacks.map(f => f.category).filter(c => !!c);
    return Array.from(new Set(categories)).sort();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.categoryFilter || this.sortBy);
  }

  get filteredFeedbacks(): Feedback[] {
    const search = this.search.trim().toLowerCase();

    const list = this.feedbacks.filter(feedback =>
      (!search ||
        (feedback.feedbackText || '').toLowerCase().includes(search) ||
        this.architectName(feedback).toLowerCase().includes(search)) &&
      (!this.categoryFilter || feedback.category === this.categoryFilter)
    );

    if (this.sortBy === 'high') {
      list.sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0));
    } else if (this.sortBy === 'low') {
      list.sort((a, b) => (a.rating ?? 0) - (b.rating ?? 0));
    }
    return list;
  }

  clearFilters(): void {
    this.search = '';
    this.categoryFilter = '';
    this.sortBy = '';
  }

  architectName(feedback: Feedback): string {
    return feedback.architect?.name ?? '';
  }

  userName(feedback: Feedback): string {
    return feedback.user?.username ?? '';
  }
}
