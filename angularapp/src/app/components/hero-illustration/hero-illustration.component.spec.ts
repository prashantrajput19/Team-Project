import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeroIllustrationComponent } from './hero-illustration.component';

describe('HeroIllustrationComponent', () => {
  let component: HeroIllustrationComponent;
  let fixture: ComponentFixture<HeroIllustrationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HeroIllustrationComponent]
    });
    fixture = TestBed.createComponent(HeroIllustrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
