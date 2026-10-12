import { Component, OnInit } from '@angular/core';

import { Architect } from '../../models/architect.model';
import { ArchitectInquiry } from '../../models/architect-inquiry.model';
import { Feedback } from '../../models/feedback.model';
import { ArchitectService } from '../../services/architect.service';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { FeedbackService } from '../../services/feedback.service';
import { AuthService } from '../../services/auth.service';
import { ChartBar } from '../bar-chart/bar-chart.component';

interface Slice {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-admin-control-panel',
  templateUrl: './admin-control-panel.component.html',
  styleUrls: ['./admin-control-panel.component.css']
})
export class AdminControlPanelComponent implements OnInit {
  // Real data for the stat cards at the top.
  architects: Architect[] = [];
  inquiries: ArchitectInquiry[] = [];
  feedbacks: Feedback[] = [];

  // Sample data for the charts below.
  inquiryStatus: Slice[] = [
    { label: 'Pending', value: 18, color: '#f2b544' },
    { label: 'In Progress', value: 12, color: '#6ea8ff' },
    { label: 'Resolved', value: 26, color: '#34d399' },
    { label: 'Closed', value: 9, color: '#8b93a1' }
  ];

  expertiseShare: Slice[] = [
    { label: 'Residential', value: 32, color: '#f2b544' },
    { label: 'Commercial', value: 21, color: '#6ea8ff' },
    { label: 'Interior Design', value: 17, color: '#e0724f' },
    { label: 'Landscape', value: 12, color: '#34d399' },
    { label: 'Urban Planning', value: 9, color: '#b56fd1' },
    { label: 'Sustainable Design', value: 9, color: '#2fa39a' }
  ];

  months = ['Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct'];
  monthlyInquiries = [14, 18, 16, 22, 27, 25, 31, 36, 33, 41, 46, 52];

  satisfaction = 86;

  ratingBars: ChartBar[] = [
    { label: '1★', value: 3 },
    { label: '2★', value: 5 },
    { label: '3★', value: 14 },
    { label: '4★', value: 38 },
    { label: '5★', value: 52 }
  ];

  // SVG shapes for the donut, pie and line charts.
  donutSegments = this.donutArcs(this.inquiryStatus);
  pieSlices = this.pieArcs(this.expertiseShare);

  linePoints = this.monthlyInquiries.map((value, i) => ({
    x: 30 + i * (540 / (this.monthlyInquiries.length - 1)),
    y: 190 - (value / Math.max(...this.monthlyInquiries)) * 160,
    value
  }));
  linePath = this.linePoints.map(p => `${p.x},${p.y}`).join(' ');
  areaPath = `M30,190 L${this.linePath} L570,190 Z`;

  constructor(
    private architectService: ArchitectService,
    private inquiryService: ArchitectInquiryService,
    private feedbackService: FeedbackService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.architectService.getAllArchitects().subscribe(architects => this.architects = architects);
    this.inquiryService.getAllInquiries().subscribe(inquiries => this.inquiries = inquiries);
    this.feedbackService.getFeedbacks().subscribe(feedbacks => this.feedbacks = feedbacks);
  }

  get username(): string {
    return this.authService.getUserName();
  }

  get activeArchitects(): number {
    return this.architects.filter(a => a.status === 'Active').length;
  }

  get pendingInquiries(): number {
    return this.inquiries.filter(i => !i.status || i.status === 'Pending').length;
  }

  get inquiryTotal(): number {
    return this.inquiryStatus.reduce((sum, slice) => sum + slice.value, 0);
  }

  // Average of rated architects only; new architects (rating 0) are skipped
  get averageRating(): string {
    const rated = this.architects.filter(a => Number(a.rating) > 0);
    if (rated.length === 0) {
      return '—';
    }
    const total = rated.reduce((sum, a) => sum + Number(a.rating), 0);
    return (total / rated.length).toFixed(1);
  }

  // Builds one SVG path per pie slice (circle of radius 90 centred at 100,100).
  private pieArcs(slices: Slice[]): { slice: Slice; path: string }[] {
    const total = slices.reduce((sum, slice) => sum + slice.value, 0);
    const point = (angle: number) => `${100 + 90 * Math.sin(angle)},${100 - 90 * Math.cos(angle)}`;
    let angle = 0;

    return slices.map(slice => {
      const sweep = (slice.value / total) * 2 * Math.PI;
      const largeArc = sweep > Math.PI ? 1 : 0;
      const path = `M100,100 L${point(angle)} A90,90 0 ${largeArc} 1 ${point(angle + sweep)} Z`;
      angle += sweep;
      return { slice, path };
    });
  }

  // Builds the dash pattern for each donut segment (circle of radius 70).
  private donutArcs(slices: Slice[]): { slice: Slice; dash: string; offset: number }[] {
    const circumference = 2 * Math.PI * 70;
    const total = slices.reduce((sum, slice) => sum + slice.value, 0);
    let start = 0;

    return slices.map(slice => {
      const length = (slice.value / total) * circumference;
      const segment = { slice, dash: `${length} ${circumference - length}`, offset: -start };
      start += length;
      return segment;
    });
  }
}
