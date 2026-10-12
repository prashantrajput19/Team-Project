import { Component } from '@angular/core';

// One person on the About us page.
// photo: path inside src/assets (e.g. 'assets/team/prashant.jpg'). Leave '' to show the first letter instead.
interface Person {
  name: string;
  role: string;
  photo: string;
  linkedin: string;
  about: string;
  skills: string[];
}

@Component({
  selector: 'app-about-us',
  templateUrl: './about-us.component.html',
  styleUrls: ['./about-us.component.css']
})
export class AboutUsComponent {
  teamName = 'Spring Warriors';

  // Subject Matter Expert (mentor of the team)
  sme: Person = {
    name: 'Swaminathan S',
    role: 'Specialist - Cloud Engineering @ LTM Mindtree',
    photo: 'assets/team/swaminathan.png',
    linkedin: 'https://www.linkedin.com/in/swaminathan-s-93011b198',
    about: 'Our Subject Matter Expert from Tiruchirappalli, Tamil Nadu. He guided us on how to plan and implement every feature, '
      + 'shared best practices and clean code tips, and helped us from the first idea to the final demo.',
    skills: ['SolarWinds Implementation', 'Observability', 'AIOps', 'Network Monitoring', 'Troubleshooting',
      'NPM', 'NCM', 'SAM', 'NTA', 'WPM']
  };

  // Team lead
  lead: Person = {
    name: 'Prashant',
    role: 'Team Lead',
    photo: 'assets/team/prashant.png',
    linkedin: '',
    about: 'Leads planning, code reviews and releases, and keeps the frontend and backend working together.',
    skills: ['Angular', 'Spring Boot', 'MySQL', 'Git']
  };

  // Team members
  members: Person[] = [
    {
      name: 'Aman', role: 'Full-stack Developer', photo: 'assets/team/aman.png', linkedin: '',
      about: 'Built the architect listing, search and filter features.',
      skills: ['Angular', 'TypeScript', 'REST APIs']
    },
    {
      name: 'Aakanksha', role: 'Full-stack Developer', photo: 'assets/team/aakanksha.png', linkedin: '',
      about: 'Designed clean, responsive pages and the light / dark theme.',
      skills: ['HTML', 'CSS', 'UI Design']
    },
    {
      name: 'Siddarth', role: 'Full-stack Developer', photo: 'assets/team/siddarth.png', linkedin: '',
      about: 'Worked on login, JWT security and OTP verification.',
      skills: ['Spring Security', 'JWT', 'Java']
    },
    {
      name: 'Gowtam', role: 'Full-stack Developer', photo: 'assets/team/gowtam.png', linkedin: '',
      about: 'Created the inquiry and follow-up message flow.',
      skills: ['Spring Boot', 'JPA', 'MySQL']
    },
    {
      name: 'Pritam', role: 'Full-stack Developer', photo: 'assets/team/pritam.png', linkedin: '',
      about: 'Built the feedback, ratings and admin dashboard charts.',
      skills: ['Angular', 'Charts', 'RxJS']
    },
    {
      name: 'Sarayu', role: 'Full-stack Developer', photo: 'assets/team/sarayu.png', linkedin: '',
      about: 'Handled testing, bug fixing and making every page work well.',
      skills: ['Testing', 'Debugging', 'Postman']
    }
  ];

  // Everyone under the mentor: team lead first, then the members
  get team(): Person[] {
    return [this.lead, ...this.members];
  }
}
