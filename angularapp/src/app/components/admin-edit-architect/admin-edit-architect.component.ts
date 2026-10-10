import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { ArchitectService } from '../../services/architect.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-admin-edit-architect',
  templateUrl: './admin-edit-architect.component.html',
  styleUrls: ['./admin-edit-architect.component.css']
})
export class AdminEditArchitectComponent implements OnInit {
  architectId = 0;

  architect: Architect = {
    name: '',
    expertise: '',
    location: '',
    rating: 0,
    portfolioLink: '',
    status: 'Active'
  };

  expertiseOptions = ['Residential', 'Commercial', 'Interior Design', 'Landscape', 'Urban Planning', 'Sustainable Design'];
  statusOptions = ['Active', 'Inactive'];
  urlPattern = 'https?://.+';

  isLoading = false;
  isSaving = false;
  errorMessage = '';

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(
    private architectService: ArchitectService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.architectId = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.architectId) {
      this.errorMessage = 'Architect ID is not available';
      return;
    }

    this.loadArchitect();
  }

  loadArchitect(): void {
    this.isLoading = true;

    this.architectService.getArchitectById(this.architectId).subscribe({
      next: (architect) => {
        this.isLoading = false;
        if (architect) {
          this.architect = architect;
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = describeError(error, 'Unable to load this architect');
      }
    });
  }

  save(): void {
    this.isSaving = true;

    this.architectService.updateArchitect(this.architectId, this.architect).subscribe({
      next: () => {
        this.isSaving = false;
        this.showAlert('success', 'Success', 'Architect updated successfully!');
      },
      error: (error) => {
        this.isSaving = false;
        this.showAlert('error', 'Failed', describeError(error, 'Failed to update Architect!'));
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/architects']);
  }

  showAlert(type: 'success' | 'error', title: string, message: string): void {
    this.alert = { show: true, type, title, message };
  }

  closeAlert(): void {
    this.alert.show = false;
    if (this.alert.type === 'success') {
      this.router.navigate(['/admin/architects']);
    }
  }

  handleModalKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.closeAlert();
    }
  }
}
