import { Component, ElementRef, HostListener, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import { AiDraft, AiMessage, AiService } from '../../services/ai.service';
import { AuthService } from '../../services/auth.service';
import { ArchitectInquiryService } from '../../services/architect-inquery.service';
import { FeedbackService } from '../../services/feedback.service';
import { describeError } from '../../error-message';

// "Archie" - the floating AI assistant shown on every page after login
@Component({
  selector: 'app-ai-assistant',
  templateUrl: './ai-assistant.component.html',
  styleUrls: ['./ai-assistant.component.css']
})
export class AiAssistantComponent implements OnInit {
  @ViewChild('chatBody') chatBody?: ElementRef<HTMLDivElement>;

  isOpen = false;
  enabled = false;
  isSending = false;
  question = '';

  categoryOptions = ['Service', 'Design', 'Communication', 'Pricing', 'Timeliness'];
  priorityOptions = ['Low', 'Medium', 'High'];

  userPrompts = [
    'Suggest an architect for a modern home',
    'Who are the top rated architects?',
    'What is the status of my inquiries?',
    'Help me give feedback on my last inquiry'
  ];

  adminPrompts = [
    'Show the top 5 architects by rating',
    'Summarise the feedback for each architect',
    'Which inquiries should I handle first?',
    'Which architect gets the most complaints?'
  ];

  constructor(
    private aiService: AiService,
    private authService: AuthService,
    private inquiryService: ArchitectInquiryService,
    private feedbackService: FeedbackService,
    private router: Router
  ) { }

  ngOnInit(): void {
    if (this.aiService.ownerId !== this.authService.getUserId()) {
      this.aiService.messages = [];
      this.aiService.ownerId = this.authService.getUserId();
    }

    this.aiService.getStatus().subscribe({
      next: (status) => this.enabled = status.enabled,
      error: () => this.enabled = false
    });
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get messages(): AiMessage[] {
    return this.aiService.messages;
  }

  get prompts(): string[] {
    return this.isAdmin ? this.adminPrompts : this.userPrompts;
  }

  get greeting(): string {
    const name = this.authService.getUserName();
    if (this.isAdmin) {
      return `Hi ${name}! I'm Archie. Ask me for the top architects, a summary of feedback, `
        + `which inquiries need attention, or let me draft a reply to a user.`;
    }
    return `Hi ${name}! I'm Archie. Tell me about your project and I'll suggest architects. `
      + `I can also write an inquiry, a follow-up question or feedback for you.`;
  }

  toggle(): void {
    this.isOpen = !this.isOpen;
    this.scrollDown();
  }

  // Escape key closes the chat
  @HostListener('document:keydown.escape')
  close(): void {
    this.isOpen = false;
  }

  newChat(): void {
    this.aiService.messages = [];
  }

  askPrompt(text: string): void {
    this.question = text;
    this.send();
  }

  // Enter sends the message, Shift+Enter makes a new line
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  send(): void {
    const text = this.question.trim();
    if (!text || this.isSending || !this.enabled) {
      return;
    }

    this.messages.push({ role: 'user', content: text });
    this.question = '';
    this.isSending = true;
    this.scrollDown();

    // Send only role + content of the earlier messages (not errors)
    const history = this.messages
      .filter(m => !m.error)
      .map(m => ({ role: m.role, content: m.content }));

    this.aiService.chat(history).subscribe({
      next: (response) => {
        const draft = response.draft;
        if (draft) {
          draft.state = 'ready';
        }
        this.messages.push({ role: 'assistant', content: response.reply, draft });
        this.isSending = false;
        this.scrollDown();
      },
      error: (error) => {
        this.messages.push({
          role: 'assistant',
          content: describeError(error, 'Archie could not answer. Please try again.'),
          error: true
        });
        this.isSending = false;
        this.scrollDown();
      }
    });
  }

  // ---------- Drafts prepared by Archie ----------

  draftLabel(draft: AiDraft): string {
    if (draft.type === 'inquiry') return 'New inquiry';
    if (draft.type === 'followup') return 'Follow-up';
    if (draft.type === 'feedback') return 'Feedback';
    return 'Reply to user';
  }

  canSend(draft: AiDraft): boolean {
    if (draft.text.trim().length < 10 || draft.state !== 'ready') {
      return false;
    }
    if (draft.type === 'feedback' && !draft.rating) {
      return false;
    }
    if (draft.type === 'inquiry' && !/^[6-9][0-9]{9}$/.test(draft.contactDetails || '')) {
      return false;
    }
    return true;
  }

  // Sends the draft using the same services the normal forms use
  sendDraft(draft: AiDraft): void {
    draft.state = 'sending';
    draft.error = '';

    const done = {
      next: () => {
        draft.state = 'sent';
        this.scrollDown();
      },
      error: (error: any) => {
        draft.state = 'ready';
        draft.error = describeError(error, 'Could not send. Please try again.');
      }
    };

    if (draft.type === 'inquiry') {
      this.inquiryService.addInquiry({
        user: { userId: this.authService.getUserId() },
        architect: { architectId: draft.architectId },
        message: draft.text,
        priority: draft.priority || 'Medium',
        contactDetails: draft.contactDetails,
        status: 'Pending'
      }).subscribe(done);
    } else if (draft.type === 'feedback') {
      this.feedbackService.sendFeedback({
        feedbackText: draft.text,
        date: new Date().toISOString().substring(0, 10),
        category: draft.category || 'Service',
        rating: draft.rating,
        user: { userId: this.authService.getUserId() },
        architect: { architectId: draft.architectId },
        inquiryId: draft.inquiryId
      }).subscribe(done);
    } else {
      // "followup" (from user) and "reply" (from admin) are both sub-inquiries
      this.inquiryService.addSubInquiry(draft.inquiryId, draft.text).subscribe(done);
    }
  }

  // Opens the page where the sent item can be seen
  openSent(draft: AiDraft): void {
    this.isOpen = false;
    if (draft.type === 'feedback') {
      this.router.navigate(['/user/feedback']);
    } else if (draft.type === 'reply') {
      this.router.navigate(['/admin/inquiries']);
    } else {
      this.router.navigate(['/user/inquiries']);
    }
  }

  private scrollDown(): void {
    // Wait for Angular to draw the new message, then scroll to the bottom
    setTimeout(() => {
      if (this.chatBody) {
        this.chatBody.nativeElement.scrollTop = this.chatBody.nativeElement.scrollHeight;
      }
    });
  }
}
