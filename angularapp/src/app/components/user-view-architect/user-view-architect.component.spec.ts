import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserViewArchitectComponent } from './user-view-architect.component';

describe('UserViewArchitectComponent', () => {
  let component: UserViewArchitectComponent;
  let fixture: ComponentFixture<UserViewArchitectComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UserViewArchitectComponent]
    });
    fixture = TestBed.createComponent(UserViewArchitectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
