import { TestBed } from '@angular/core/testing';
import { CoreModule, initAuth } from './core.module';
import { AuthService } from './services/auth.service';

describe('CoreModule', () => {

  it('forRoot() devuelve los providers correctos', () => {
    const result = CoreModule.forRoot();
    expect(result.ngModule).toBe(CoreModule);
    expect(result.providers!.length).toBeGreaterThan(0);
  });

  it('initAuth devuelve una función que llama a authService.init()', () => {
    const authService = jasmine.createSpyObj<AuthService>('AuthService', ['init']);
    const fn = initAuth(authService);
    fn();
    expect(authService.init).toHaveBeenCalledOnceWith();
  });

  it('constructor lanza error si CoreModule ya fue importado', () => {
    const parentModule = new CoreModule(null as any);
    expect(() => new CoreModule(parentModule)).toThrowError(
      'CoreModule already loaded. Import only in AppModule.'
    );
  });

});
