import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchitectPortfolioComponent } from './architect-portfolio.component';

describe('ArchitectPortfolioComponent', () => {
  let component: ArchitectPortfolioComponent;
  let fixture: ComponentFixture<ArchitectPortfolioComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ArchitectPortfolioComponent]
    });
    fixture = TestBed.createComponent(ArchitectPortfolioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
