import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { ArchitectService } from '../../services/architect.service';
import { FeedbackService } from '../../services/feedback.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-admin-view-architect',
  templateUrl: './admin-view-architect.component.html',
  styleUrls: ['./admin-view-architect.component.css']
})
export class AdminViewArchitectComponent implements OnInit {
  architects: Architect[] = [];
  isLoading = false;
  errorMessage = '';

  // Average star rating that users gave each architect, keyed by architectId.
  userRatings: { [architectId: number]: { average: number; count: number } } = {};

  // Filters and sorting
  search = '';
  expertiseFilter = '';
  statusFilter = '';
  sortBy = '';

  // Delete popup
  architectToDelete?: Architect;
  showDeleteModal = false;
  isDeleting = false;

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(
    private architectService: ArchitectService,
    private feedbackService: FeedbackService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadArchitects();
    this.loadUserRatings();
  }

  loadArchitects(): void {
    this.errorMessage = '';
    this.isLoading = true;

    this.architectService.getAllArchitects().subscribe({
      next: (architects) => {
        this.isLoading = false;
        this.architects = architects;
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to retrieve architects');
      }
    });
  }

  loadUserRatings(): void {
    this.feedbackService.getFeedbacks().subscribe(feedbacks => {
      // First add up the ratings for each architect...
      const totals: { [architectId: number]: { sum: number; count: number } } = {};

      for (const feedback of feedbacks) {
        if (!feedback.rating) {
          continue; // this feedback has no stars
        }
        const id = feedback.architect.architectId;
        totals[id] = totals[id] || { sum: 0, count: 0 };
        totals[id].sum += feedback.rating;
        totals[id].count++;
      }

      // ...then turn each total into an average.
      this.userRatings = {};
      for (const id of Object.keys(totals).map(Number)) {
        this.userRatings[id] = { average: totals[id].sum / totals[id].count, count: totals[id].count };
      }
    });
  }

  get expertiseOptions(): string[] {
    return this.unique(this.architects.map(a => a.expertise));
  }

  get statusOptions(): string[] {
    return this.unique(this.architects.map(a => a.status));
  }

  get hasFilters(): boolean {
    return !!(this.search || this.expertiseFilter || this.statusFilter || this.sortBy);
  }

  get filteredArchitects(): Architect[] {
    const search = this.search.trim().toLowerCase();

    const list = this.architects.filter(architect =>
      (!search ||
        architect.name.toLowerCase().includes(search) ||
        architect.location.toLowerCase().includes(search)) &&
      (!this.expertiseFilter || architect.expertise === this.expertiseFilter) &&
      (!this.statusFilter || architect.status === this.statusFilter)
    );

    const userAverage = (a: Architect) => this.userRatings[a.architectId]?.average ?? 0;
    const isActive = (a: Architect) => (a.status === 'Active' ? 1 : 0);

    switch (this.sortBy) {
      case 'userHigh': return list.sort((a, b) => userAverage(b) - userAverage(a));
      case 'userLow': return list.sort((a, b) => userAverage(a) - userAverage(b));
      case 'ratingHigh': return list.sort((a, b) => b.rating - a.rating);
      case 'ratingLow': return list.sort((a, b) => a.rating - b.rating);
      case 'activeFirst': return list.sort((a, b) => isActive(b) - isActive(a));
      case 'inactiveFirst': return list.sort((a, b) => isActive(a) - isActive(b));
      default: return list;
    }
  }

  // Pagination: show 9 per page
  page = 1;
  pageSize = 9;

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredArchitects.length / this.pageSize));
  }

  // If filters make the list shorter, never stay on a page that no longer exists
  get currentPage(): number {
    return Math.min(this.page, this.totalPages);
  }

  get pagedArchitects(): Architect[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredArchitects.slice(start, start + this.pageSize);
  }

  // Change page and jump back to the top of the page
  goToPage(page: number): void {
    this.page = page;
    window.scrollTo(0, 0);
  }

  clearFilters(): void {
    this.search = '';
    this.expertiseFilter = '';
    this.statusFilter = '';
    this.sortBy = '';
  }

  editArchitect(architectId: number): void {
    this.router.navigate(['/admin/architects/edit', architectId]);
  }

  // Opens the delete popup. Only Inactive architects can be deleted.
  deleteArchitect(architectId: number): void {
    const architect = this.architects.find(a => a.architectId === architectId);

    if (architect.status !== 'Inactive') {
      this.showAlert('error', 'Cannot Delete',
        'Cannot delete an active architect. Mark the architect as Inactive first.');
      return;
    }

    this.architectToDelete = architect;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    const architectId = this.architectToDelete.architectId;
    this.isDeleting = true;

    this.architectService.deleteArchitect(architectId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.showAlert('success', 'Deleted', 'Architect deleted successfully!');
        this.loadArchitects();
      },
      error: (error) => {
        this.isDeleting = false;
        this.closeDeleteModal();
        this.showAlert('error', 'Delete Failed', describeError(error, 'Failed to delete Architect!'));
      }
    });
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.architectToDelete = undefined;
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
  statusClass(status: string | undefined): string {
    return (status || '').toLowerCase().replace(/\s+/g, '');
  }

  private unique(values: string[]): string[] {
    return Array.from(new Set(values.filter(v => !!v))).sort();
  }
}
