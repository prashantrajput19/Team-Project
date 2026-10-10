import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginIllustrationComponent } from './login-illustration.component';

describe('LoginIllustrationComponent', () => {
  let component: LoginIllustrationComponent;
  let fixture: ComponentFixture<LoginIllustrationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LoginIllustrationComponent]
    });
    fixture = TestBed.createComponent(LoginIllustrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
