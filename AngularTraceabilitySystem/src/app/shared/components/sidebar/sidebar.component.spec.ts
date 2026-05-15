import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BehaviorSubject } from 'rxjs';
import { SidebarComponent } from './sidebar.component';
import { AuthService } from '../../../core/services/auth.service';
import { UserSession } from '../../models/index';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let role$: BehaviorSubject<string | null>;
  let currentUser$: BehaviorSubject<UserSession | null>;

  const mockSession: UserSession = {
    token: 'tok',
    role: 'ROLE_PARTNER',
    needsPasswordChange: false,
  };

  beforeEach(async () => {
    role$ = new BehaviorSubject<string | null>(null);
    currentUser$ = new BehaviorSubject<UserSession | null>(null);

    await TestBed.configureTestingModule({
    imports: [RouterTestingModule, SidebarComponent],
    providers: [
        {
            provide: AuthService,
            useValue: { role$, currentUser$, logout: jasmine.createSpy('logout') },
        },
    ],
}).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('carga navItems cuando el rol es ROLE_PARTNER', () => {
    role$.next('ROLE_PARTNER');
    expect(component.navItems.length).toBeGreaterThan(0);
  });

  it('navItems vacío cuando rol es null', () => {
    role$.next(null);
    expect(component.navItems).toEqual([]);
  });

  it('llama a authService.logout() al hacer clic en cerrar sesión', () => {
    const authService = TestBed.inject(AuthService) as any;
    component.logout();
    expect(authService.logout).toHaveBeenCalled();
  });

  it('completa destroy$ al destruir el componente', () => {
    spyOn(component['destroy$'], 'next');
    spyOn(component['destroy$'], 'complete');
    component.ngOnDestroy();
    expect(component['destroy$'].next).toHaveBeenCalled();
    expect(component['destroy$'].complete).toHaveBeenCalled();
  });
});
