import { Route } from '@angular/router';
import { LoginComponent, RegisterComponent, ForgotPasswordComponent } from '@microfrontends/shared-ui';


export const appRoutes: Route[] = [
    {
    path:'',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent
  },
  {
    path: 'app',
    loadChildren: () =>
      import('./remote-entry/entry.routes').then((m) => m.remoteRoutes),
  },
];
