import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AccessLogComponent } from './access-log.component';
import { PartnerService } from '../../partner.service';

describe('AccessLogComponent', () => {
  let component: AccessLogComponent;
  let fixture: ComponentFixture<AccessLogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    imports: [AccessLogComponent, HttpClientTestingModule],
    providers: [PartnerService],
}).compileComponents();
    fixture = TestBed.createComponent(AccessLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
