import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordIllustrationComponent } from './password-illustration.component';

describe('PasswordIllustrationComponent', () => {
  let component: PasswordIllustrationComponent;
  let fixture: ComponentFixture<PasswordIllustrationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PasswordIllustrationComponent]
    });
    fixture = TestBed.createComponent(PasswordIllustrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
