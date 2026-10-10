import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchitectAvatarComponent } from './architect-avatar.component';

describe('ArchitectAvatarComponent', () => {
  let component: ArchitectAvatarComponent;
  let fixture: ComponentFixture<ArchitectAvatarComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ArchitectAvatarComponent]
    });
    fixture = TestBed.createComponent(ArchitectAvatarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
