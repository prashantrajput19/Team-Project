import { User } from './user.model';
import { Architect } from './architect.model';

export interface Feedback {
  feedbackId?: number;
  feedbackText: string;
  date: string;
  user: Partial<User>;
  architect?: Partial<Architect>;
  category: string;
  rating?: number;
  inquiryId?: number;
  deleted?: boolean;
}
