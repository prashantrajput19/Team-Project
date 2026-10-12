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
  deletedFilter = ''; // '' = all, 'deleted', 'active' = not deleted

  constructor(private feedbackService: FeedbackService) { }

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  loadFeedbacks(): void {
    this.errorMessage = '';
    this.isLoading = true;

    this.feedbackService.getFeedbacksIncludingDeleted().subscribe({
      next: (feedbacks) => {
        this.isLoading = false;
        this.feedbacks = feedbacks.sort((a, b) => b.feedbackId - a.feedbackId); // newest first
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve feedback');
      }
    });
  }

  get categoryOptions(): string[] {
    const categories = this.feedbacks.map(f => f.category);
    return Array.from(new Set(categories)).sort();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.categoryFilter || this.sortBy || this.deletedFilter);
  }

  get filteredFeedbacks(): Feedback[] {
    const search = this.search.trim().toLowerCase();

    const list = this.feedbacks.filter(feedback =>
      (!search ||
        feedback.feedbackText.toLowerCase().includes(search) ||
        this.architectName(feedback).toLowerCase().includes(search)) &&
      (!this.categoryFilter || feedback.category === this.categoryFilter) &&
      (!this.deletedFilter ||
        (this.deletedFilter === 'deleted' && feedback.deleted) ||
        (this.deletedFilter === 'active' && !feedback.deleted))
    );

    if (this.sortBy === 'high') {
      list.sort((a, b) => b.rating - a.rating);
    } else if (this.sortBy === 'low') {
      list.sort((a, b) => a.rating - b.rating);
    }
    return list;
  }

  // Pagination: show 5 per page
  page = 1;
  pageSize = 5;

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredFeedbacks.length / this.pageSize));
  }

  // If filters make the list shorter, never stay on a page that no longer exists
  get currentPage(): number {
    return Math.min(this.page, this.totalPages);
  }

  get pagedFeedbacks(): Feedback[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredFeedbacks.slice(start, start + this.pageSize);
  }

  // Change page and jump back to the top of the page
  goToPage(page: number): void {
    this.page = page;
    window.scrollTo(0, 0);
  }

  clearFilters(): void {
    this.search = '';
    this.categoryFilter = '';
    this.sortBy = '';
    this.deletedFilter = '';
  }

  architectName(feedback: Feedback): string {
    return feedback.architect.name;
  }

  userName(feedback: Feedback): string {
    return feedback.user.username;
  }
}
