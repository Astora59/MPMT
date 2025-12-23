import { Routes } from '@angular/router';
import { RouterModule } from '@angular/router';

import { Home } from './pages/home/home';
import { ProjectPage } from './pages/project-page/project-page';
import { Task } from './pages/task/task';
import { Register } from './pages/register/register';
import { Login } from './pages/login/login';
import { Dashboard } from './pages/dashboard/dashboard';
import { NotFound } from './pages/not-found/not-found';

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
        component: Register
    },
    {
        path: 'login',
        component: Login
    },
    {
        path: 'dashboard',
        component: Dashboard
    },
    {
        path: '**',
        component: NotFound
    }
    
];
