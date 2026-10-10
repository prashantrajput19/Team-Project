import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignupIllustrationComponent } from './signup-illustration.component';

describe('SignupIllustrationComponent', () => {
  let component: SignupIllustrationComponent;
  let fixture: ComponentFixture<SignupIllustrationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SignupIllustrationComponent]
    });
    fixture = TestBed.createComponent(SignupIllustrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
