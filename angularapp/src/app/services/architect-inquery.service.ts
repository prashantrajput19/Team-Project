import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ArchitectInquiry } from '../models/architect-inquiry.model';
import { environment } from 'src/enviornments/enviornment';

@Injectable({
  providedIn: 'root'
})
export class ArchitectInquiryService {
  private apiUrl = `${environment.apiUrl}/api/inquiries`;

  constructor(private http: HttpClient) { }

  addInquiry(inquiry: ArchitectInquiry): Observable<ArchitectInquiry> {
    return this.http.post<ArchitectInquiry>(this.apiUrl, inquiry);
  }

  getAllInquiries(): Observable<ArchitectInquiry[]> {
    return this.http.get<ArchitectInquiry[]>(this.apiUrl);
  }

  getInquiriesByUserId(userId: number): Observable<ArchitectInquiry[]> {
    return this.http.get<ArchitectInquiry[]>(`${this.apiUrl}/user/${userId}`);
  }

  updateInquiry(inquiryId: number, inquiry: ArchitectInquiry): Observable<ArchitectInquiry> {
    return this.http.put<ArchitectInquiry>(`${this.apiUrl}/${inquiryId}`, inquiry);
  }

  deleteInquiry(inquiryId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${inquiryId}`);
  }

  addSubInquiry(inquiryId: number, message: string): Observable<ArchitectInquiry> {
    return this.http.post<ArchitectInquiry>(`${this.apiUrl}/${inquiryId}/subinquiries`, { message });
  }
}
