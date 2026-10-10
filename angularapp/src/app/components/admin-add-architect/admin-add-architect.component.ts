import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { ArchitectService } from '../../services/architect.service';
import { describeError } from '../../error-message';

@Component({
  selector: 'app-admin-add-architect',
  templateUrl: './admin-add-architect.component.html',
  styleUrls: ['./admin-add-architect.component.css']
})
export class AdminAddArchitectComponent {
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

  isSaving = false;

  // Success / error popup
  alert = { show: false, type: 'success' as 'success' | 'error', title: '', message: '' };

  constructor(private architectService: ArchitectService, private router: Router) { }

  save(): void {
    this.isSaving = true;

    this.architectService.addArchitect(this.architect).subscribe({
      next: () => {
        this.isSaving = false;
        this.showAlert('success', 'Success', 'Architect added successfully!');
      },
      error: (error) => {
        this.isSaving = false;
        this.showAlert('error', 'Failed', describeError(error, 'Failed to add Architect!'));
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
