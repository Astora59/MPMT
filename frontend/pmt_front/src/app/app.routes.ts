import { Routes } from '@angular/router';
import { RouterModule } from '@angular/router';

import { Home } from './pages/home/home';
import { ProjectPage } from './pages/project-page/project-page';
import { Task } from './pages/task/task';
import { RegisterComponent  } from './pages/register/register';
import { Login } from './pages/login/login';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { NotFound } from './pages/not-found/not-found';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
    {
        path: '',
        component: Home
    },
    {
        path: 'project/:id',
        component: ProjectPage
    },
    {
        path: 'task/:id',
        component: Task
    },
    {
        path: 'register',
        component: RegisterComponent
    },
    {
        path: 'login',
        component: Login
    },
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [authGuard]
    },
    {
        path: '**',
        component: NotFound
    }
    
];
