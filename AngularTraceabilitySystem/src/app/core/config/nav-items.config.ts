import { NavItem, UserSession } from '../../shared/models';

type Role = UserSession['role'];

export const NAV_ITEMS: Record<Role, NavItem[]> = {
  ROLE_PARTNER: [
    { label: 'Dashboard',         icon: 'bi-grid-1x2',          route: '/app/partner/dashboard' },
    { label: 'Mi Perfil',         icon: 'bi-person',            route: '/app/partner/profile' },
    { label: 'Consumos',          icon: 'bi-receipt',           route: '/app/partner/consumptions' },
    { label: 'Notificaciones',    icon: 'bi-bell',              route: '/app/partner/notifications' },
    { label: 'Historial Accesos', icon: 'bi-shield-check',      route: '/app/partner/access-log' },
  ],
  ROLE_MANAGER: [
    { label: 'Dashboard',          icon: 'bi-grid-1x2',          route: '/app/manager/dashboard' },
    { label: 'Buscar Socio',       icon: 'bi-person-lines-fill', route: '/app/manager/partner-search' },
    { label: 'Consumos Ambiente',  icon: 'bi-receipt',           route: '/app/manager/consumptions' },
    { label: 'Reportes',          icon: 'bi-file-earmark-pdf', route: '/app/manager/reports' },
  ],
  ROLE_ADMIN: [
    { label: 'Dashboard',          icon: 'bi-grid-1x2',          route: '/app/admin/dashboard' },
    { label: 'Socios',             icon: 'bi-people',            route: '/app/admin/partners' },
    { label: 'Consumos Ambiente',  icon: 'bi-receipt',           route: '/app/admin/consumptions' },
    { label: 'Auditoría',          icon: 'bi-clipboard-data',    route: '/app/admin/audit' },
    { label: 'Reportes',          icon: 'bi-file-earmark-pdf', route: '/app/admin/reports' },
  ],
};
