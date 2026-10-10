import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DesignArtComponent } from './design-art.component';

describe('DesignArtComponent', () => {
  let component: DesignArtComponent;
  let fixture: ComponentFixture<DesignArtComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DesignArtComponent]
    });
    fixture = TestBed.createComponent(DesignArtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
