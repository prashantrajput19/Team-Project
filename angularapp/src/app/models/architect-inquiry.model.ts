import { User } from './user.model';
import { Architect } from './architect.model';

export interface ArchitectInquiry {
  inquiryId?: number;
  user: Partial<User>;
  architect: Partial<Architect>;
  message: string;
  status?: string;
  priority: string;
  contactDetails?: string;
  subInquiries?: SubInquiry[];
}

export interface SubInquiry {
  subInquiryId?: number;
  message: string;
  createdAt?: string;
  sender?: 'User' | 'Admin';
}
