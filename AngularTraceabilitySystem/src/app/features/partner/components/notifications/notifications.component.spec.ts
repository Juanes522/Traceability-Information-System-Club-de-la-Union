import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { NotificationsComponent } from './notifications.component';
import { PartnerService } from '../../partner.service';

describe('NotificationsComponent', () => {
  let component: NotificationsComponent;
  let fixture: ComponentFixture<NotificationsComponent>;

  beforeEach(async () => {
    const partnerServiceSpy = jasmine.createSpyObj('PartnerService', ['getNotifications']);
    partnerServiceSpy.getNotifications.and.returnValue(of({ content: [], totalElements: 0, number: 0, size: 10 }));

    await TestBed.configureTestingModule({
      imports: [NotificationsComponent],
      providers: [
        { provide: PartnerService, useValue: partnerServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
