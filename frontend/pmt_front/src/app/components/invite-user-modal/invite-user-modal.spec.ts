
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { InviteUserModalComponent } from './invite-user-modal';
import { ProjectService } from '../../services/project-service';

describe('InviteUserModalComponent', () => {
  let component: InviteUserModalComponent;
  let fixture: ComponentFixture<InviteUserModalComponent>;
  let projectServiceSpy: jasmine.SpyObj<ProjectService>;

  beforeEach(async () => {
    projectServiceSpy = jasmine.createSpyObj<ProjectService>('ProjectService', ['inviteUser']);

    await TestBed.configureTestingModule({
      imports: [InviteUserModalComponent],
      providers: [
        provideRouter([]),
        { provide: ProjectService, useValue: projectServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InviteUserModalComponent);
    component = fixture.componentInstance;
    component.projectId = 'project-1';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit close when onClose is called', () => {
    spyOn(component.close, 'emit');

    component.onClose();

    expect(component.close.emit).toHaveBeenCalled();
  });

  it('should not call the service when the email is empty', () => {
    component.email = '';

    component.inviteUser();

    expect(projectServiceSpy.inviteUser).not.toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  });

  it('should invite the user, emit invited and close on success', () => {
    projectServiceSpy.inviteUser.and.returnValue(of(undefined as any));
    spyOn(component.invited, 'emit');
    spyOn(component.close, 'emit');
    component.email = 'new.member@example.com';

    component.inviteUser();

    expect(projectServiceSpy.inviteUser).toHaveBeenCalledWith('project-1', 'new.member@example.com');
    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('');
    expect(component.invited.emit).toHaveBeenCalled();
    expect(component.close.emit).toHaveBeenCalled();
  });

  it('should display an error message when the invitation fails', () => {
    projectServiceSpy.inviteUser.and.returnValue(throwError(() => new Error('boom')));
    spyOn(console, 'error');
    spyOn(component.invited, 'emit');
    spyOn(component.close, 'emit');
    component.email = 'unknown@example.com';

    component.inviteUser();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe("Impossible d'inviter cet utilisateur.");
    expect(component.invited.emit).not.toHaveBeenCalled();
    expect(component.close.emit).not.toHaveBeenCalled();
    expect(console.error).toHaveBeenCalled();
  });
});