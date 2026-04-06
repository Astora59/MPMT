import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const API_URL = 'http://localhost:8080/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
      provideRouter([])
    ]
      
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should register a user', () => {
    const mockUser = {
      username: 'john',
      email: 'john@mail.com',
      password: '123456'
    };

    const mockResponse = {
      message: 'User registered successfully'
    };

    service.register(mockUser).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${API_URL}/register`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockUser);

    req.flush(mockResponse);
  });

  it('should login user', () => {
    const loginData = {
      email: 'john@mail.com',
      password: '123456'
    };

    const mockResponse = {
      token: 'fake-jwt-token',
      users_id: '1'
    };

    service.login(loginData).subscribe((response) => {
      expect(response.token).toBe('fake-jwt-token');
      expect(response.users_id).toBe('1');
    });

    const req = httpMock.expectOne(`${API_URL}/login`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginData);

    req.flush(mockResponse);
  });
});