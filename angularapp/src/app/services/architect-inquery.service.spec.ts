import { TestBed } from '@angular/core/testing';

import { ArchitectInquiryService } from './architect-inquery.service';

describe('ArchitectInquiryService', () => {
  let service: ArchitectInquiryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchitectInquiryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
