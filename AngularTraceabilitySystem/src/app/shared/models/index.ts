export interface UserSession {
  token: string;
  role: 'ROLE_PARTNER' | 'ROLE_MANAGER' | 'ROLE_ADMIN';
  needsPasswordChange: boolean;
  needsConsent: boolean;
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
  shareNumber?: number;
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

export interface NotificationPage {
  content: NotificationDTO[];
  totalElements: number;
  number: number;
  size: number;
}

export interface LoginHistory {
  timestamp: string;
  ip: string;
}

export interface LoginHistoryPage {
  content: LoginHistory[];
  totalElements: number;
  number: number;
  size: number;
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

export interface ConsumptionSummary {
  totalBilled: number; totalConsumption: number; totalIva: number;
  totalService: number; totalTip: number; chargeCount: number;
  averagePerAccount: number; tipPercentage: number;
}
export interface EnvironmentTotal { environment: string; total: number; count: number; percentage: number; }
export interface TrendPoint { bucket: string; total: number; count: number; }
export interface Comparison {
  currentTotal: number; previousTotal: number; currentCount: number;
  previousCount: number; variancePercentage: number;
}
export interface PeakHeatmapCell { weekday: number; hour: number; total: number; count: number; }
export interface SecuritySummary {
  loginFailedCount: number; rateLimitBlockCount: number;
  accessDeniedCount: number; criticalAlertCount: number; degraded: boolean;
}
export interface MetricsWindow { from?: string; to?: string; }

export interface PartnerMetrics {
  summary: ConsumptionSummary;
  byEnvironment: EnvironmentTotal[];
  trend: TrendPoint[];
  visits: number;
  lastVisit: string | null;
}

export interface AccessSummary { presentNow: number; visits: number; uniquePartners: number; avgFrequency: number; }
export interface EnvironmentOccupancy { environment: string; partners: number; }
export interface AttendancePoint { bucket: string; count: number; }

export interface MonthlySnapshot {
  yearMonth: string; totalBilled: number; chargeCount: number;
  averagePerAccount: number; tipPercentage: number; visits: number; uniquePartners: number;
}

export interface ProductRank { productId: string; name: string; quantity: number; revenue: number; }
export interface CategoryMix { category: string; subcategory: string; quantity: number; revenue: number; percentage: number; }
export interface EnvironmentCategory { environment: string; category: string; quantity: number; revenue: number; }

export interface ConsentPolicy { version: string; title: string; text: string; }
