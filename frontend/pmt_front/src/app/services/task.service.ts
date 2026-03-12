import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Task {
  taskId: string;
  taskTitle: string;
  taskDescription: string;
  taskDeadline: string;
  taskStatus: string;
  taskPriority: string;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private apiUrl = 'http://localhost:8080/projects';

  constructor(private http: HttpClient) {}

  getTasksByProject(projectId: string): Observable<Task[]> {
    return this.http.get<Task[]>(
      `${this.apiUrl}/${projectId}/tasks`
    );
  }
}