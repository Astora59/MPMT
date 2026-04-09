import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ActivatedRoute } from '@angular/router';


describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {},
            params: of({}),
            queryParams: of({})
          }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit if form is invalid', () => {
    const fakeForm = { invalid: true } as NgForm;

    component.onSubmit(fakeForm);

    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should register successfully and redirect to login', () => {
    const fakeForm = { invalid: false } as NgForm;

    component.form = {
      username: 'john',
      email: 'john@mail.com',
      password: '123456'
    };

    authServiceSpy.register.and.returnValue(of({}));

    component.onSubmit(fakeForm);

    expect(authServiceSpy.register)
      .toHaveBeenCalledWith(component.form);

    expect(router.navigate)
      .toHaveBeenCalledWith(['/login']);
  });

  it('should handle registration error', () => {
    const fakeForm = { invalid: false } as NgForm;

    spyOn(console, 'error');

    authServiceSpy.register.and.returnValue(
      throwError(() => new Error('Register failed'))
    );

    component.onSubmit(fakeForm);

    expect(console.error).toHaveBeenCalled();
  });
});