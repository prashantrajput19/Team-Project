import { Component, Input } from '@angular/core';

const BACKGROUNDS = ['#f2b544', '#6ea8ff', '#34d399', '#e0724f', '#b56fd1', '#2fa39a'];
const SKIN = ['#f1c7a5', '#d9a37e', '#b97e57', '#8d5a3b'];
const HAIR = ['#2b2118', '#4a3122', '#1b1b1b', '#7a4b2a'];
const SHIRT = ['#1c3f6e', '#2a303b', '#3a4566', '#8f5c31', '#20365c'];

@Component({
  selector: 'app-architect-avatar',
  templateUrl: './architect-avatar.component.html',
  styleUrls: ['./architect-avatar.component.css']
})
export class ArchitectAvatarComponent {
  @Input() seed = 0;
  @Input() size = 48;
  @Input() name = '';

  private pick<T>(list: T[], salt: number): T {
    return list[(this.seed * 7 + salt) % list.length];
  }

  get background(): string { return this.pick(BACKGROUNDS, 0); }
  get skin(): string { return this.pick(SKIN, 1); }
  get hair(): string { return this.pick(HAIR, 2); }
  get shirt(): string { return this.pick(SHIRT, 3); }

  get hardHat(): boolean { return this.seed % 3 === 0; }
  get longHair(): boolean { return !this.hardHat && this.seed % 4 === 1; }
  get glasses(): boolean { return this.seed % 2 === 0; }
}