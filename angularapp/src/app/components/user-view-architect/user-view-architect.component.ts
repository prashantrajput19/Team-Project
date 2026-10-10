import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { ArchitectService } from '../../services/architect.service';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-view-architect',
  templateUrl: './user-view-architect.component.html',
  styleUrls: ['./user-view-architect.component.css']
})
export class UserViewArchitectComponent implements OnInit {
  architects: Architect[] = [];
  locations: string[] = [];

  // Filters
  search = '';
  expertiseFilter = '';
  locationFilter = '';

  // Dialog shown when the user already has an open inquiry with the architect
  showOngoingDialog = false;
  ongoingInquiryId?: number;
  selectedArchitectId?: number;

  constructor(
    private architectService: ArchitectService,
    private inquiryService: ArchitectInquiryService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadArchitects();
    this.architectService.getLocations().subscribe(locations => this.locations = locations || []);
  }

  loadArchitects(): void {
    this.architectService.getAllArchitects().subscribe(architects => this.architects = architects || []);
  }

  get hasFilters(): boolean {
    return !!(this.search || this.expertiseFilter || this.locationFilter);
  }

  get filteredArchitects(): Architect[] {
    const search = this.search.trim().toLowerCase();

    return this.architects.filter(architect =>
      (!search ||
        (architect.name || '').toLowerCase().includes(search) ||
        (architect.expertise || '').toLowerCase().includes(search) ||
        (architect.location || '').toLowerCase().includes(search)) &&
      (!this.expertiseFilter || architect.expertise === this.expertiseFilter) &&
      (!this.locationFilter || architect.location === this.locationFilter)
    );
  }

  clearFilters(): void {
    this.search = '';
    this.expertiseFilter = '';
    this.locationFilter = '';
  }

  sendInquiry(architectId: number | undefined): void {
    const userId = this.authService.getUserId();
    if (architectId === undefined || !userId) {
      return;
    }

    // If the user already has an open inquiry with this architect, ask before creating a new one.
    this.inquiryService.getInquiriesByUserId(userId).subscribe(inquiries => {
      const ongoing = (inquiries || []).find(i =>
        i.architect?.architectId === architectId && i.status !== 'Closed');

      if (ongoing) {
        this.ongoingInquiryId = ongoing.inquiryId;
        this.selectedArchitectId = architectId;
        this.showOngoingDialog = true;
      } else {
        this.router.navigate(['/user/inquiry'], { queryParams: { architectId } });
      }
    });
  }

  continueInquiry(): void {
    this.showOngoingDialog = false;
    this.router.navigate(['/user/inquiries'], { queryParams: { inquiryId: this.ongoingInquiryId } });
  }

  raiseNewInquiry(): void {
    this.showOngoingDialog = false;
    this.router.navigate(['/user/inquiry'], { queryParams: { architectId: this.selectedArchitectId } });
  }
}
