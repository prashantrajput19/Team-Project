import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-design-art',
  templateUrl: './design-art.component.html',
  styleUrls: ['./design-art.component.css']
})
export class DesignArtComponent {
  @Input() kind = 'residential';
}