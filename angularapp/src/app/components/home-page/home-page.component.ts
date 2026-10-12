import { Component, OnInit } from '@angular/core';

import { Architect } from '../../models/architect.model';
import { ArchitectService } from '../../services/architect.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {
  steps = [
    { title: 'Browse architects', text: 'Filter by expertise, city and rating to find the right fit for your project.' },
    { title: 'Send an inquiry', text: 'Pick an architect card and describe your plot, budget and timeline.' },
    { title: 'Follow up', text: 'Ask more questions under the same inquiry and get replies in one thread.' },
    { title: 'Rate the work', text: 'Once it is closed, leave stars and feedback to help the next client.' }
  ];

  expertise = [
    { name: 'Residential', kind: 'residential', text: 'Villas, bungalows and family homes' },
    { name: 'Commercial', kind: 'commercial', text: 'Offices, retail and mixed-use' },
    { name: 'Interior Design', kind: 'interior', text: 'Layouts, materials and lighting' },
    { name: 'Landscape', kind: 'landscape', text: 'Gardens, terraces and courtyards' },
    { name: 'Urban Planning', kind: 'urban', text: 'Townships and public spaces' },
    { name: 'Sustainable Design', kind: 'sustainable', text: 'Net-zero and green buildings' }
  ];

  news = [
    { year: 2025, tag: 'Award', place: 'Chengdu, China', title: 'Liu Jiakun wins the Pritzker Architecture Prize',
      text: 'The Chengdu architect, known for projects like West Village, received the field\'s highest honour.' },
    { year: 2025, tag: 'Event', place: 'Venice, Italy', title: 'Venice Architecture Biennale opens under Carlo Ratti',
      text: 'The 19th edition, "Intelligens. Natural. Artificial. Collective.", asks how buildings can adapt to a changing climate.' },
    { year: 2024, tag: 'Heritage', place: 'Paris, France', title: 'Notre-Dame de Paris reopens after the 2019 fire',
      text: 'Five years of restoration rebuilt the spire and roof using traditional oak carpentry.' },
    { year: 2024, tag: 'Infrastructure', place: 'Mumbai, India', title: 'Atal Setu, India\'s longest sea bridge, opens',
      text: 'The 21.8 km Mumbai Trans Harbour Link now connects South Mumbai with Navi Mumbai.' },
    { year: 2023, tag: 'Civic', place: 'New Delhi, India', title: 'India\'s new Parliament building is inaugurated',
      text: 'Designed by Bimal Patel\'s HCP Design, the triangular building is part of the Central Vista redevelopment.' },
    { year: 2018, tag: 'Award', place: 'Ahmedabad, India', title: 'B. V. Doshi becomes the first Indian Pritzker laureate',
      text: 'Celebrated for low-cost housing such as Aranya in Indore and campuses like IIM Bangalore.' }
  ];

  topArchitects: Architect[] = [];
  architectCount = 0;
  cityCount = 0;
  averageRating = 0;

  testimonials = [
    { name: 'Neha Sharma', city: 'Pune', project: 'Residential', stars: 5,
      quote: 'Found an architect for our 3BHK in two days. The follow-up thread kept every decision in one place.' },
    { name: 'Arjun Mehta', city: 'Bengaluru', project: 'Commercial', stars: 5,
      quote: 'Our office fit-out was planned around the team, not just the floor plate. Smooth from inquiry to handover.' },
    { name: 'Priya Iyer', city: 'Chennai', project: 'Landscape', stars: 4,
      quote: 'The terrace garden turned out better than the renders. Comparing ratings made choosing easy.' },
    { name: 'Rohan Desai', city: 'Mumbai', project: 'Interior Design', stars: 5,
      quote: 'Clear pricing, quick replies and a 2BHK that finally feels open. Would book the same studio again.' },
    { name: 'Kavya Reddy', city: 'Hyderabad', project: 'Sustainable Design', stars: 5,
      quote: 'We wanted a net-zero home and got matched with exactly the right specialist. Bills are almost zero now.' },
    { name: 'Vikram Singh', city: 'Jaipur', project: 'Residential', stars: 4,
      quote: 'Restoring our haveli facade needed care. The architect understood the heritage brief from day one.' }
  ];

  constructor(private authService: AuthService, private architectService: ArchitectService) { }

  ngOnInit(): void {
    this.architectService.getAllArchitects().subscribe(architects => {
      this.architectCount = architects.length;
      this.cityCount = new Set(architects.map(a => a.location)).size;
    });

    // Average rating of the top 3 architects only
    this.architectService.getTopRated().subscribe(top => {
      this.topArchitects = top;
      let total = 0;
      for (const a of this.topArchitects) {
        total = total + a.rating;
      }
      this.averageRating = this.topArchitects.length ? total / this.topArchitects.length : 0;
    });
  }

  get username(): string {
    return this.authService.getUserName();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get architectsRoute(): string {
    return this.isAdmin ? '/admin/architects' : '/user/architects';
  }
}
