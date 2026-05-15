import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProfileComponent } from './profile.component';
import { PartnerService } from '../../partner.service';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;

  beforeEach(async () => {
    const partnerServiceSpy = jasmine.createSpyObj('PartnerService', ['getProfile']);
    partnerServiceSpy.getProfile.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        { provide: PartnerService, useValue: partnerServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
