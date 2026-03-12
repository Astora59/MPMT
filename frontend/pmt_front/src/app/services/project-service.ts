import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Project } from '../models/project.model';

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

  updateUserRole(projectId: string, email: string, roleName: string) {

  return this.http.put(
    `http://localhost:8080/projects/${projectId}/role`,
    {
      email: email,
      roleName: roleName
    }
  );

}
}
