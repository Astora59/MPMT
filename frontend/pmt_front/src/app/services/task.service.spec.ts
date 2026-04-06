import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';

import { TaskService, Task } from './task.service';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const API_URL = 'http://localhost:8080/projects';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
      provideRouter([])
    ]
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get tasks by project', () => {
    const mockTasks: Task[] = [
      {
        taskId: '1',
        taskTitle: 'Task 1',
        taskDescription: 'Description',
        taskDeadline: '2026-01-01',
        taskStatus: 'pending',
        taskPriority: 'High'
      },
      {
        taskId: '2',
        taskTitle: 'Task 2',
        taskDescription: 'Description 2',
        taskDeadline: '2026-01-02',
        taskStatus: 'completed',
        taskPriority: 'Low'
      }
    ];

    service.getTasksByProject('1').subscribe((tasks) => {
      expect(tasks.length).toBe(2);
      expect(tasks[0].taskTitle).toBe('Task 1');
      expect(tasks[1].taskStatus).toBe('completed');
    });

    const req = httpMock.expectOne(
      `${API_URL}/1/tasks`
    );

    expect(req.request.method).toBe('GET');

    req.flush(mockTasks);
  });
});