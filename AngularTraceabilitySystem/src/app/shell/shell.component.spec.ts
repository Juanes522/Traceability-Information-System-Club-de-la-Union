import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { ShellComponent } from './shell.component';
import { AuthService } from '../core/services/auth.service';
import { AuthApiService } from '../core/services/auth-api.service';
import { ToastService } from '../core/services/toast.service';
import { PushNotificationService } from '../core/services/push-notification.service';
import { IdleService } from '../core/services/idle.service';
import { of } from 'rxjs';

describe('ShellComponent', () => {
  let component: ShellComponent;
  let fixture: ComponentFixture<ShellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShellComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            role$: new BehaviorSubject(null),
            currentUser$: new BehaviorSubject(null),
            needsPasswordChange$: new BehaviorSubject(false),
            needsConsent$: new BehaviorSubject(false),
            clearNeedsPasswordChange: jasmine.createSpy('clearNeedsPasswordChange'),
            clearNeedsConsent: jasmine.createSpy('clearNeedsConsent'),
            logout: jasmine.createSpy('logout'),
          },
        },
        {
          provide: AuthApiService,
          useValue: {
            changePassword: jasmine.createSpy('changePassword'),
            acceptConsent: jasmine.createSpy('acceptConsent'),
            getConsentPolicy: jasmine.createSpy('getConsentPolicy').and.returnValue(of({ version: '1.0', title: 'T', text: 'X' })),
          },
        },
        {
          provide: ToastService,
          useValue: { success: jasmine.createSpy('success'), error: jasmine.createSpy('error') },
        },
        {
          provide: PushNotificationService,
          useValue: { subscribeToPartnerNotifications: jasmine.createSpy('subscribeToPartnerNotifications') },
        },
        {
          provide: IdleService,
          useValue: jasmine.createSpyObj('IdleService', ['start', 'stop']),
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ShellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });

  it('renderiza app-sidebar en el template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-sidebar')).toBeTruthy();
  });

  it('renderiza router-outlet en el template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });
});
