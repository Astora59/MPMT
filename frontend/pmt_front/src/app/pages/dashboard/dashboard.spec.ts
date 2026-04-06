import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard';
import { ProjectService } from '../../services/project-service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let projectServiceSpy: jasmine.SpyObj<ProjectService>;

  beforeEach(async () => {
    projectServiceSpy = jasmine.createSpyObj('ProjectService', [
      'getMyProjects'
    ]);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: ProjectService, useValue: projectServiceSpy },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {
    spyOn(localStorage, 'getItem').and.returnValue('1');

    projectServiceSpy.getMyProjects.and.returnValue(
      of([
        {
          project_id: '1',
          project_name: 'PMT',
          project_description: 'Project Test'
        }
      ] as any)
    );

    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load user id from localStorage', () => {
    expect(localStorage.getItem)
      .toHaveBeenCalledWith('userId');

    expect(component.currentUserId).toBe('1');
  });

  it('should load projects on init', () => {
    expect(projectServiceSpy.getMyProjects)
      .toHaveBeenCalled();

    expect(component.projects.length).toBe(1);
  });

  it('should set loading false after success', () => {
    component.loadProjects();

    expect(component.loading).toBeFalse();
  });

  it('should handle project loading error', () => {
    spyOn(console, 'error');

    projectServiceSpy.getMyProjects.and.returnValue(
      throwError(() => new Error('Server error'))
    );

    component.loadProjects();

    expect(component.loading).toBeFalse();
    expect(console.error).toHaveBeenCalled();
  });

  it('should open create modal', () => {
    component.openModal();

    expect(component.showModal).toBeTrue();
  });

  it('should close create modal and reload projects', () => {
    spyOn(component, 'loadProjects');

    component.closeModal();

    expect(component.showModal).toBeFalse();
    expect(component.loadProjects).toHaveBeenCalled();
  });

  it('should open invite modal', () => {
    component.openInviteModal('10');

    expect(component.selectedProjectId).toBe('10');
    expect(component.showInviteModal).toBeTrue();
  });

  it('should close invite modal', () => {
    component.selectedProjectId = '10';
    component.showInviteModal = true;

    component.closeInviteModal();

    expect(component.selectedProjectId).toBeNull();
    expect(component.showInviteModal).toBeFalse();
  });
});