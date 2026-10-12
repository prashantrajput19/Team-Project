import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from 'src/enviornments/enviornment';

// One message in the chat with Archie
export interface AiMessage {
  role: 'user' | 'assistant';
  content: string;
  draft?: AiDraft | null;
  error?: boolean;
}

// Something Archie prepared for the user to send (inquiry, follow-up, feedback or admin reply)
export interface AiDraft {
  type: 'inquiry' | 'followup' | 'feedback' | 'reply';
  text: string;
  title: string;
  architectId?: number;
  inquiryId?: number;
  rating?: number;
  category?: string;
  priority?: string;
  contactDetails?: string;
  state?: 'ready' | 'sending' | 'sent' | 'cancelled';
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = `${environment.apiUrl}/api/ai`;

  // The chat is kept here so it is not lost when the user moves between pages
  messages: AiMessage[] = [];

  // Whose chat this is, so a different user who logs in gets a fresh chat
  ownerId: number | null = null;

  constructor(private http: HttpClient) { }

  getStatus(): Observable<{ enabled: boolean }> {
    return this.http.get<{ enabled: boolean }>(`${this.apiUrl}/status`);
  }

  chat(messages: { role: string; content: string }[]): Observable<{ reply: string; draft: AiDraft | null }> {
    return this.http.post<{ reply: string; draft: AiDraft | null }>(`${this.apiUrl}/chat`, { messages });
  }

  // Used by the "Write with AI" buttons on the forms
  write(request: {
    type: 'feedback' | 'inquiry' | 'followup' | 'reply';
    notes: string;
    architectId?: number;
    inquiryId?: number;
    rating?: number;
    category?: string;
  }): Observable<{ text: string }> {
    return this.http.post<{ text: string }>(`${this.apiUrl}/write`, request);
  }
}
