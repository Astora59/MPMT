import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectPage } from './project-page';
import { ActivatedRoute } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { ProjectService } from '../../services/project-service';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('ProjectPage Component', () => {
  let component: ProjectPage;
  let fixture: ComponentFixture<ProjectPage>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let projectServiceSpy: jasmine.SpyObj<ProjectService>;

  beforeEach(async () => {
    taskServiceSpy = jasmine.createSpyObj('TaskService', [
      'getTasksByProject'
    ]);
    providers: [
      provideRouter([])
    ]

    projectServiceSpy = jasmine.createSpyObj('ProjectService', [
      'getProjectMembers',
      'updateUserRole',
      'assignTask',
      'createTask',
      'getTaskHistory',
      'updateTask'
    ]);

    await TestBed.configureTestingModule({
      imports: [ProjectPage],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        },
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: ProjectService, useValue: projectServiceSpy }
      ]
    }).compileComponents();

    taskServiceSpy.getTasksByProject.and.returnValue(of([]));
    projectServiceSpy.getProjectMembers.and.returnValue(of([]));

    fixture = TestBed.createComponent(ProjectPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should load tasks on init', () => {
    expect(taskServiceSpy.getTasksByProject)
      .toHaveBeenCalledWith('1');
  });

  it('should filter tasks correctly', () => {
    const tasks = [
      { taskStatus: 'pending' },
      { taskStatus: 'in_progress' },
      { taskStatus: 'completed' }
    ];

    taskServiceSpy.getTasksByProject.and.returnValue(of(tasks as any));

    component.loadTasks();

    expect(component.pendingTasks.length).toBe(1);
    expect(component.inProgressTasks.length).toBe(1);
    expect(component.completedTasks.length).toBe(1);
  });

  it('should open modal', () => {
    component.openModal();
    expect(component.showModal).toBeTrue();
  });

  it('should close modal', () => {
    component.closeModal();
    expect(component.showModal).toBeFalse();
  });

  it('should enable edit mode', () => {
    component.enableEdit();
    expect(component.editMode).toBeTrue();
  });

  it('should assign task', () => {
    const task = { taskId: 1 };

    projectServiceSpy.assignTask.and.returnValue(
      of({
        assignedUser: {
          username: 'John'
        }
      })
    );

    component.assignTask(task, 'test@mail.com');

    expect(projectServiceSpy.assignTask)
      .toHaveBeenCalled();
  });

  it('should create task', () => {
    projectServiceSpy.createTask.and.returnValue(of({}));

    spyOn(component, 'loadTasks');
    spyOn(component, 'closeModal');

    component.newTask = {
      taskTitle: 'Task test',
      taskDescription: 'Desc',
      taskDeadline: '2026-01-01',
      taskStatus: 'pending',
      taskPriority: 'High'
    };

    component.createTask();

    expect(projectServiceSpy.createTask)
      .toHaveBeenCalled();

    expect(component.loadTasks).toHaveBeenCalled();
    expect(component.closeModal).toHaveBeenCalled();
  });

  it('should update task', () => {
    component.selectedTask = {
      taskId: 1,
      taskTitle: 'Updated',
      taskDescription: 'Desc',
      taskDeadline: '2026-01-01',
      taskStatus: 'completed',
      taskPriority: 'High'
    };

    projectServiceSpy.updateTask.and.returnValue(of({}));
    projectServiceSpy.getTaskHistory.and.returnValue(of([]));

    spyOn(component, 'loadTasks');
    spyOn(component, 'closeTaskModal');

    component.updateTask();

    expect(projectServiceSpy.updateTask)
      .toHaveBeenCalled();

    expect(component.loadTasks).toHaveBeenCalled();
  });
});