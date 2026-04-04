import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterConsumptionComponent } from './register-consumption.component';

describe('RegisterConsumptionComponent', () => {
  let component: RegisterConsumptionComponent;
  let fixture: ComponentFixture<RegisterConsumptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RegisterConsumptionComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterConsumptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
