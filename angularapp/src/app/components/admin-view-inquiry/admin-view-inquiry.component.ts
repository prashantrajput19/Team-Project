import { Component, OnInit } from '@angular/core';

import { ArchitectInquiry } from '../../models/architect-inquiry.model';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-admin-view-inquiry',
  templateUrl: './admin-view-inquiry.component.html',
  styleUrls: ['./admin-view-inquiry.component.css']
})
export class AdminViewInquiryComponent implements OnInit {
  inquiries: ArchitectInquiry[] = [];
  isLoading = false;
  errorMessage = '';

  // Filters
  search = '';
  statusFilter = '';
  priorityFilter = '';

  statusOptions = ['Pending', 'In Progress', 'Resolved', 'Closed'];

  // Reply text typed under each inquiry, keyed by inquiryId.
  drafts: { [inquiryId: number]: string } = {};

  // "Update Status" popup
  inquiryToUpdate?: ArchitectInquiry;
  newStatus = '';
  showStatusModal = false;
  isUpdating = false;

  constructor(private inquiryService: ArchitectInquiryService) { }

  ngOnInit(): void {
    this.loadInquiries();
  }

  loadInquiries(): void {
    this.errorMessage = '';
    this.isLoading = true;

    this.inquiryService.getAllInquiries().subscribe({
      next: (inquiries) => {
        this.isLoading = false;
        this.inquiries = inquiries || [];
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve inquiries');
      }
    });
  }

  get priorityOptions(): string[] {
    const priorities = this.inquiries.map(i => i.priority).filter(p => !!p);
    return Array.from(new Set(priorities)).sort();
  }

  get hasFilters(): boolean {
    return !!(this.search || this.statusFilter || this.priorityFilter);
  }

  get filteredInquiries(): ArchitectInquiry[] {
    const search = this.search.trim().toLowerCase();

    return this.inquiries.filter(inquiry =>
      (!search ||
        (inquiry.message || '').toLowerCase().includes(search) ||
        (inquiry.architect?.name || '').toLowerCase().includes(search) ||
        (inquiry.user?.username || '').toLowerCase().includes(search)) &&
      (!this.statusFilter || inquiry.status === this.statusFilter) &&
      (!this.priorityFilter || inquiry.priority === this.priorityFilter)
    );
  }

  clearFilters(): void {
    this.search = '';
    this.statusFilter = '';
    this.priorityFilter = '';
  }

  // The reply area is shown unless the inquiry is closed with no messages.
  hasDetail(inquiry: ArchitectInquiry): boolean {
    return inquiry.status !== 'Closed' || !!inquiry.subInquiries?.length;
  }

  sendReply(inquiry: ArchitectInquiry): void {
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
        this.errorMessage = describeError(error, 'Failed to send your reply.');
      }
    });
  }

  openStatusModal(inquiry: ArchitectInquiry): void {
    this.inquiryToUpdate = inquiry;
    this.newStatus = inquiry.status ?? 'Pending';
    this.showStatusModal = true;
  }

  closeStatusModal(): void {
    this.showStatusModal = false;
    this.inquiryToUpdate = undefined;
  }

  confirmStatusUpdate(): void {
    const inquiry = this.inquiryToUpdate;

    if (!inquiry || inquiry.inquiryId === undefined) {
      return;
    }

    this.isUpdating = true;

    this.inquiryService.updateInquiry(inquiry.inquiryId, { ...inquiry, status: this.newStatus }).subscribe({
      next: () => {
        this.isUpdating = false;
        this.closeStatusModal();
        this.loadInquiries();
      },
      error: (error) => {
        this.isUpdating = false;
        this.closeStatusModal();
        this.errorMessage = describeError(error, 'Failed to update the inquiry!');
      }
    });
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && !this.isUpdating) {
      this.closeStatusModal();
    }
  }

  // "In Progress" -> "inprogress", used as a CSS class name.
  badgeClass(value: string | undefined): string {
    return (value || '').toLowerCase().replace(/\s+/g, '');
  }
}
