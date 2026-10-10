import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Architect } from '../models/architect.model';
import { environment } from 'src/enviornments/enviornment';

@Injectable({
  providedIn: 'root'
})
export class ArchitectService {
  private apiUrl = `${environment.apiUrl}/api/architects`;

  constructor(private http: HttpClient) { }

  getAllArchitects(): Observable<Architect[]> {
    return this.http.get<Architect[]>(this.apiUrl);
  }

  getTopRated(): Observable<Architect[]> {
    return this.http.get<Architect[]>(`${this.apiUrl}/top-rated`);
  }

  getLocations(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/locations`);
  }

  getArchitectById(architectId: number): Observable<Architect> {
    return this.http.get<Architect>(`${this.apiUrl}/${architectId}`);
  }

  addArchitect(architect: Architect): Observable<Architect> {
    return this.http.post<Architect>(this.apiUrl, architect);
  }

  updateArchitect(architectId: number, architect: Architect): Observable<Architect> {
    return this.http.put<Architect>(`${this.apiUrl}/${architectId}`, architect);
  }

  deleteArchitect(architectId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${architectId}`);
  }
}
