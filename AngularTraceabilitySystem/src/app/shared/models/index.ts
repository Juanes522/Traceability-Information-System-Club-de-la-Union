export interface UserSession {
  token: string;
  role: 'ROLE_PARTNER' | 'ROLE_MANAGER' | 'ROLE_ADMIN';
  needsPasswordChange: boolean;
}

export interface PartnerProfile {
  personId: number;
  firstName: string;
  secondName: string;
  lastName: string;
  identification: string;
  email: string[];
  shareNumber: number;
  birthDate: string;
  ingressDate: string;
  role: string;
  partnerState: boolean;
  phone: string | null;
  cellPhone: string | null;
  kinship: string | null;
  partnerKind: string;
  gender: string;
  sequence: number;
  forcePasswordChange: boolean | null;
}

export interface Consumption {
  consumptionId: number;
  enviroment: string;
  account: number;
  table: string;
  waiterName: string;
  isPartner: string;
  consumptionValue: number;
  iva: number;
  service: number;
  tip: number;
  consumptionOpening: string;
  consumptionClosing: string | null;
}

export interface ConsumptionValidation {
  id: number;
  presentPartner: boolean;
  answerPartner: boolean;
  validationDate: string;
}

export interface NotificationDTO {
  notificationId: number;
  title: string;
  body: string;
  generationDate: string;
  state: string;
  consumptionId: number;
  environment: string;
  totalAmount: number;
}

export interface AccessLog {
  id: number;
  partnerId: number;
  partnerName: string;
  accessDate: string;
  location: string;
}

export interface SystemUser {
  id: number;
  name: string;
  email: string[];
  role: string;
  active: boolean;
}

export interface ChangePasswordRequest {
  newPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface NavItem {
  label: string;
  icon: string;
  route: string;
}

export interface AuditEvent {
  id: string;
  timestamp: string;
  eventType: string;
  result: string;
  username: string | null;
  ipAddress: string | null;
  detail: string | null;
  targetId: string | null;
  severity: string | null;
}

export interface AuditPage {
  content: AuditEvent[];
  totalElements: number;
  number: number;
  size: number;
}

export interface AuditFilters {
  username?: string;
  eventType?: string;
  result?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export interface PartnerPage {
  content: PartnerProfile[];
  totalElements: number;
  number: number;
  size: number;
}

export interface ConsumptionPage {
  content: Consumption[];
  totalElements: number;
  number: number;
  size: number;
}
