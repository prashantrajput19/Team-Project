import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Architect } from '../../models/architect.model';
import { ArchitectService } from '../../services/architect.service';
import { AuthService } from '../../services/auth.service';

interface PortfolioProject {
  title: string;
  year: number;
  area: string;
  kind: string;
}

const PROJECT_TITLES: { [expertise: string]: string[] } = {
  'Residential': ['Courtyard Villa', 'Twin Family Homes', 'Hillside Bungalow', 'Row House Renewal'],
  'Commercial': ['Glass Office Tower', 'Co-working Hub', 'Retail Arcade', 'Tech Park Block B'],
  'Interior Design': ['Minimal 2BHK Makeover', 'Boutique Cafe Interior', 'Reading Loft', 'Open-plan Kitchen'],
  'Landscape': ['Terrace Garden', 'Monsoon Courtyard', 'Community Park Walk', 'Rooftop Orchard'],
  'Urban Planning': ['Riverside Township', 'Transit Plaza', 'Market Street Revival', 'Green Corridor'],
  'Sustainable Design': ['Net-zero Family Home', 'Solar Office Pavilion', 'Rainwater Campus', 'Passive-cooled Villa']
};

const IMAGES: { [expertise: string]: string } = {
  'Residential': 'residential',
  'Commercial': 'commercial',
  'Interior Design': 'interior',
  'Landscape': 'landscape',
  'Urban Planning': 'urban',
  'Sustainable Design': 'sustainable'
};

@Component({
  selector: 'app-architect-portfolio',
  templateUrl: './architect-portfolio.component.html',
  styleUrls: ['./architect-portfolio.component.css']
})
export class ArchitectPortfolioComponent implements OnInit {
  architect?: Architect;
  projects: PortfolioProject[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private architectService: ArchitectService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.architectService.getAllArchitects().subscribe({
      next: (architects) => {
        this.isLoading = false;
        this.architect = (architects || []).find(a => a.architectId === id);

        if (this.architect) {
          this.projects = this.buildProjects(this.architect);
        } else {
          this.errorMessage = 'Architect not found';
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Unable to load this portfolio';
      }
    });
  }

  get isUser(): boolean {
    return this.authService.isUser();
  }

  get isActive(): boolean {
    return this.architect?.status === 'Active';
  }

  get yearsOfPractice(): number {
    return 6 + ((this.architect?.architectId ?? 0) % 9);
  }

  get projectsCompleted(): number {
    return 20 + ((this.architect?.architectId ?? 0) * 7) % 40;
  }

  sendInquiry(): void {
    this.router.navigate(['/user/inquiry'], { queryParams: { architectId: this.architect?.architectId } });
  }

  goBack(): void {
    window.history.back();
  }

  private buildProjects(architect: Architect): PortfolioProject[] {
    const titles = PROJECT_TITLES[architect.expertise] ?? PROJECT_TITLES['Residential'];
    const main = IMAGES[architect.expertise] ?? 'residential';
    const others = Object.values(IMAGES).filter(image => image !== main);

    return titles.map((title, i) => ({
      title,
      year: new Date().getFullYear() - i,
      area: `${(1200 + i * 850 + (architect.architectId ?? 0) * 40).toLocaleString('en-IN')} sq ft`,
      kind: i % 2 === 0 ? main : others[i % others.length]
    }));
  }
}