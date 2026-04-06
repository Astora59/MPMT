import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Login } from './login';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [Login, FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([])

      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit invalid form', () => {
    const fakeForm = { invalid: true } as NgForm;

    component.onSubmit(fakeForm);

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should login successfully', () => {
    const fakeForm = { invalid: false } as NgForm;

    component.form = {
      email: 'test@mail.com',
      password: '123456'
    };

    authServiceSpy.login.and.returnValue(
      of({
        token: 'fake-token',
        users_id: '1'
      })
    );

    spyOn(localStorage, 'setItem');

    component.onSubmit(fakeForm);

    expect(authServiceSpy.login).toHaveBeenCalled();
    expect(localStorage.setItem).toHaveBeenCalledWith('token', 'fake-token');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should display error on login failure', () => {
    const fakeForm = { invalid: false } as NgForm;

    authServiceSpy.login.and.returnValue(
      throwError(() => new Error('Unauthorized'))
    );

    component.onSubmit(fakeForm);

    expect(component.errorMessage)
      .toBe('Email ou mot de passe incorrect');
  });
});