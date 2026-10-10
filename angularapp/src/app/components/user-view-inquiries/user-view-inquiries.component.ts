import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ArchitectInquiry } from '../../models/architect-inquiry.model';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { AuthService } from '../../services/auth.service';
import { FeedbackService } from '../../services/feedback.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-user-view-inquiries',
  templateUrl: './user-view-inquiries.component.html',
  styleUrls: ['./user-view-inquiries.component.css']
})
export class UserViewInquiriesComponent implements OnInit {
  inquiries: ArchitectInquiry[] = [];
  isLoading = false;
  errorMessage = '';

  // Filters
  search = '';
  statusFilter = '';
  inquiryIdFilter = 0;

  // Follow-up text typed under each inquiry, keyed by inquiryId.
  drafts: { [inquiryId: number]: string } = {};

  // Stars this user already gave each architect, keyed by architectId.
  myRatings: { [architectId: number]: number } = {};

  // Delete popup
  inquiryToDelete?: ArchitectInquiry;
  showDeleteModal = false;
  isDeleting = false;

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(
    private inquiryService: ArchitectInquiryService,
    private feedbackService: FeedbackService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Coming from "continue inquiry" popup: show only that inquiry.
    this.inquiryIdFilter = Number(this.route.snapshot.queryParamMap.get('inquiryId')) || 0;
    this.loadInquiries();
    this.loadMyRatings();
  }

  loadInquiries(): void {
    const userId = this.authService.getUserId();

    if (!userId) {
      this.errorMessage = 'Please login again to see your inquiries';
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    this.inquiryService.getInquiriesByUserId(userId).subscribe({
      next: (inquiries) => {
        this.isLoading = false;
        this.inquiries = inquiries || [];
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve your inquiries');
      }
    });
  }

  loadMyRatings(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      return;
    }

    this.feedbackService.getAllFeedbacksByUserId(userId).subscribe(feedbacks => {
      for (const feedback of feedbacks || []) {
        const architectId = feedback.architect?.architectId;
        if (architectId && feedback.rating) {
          this.myRatings[architectId] = feedback.rating;
        }
      }
    });
  }

  ratingFor(inquiry: ArchitectInquiry): number {
    return this.myRatings[inquiry.architect?.architectId ?? 0] || 0;
  }

  giveFeedback(inquiry: ArchitectInquiry): void {
    this.router.navigate(['/user/feedback/add'], { queryParams: { architectId: inquiry.architect?.architectId } });
  }

  get statusOptions(): string[] {
    const statuses = this.inquiries.map(i => i.status || '').filter(s => !!s);
    return Array.from(new Set(statuses)).sort();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.statusFilter || this.inquiryIdFilter);
  }

  get filteredInquiries(): ArchitectInquiry[] {
    const search = this.search.trim().toLowerCase();

    return this.inquiries.filter(inquiry =>
      (!search ||
        (inquiry.message || '').toLowerCase().includes(search) ||
        (inquiry.architect?.name || '').toLowerCase().includes(search)) &&
      (!this.statusFilter || inquiry.status === this.statusFilter) &&
      (!this.inquiryIdFilter || inquiry.inquiryId === this.inquiryIdFilter)
    );
  }

  clearFilters(): void {
    this.search = '';
    this.statusFilter = '';
    this.inquiryIdFilter = 0;
  }

  sendSubInquiry(inquiry: ArchitectInquiry): void {
    const id = inquiry.inquiryId!;
    const message = (this.drafts[id] || '').trim();

    if (!message) {
      return;
    }

    this.inquiryService.addSubInquiry(id, message).subscribe({
      next: (updated) => {
        this.drafts[id] = '';
        this.inquiries = this.inquiries.map(i => i.inquiryId === id ? updated : i);
      },
      error: (error) => {
        this.showAlert('error', 'Failed', describeError(error, 'Failed to send your follow-up.'));
      }
    });
  }

  // Opens the delete popup.
  deleteInquiry(inquiry: ArchitectInquiry): void {
    this.inquiryToDelete = inquiry;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    const inquiryId = this.inquiryToDelete?.inquiryId;

    if (inquiryId === undefined) {
      return;
    }

    this.isDeleting = true;

    this.inquiryService.deleteInquiry(inquiryId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.showAlert('success', 'Deleted', 'Your inquiry was withdrawn successfully.');
        this.loadInquiries();
      },
      error: (error) => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.showAlert('error', 'Delete Failed', describeError(error, 'Failed to withdraw this inquiry.'));
      }
    });
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.inquiryToDelete = undefined;
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

  // "In Progress" -> "inprogress", used as a CSS class name.
  badgeClass(value: string | undefined): string {
    return (value || '').toLowerCase().replace(/\s+/g, '');
  }
}
