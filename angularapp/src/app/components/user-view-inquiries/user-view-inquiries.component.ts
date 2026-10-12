import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ArchitectInquiry } from '../../models/architect-inquiry.model';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { AuthService } from '../../services/auth.service';
import { FeedbackService } from '../../services/feedback.service';
import { AiService } from '../../services/ai.service';
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

  // The inquiry Archie (AI) is writing a follow-up for right now
  writingId = 0;

  // Stars this user already gave for each inquiry, keyed by inquiryId.
  myRatings: { [inquiryId: number]: number } = {};

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
    private aiService: AiService,
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
    this.errorMessage = '';
    this.isLoading = true;

    this.inquiryService.getInquiriesByUserId(userId).subscribe({
      next: (inquiries) => {
        this.isLoading = false;
        this.inquiries = inquiries.sort((a, b) => b.inquiryId - a.inquiryId); // newest first
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve your inquiries');
      }
    });
  }

  loadMyRatings(): void {
    const userId = this.authService.getUserId();

    this.feedbackService.getAllFeedbacksByUserId(userId).subscribe(feedbacks => {
      for (const feedback of feedbacks) {
        if (feedback.inquiryId && feedback.rating) {
          this.myRatings[feedback.inquiryId] = feedback.rating;
        }
      }
    });
  }

  ratingFor(inquiry: ArchitectInquiry): number {
    return this.myRatings[inquiry.inquiryId] || 0;
  }

  giveFeedback(inquiry: ArchitectInquiry): void {
    this.router.navigate(['/user/feedback/add'], { queryParams: { architectId: inquiry.architect.architectId, inquiryId: inquiry.inquiryId } });
  }

  get statusOptions(): string[] {
    const statuses = this.inquiries.map(i => i.status);
    return Array.from(new Set(statuses)).sort();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.statusFilter || this.inquiryIdFilter);
  }

  get filteredInquiries(): ArchitectInquiry[] {
    const search = this.search.trim().toLowerCase();

    return this.inquiries.filter(inquiry =>
      (!search ||
        inquiry.message.toLowerCase().includes(search) ||
        inquiry.architect.name.toLowerCase().includes(search)) &&
      (!this.statusFilter || inquiry.status === this.statusFilter) &&
      (!this.inquiryIdFilter || inquiry.inquiryId === this.inquiryIdFilter)
    );
  }

  // Pagination: show 5 per page
  page = 1;
  pageSize = 5;

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredInquiries.length / this.pageSize));
  }

  // If filters make the list shorter, never stay on a page that no longer exists
  get currentPage(): number {
    return Math.min(this.page, this.totalPages);
  }

  get pagedInquiries(): ArchitectInquiry[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredInquiries.slice(start, start + this.pageSize);
  }

  // Change page and jump back to the top of the page
  goToPage(page: number): void {
    this.page = page;
    window.scrollTo(0, 0);
  }

  clearFilters(): void {
    this.search = '';
    this.statusFilter = '';
    this.inquiryIdFilter = 0;
  }

  // Archie (AI) writes a follow-up question (uses what the user typed as notes)
  writeWithAi(inquiry: ArchitectInquiry): void {
    const id = inquiry.inquiryId;
    this.writingId = id;

    this.aiService.write({ type: 'followup', notes: this.drafts[id] || '', inquiryId: id }).subscribe({
      next: (response) => {
        this.writingId = 0;
        this.drafts[id] = response.text;
      },
      error: (error) => {
        this.writingId = 0;
        this.showAlert('error', 'Archie', describeError(error, 'Archie could not write it. Please try again.'));
      }
    });
  }

  sendSubInquiry(inquiry: ArchitectInquiry): void {
    const id = inquiry.inquiryId;
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
    const inquiryId = this.inquiryToDelete.inquiryId;
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
