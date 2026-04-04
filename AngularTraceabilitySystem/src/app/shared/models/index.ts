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
  stateAccount: string;
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

export interface Notification {
  id: number;
  message: string;
  sentAt: string;
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

export interface NavItem {
  label: string;
  icon: string;
  route: string;
}
