import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ProjectService } from './project-service';
import { provideHttpClient } from '@angular/common/http';

describe('ProjectService', () => {
  let service: ProjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectService);
    providers: [
  provideHttpClient()
];
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
