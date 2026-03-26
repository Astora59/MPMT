import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Project } from '../models/project.model';
import { ProjectMember } from '../models/project-member';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private apiUrl = 'http://localhost:8080/projects';

  constructor(private http: HttpClient) {}

  getMyProjects() {
    return this.http.get<Project[]>(`${this.apiUrl}/me`);
  }

  createProject(project: { projectName: string; projectDescription: string }) {
    return this.http.post(`${this.apiUrl}/create`, project);
  }

  inviteUser(projectId: string, email: string) {
    return this.http.post(
      `${this.apiUrl}/${projectId}/invite`,
      { email: email },
      { responseType: 'text' }
    );
  }

  getProjectMembers(projectId: string) {
  return this.http.get<ProjectMember[]>(
    `http://localhost:8080/projects/${projectId}/members`
  )
}

assignTask(projectId: string, taskId: string, userEmail: string) {

  const body = {
    userEmail: userEmail
  }

  return this.http.put(
    `http://localhost:8080/projects/${projectId}/tasks/${taskId}/assign`,
    body
  )
}


  updateUserRole(projectId: string, body: any) {

  return this.http.put(
    `http://localhost:8080/projects/${projectId}/role`,
    body,
    { responseType: 'text' }
  );

  }

createTask(projectId: string, body: any) {

  const token = localStorage.getItem('token')

  return this.http.post(
    `http://localhost:8080/projects/${projectId}/tasks`,
    body,
    {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  )

}


updateTask(projectId: string, taskId: string, body: any) {
  return this.http.put(
    `http://localhost:8080/projects/${projectId}/tasks/${taskId}`,
    body
  );
}

getTaskHistory(projectId: string, taskId: string) {
  return this.http.get<any[]>(
    `http://localhost:8080/projects/${projectId}/tasks/${taskId}/history`
  );
}
}
 