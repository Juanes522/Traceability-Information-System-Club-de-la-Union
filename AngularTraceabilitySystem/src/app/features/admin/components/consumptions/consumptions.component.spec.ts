import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsumptionsComponent } from './consumptions.component';

describe('ConsumptionsComponent', () => {
  let component: ConsumptionsComponent;
  let fixture: ComponentFixture<ConsumptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConsumptionsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsumptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
