import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { ArchitectInquiry } from '../../models/architect-inquiry.model';
import { ArchitectService } from '../../services/architect.service';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { AuthService } from '../../services/auth.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-user-add-inquiry',
  templateUrl: './user-add-inquiry.component.html',
  styleUrls: ['./user-add-inquiry.component.css']
})
export class UserAddInquiryComponent implements OnInit {
  architectId = 0;
  architect?: Architect;

  // Form fields
  message = '';
  priority = 'Medium';
  contactDetails = '';
  priorityOptions = ['Low', 'Medium', 'High'];

  isLoading = false;
  isSaving = false;
  errorMessage = '';

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(
    private architectService: ArchitectService,
    private inquiryService: ArchitectInquiryService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    // The architect is passed in the URL: /user/inquiry?architectId=5
    this.architectId = Number(this.route.snapshot.queryParamMap.get('architectId'));

    if (!this.architectId) {
      this.router.navigate(['/user/architects']);
      return;
    }

    this.loadArchitect();
  }

  loadArchitect(): void {
    this.isLoading = true;

    this.architectService.getAllArchitects().subscribe({
      next: (architects) => {
        this.isLoading = false;
        const found = (architects || []).find(a => a.architectId === this.architectId);

        if (!found) {
          this.errorMessage = 'Architect not found';
        } else if (found.status === 'Inactive') {
          this.errorMessage = `${found.name} is not taking new inquiries right now.`;
        } else {
          this.architect = found;
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to load architects');
      }
    });
  }

  save(): void {
    const userId = this.authService.getUserId();

    if (!userId || !this.architect) {
      this.showAlert('error', 'Failed', 'Please choose an architect from the list and try again.');
      return;
    }

    const inquiry: ArchitectInquiry = {
      user: { userId },
      architect: { architectId: this.architectId },
      message: this.message,
      priority: this.priority,
      contactDetails: this.contactDetails,
      status: 'Pending'
    };

    this.isSaving = true;

    this.inquiryService.addInquiry(inquiry).subscribe({
      next: () => {
        this.isSaving = false;
        this.showAlert('success', 'Inquiry Sent', 'Your inquiry has been successfully submitted.');
      },
      error: (error) => {
        this.isSaving = false;
        this.showAlert('error', 'Failed', describeError(error, 'Failed to submit your inquiry. Please try again.'));
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/user/architects']);
  }

  showAlert(type: 'success' | 'error', title: string, message: string): void {
    this.alert = { show: true, type, title, message };
  }

  closeAlert(): void {
    this.alert.show = false;
    if (this.alert.type === 'success') {
      this.router.navigate(['/user/inquiries']);
    }
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.closeAlert();
    }
  }
}
