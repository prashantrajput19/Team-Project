import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminAddArchitectComponent } from './admin-add-architect.component';

describe('AdminAddArchitectComponent', () => {
  let component: AdminAddArchitectComponent;
  let fixture: ComponentFixture<AdminAddArchitectComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminAddArchitectComponent]
    });
    fixture = TestBed.createComponent(AdminAddArchitectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
