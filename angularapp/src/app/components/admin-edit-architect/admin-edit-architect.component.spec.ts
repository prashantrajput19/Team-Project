import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminEditArchitectComponent } from './admin-edit-architect.component';

describe('AdminEditArchitectComponent', () => {
  let component: AdminEditArchitectComponent;
  let fixture: ComponentFixture<AdminEditArchitectComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminEditArchitectComponent]
    });
    fixture = TestBed.createComponent(AdminEditArchitectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
