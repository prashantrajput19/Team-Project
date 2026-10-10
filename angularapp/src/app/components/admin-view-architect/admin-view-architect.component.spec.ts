import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminViewArchitectComponent } from './admin-view-architect.component';

describe('AdminViewArchitectComponent', () => {
  let component: AdminViewArchitectComponent;
  let fixture: ComponentFixture<AdminViewArchitectComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminViewArchitectComponent]
    });
    fixture = TestBed.createComponent(AdminViewArchitectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
