import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccessLogComponent } from './access-log.component';

describe('AccessLogComponent', () => {
  let component: AccessLogComponent;
  let fixture: ComponentFixture<AccessLogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AccessLogComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(AccessLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
