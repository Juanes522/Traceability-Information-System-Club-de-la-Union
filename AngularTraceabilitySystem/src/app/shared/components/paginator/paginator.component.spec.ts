import { TestBed } from '@angular/core/testing';
import { PaginatorComponent } from './paginator.component';

describe('PaginatorComponent', () => {
  let component: PaginatorComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PaginatorComponent] }).compileComponents();
    component = TestBed.createComponent(PaginatorComponent).componentInstance;
  });

  it('muestra todas las páginas cuando son 7 o menos', () => {
    component.totalPages = 5;
    component.currentPage = 1;
    expect(component.pages).toEqual([1, 2, 3, 4, 5]);
  });

  it('usa ventana con elipsis para muchas páginas (actual en medio)', () => {
    component.totalPages = 20;
    component.currentPage = 10;
    expect(component.pages).toEqual([1, '…', 9, 10, 11, '…', 20]);
  });

  it('sin elipsis inicial cuando la actual está cerca del inicio', () => {
    component.totalPages = 20;
    component.currentPage = 2;
    expect(component.pages).toEqual([1, 2, 3, '…', 20]);
  });

  it('sin elipsis final cuando la actual está en la última página', () => {
    component.totalPages = 20;
    component.currentPage = 20;
    expect(component.pages).toEqual([1, '…', 19, 20]);
  });

  it('emite el número al hacer clic en una página distinta', () => {
    spyOn(component.pageChange, 'emit');
    component.totalPages = 20;
    component.currentPage = 10;
    component.go(11);
    expect(component.pageChange.emit).toHaveBeenCalledWith(11);
  });

  it('no emite al clic en la página actual ni en la elipsis', () => {
    spyOn(component.pageChange, 'emit');
    component.totalPages = 20;
    component.currentPage = 10;
    component.go(10);
    component.go('…');
    expect(component.pageChange.emit).not.toHaveBeenCalled();
  });
});
