import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsumptionListComponent } from './consumption-list.component';

describe('ConsumptionListComponent', () => {
  let component: ConsumptionListComponent;
  let fixture: ComponentFixture<ConsumptionListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConsumptionListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsumptionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería crearse', () => {
    expect(component).toBeTruthy();
  });
});
