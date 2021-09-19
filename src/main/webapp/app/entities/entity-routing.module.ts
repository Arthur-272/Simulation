import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'viewer',
        data: { pageTitle: 'Viewer' },
        loadChildren: () => import('./viewer/viewer.module').then(m => m.ViewerModule),
      },
      {
        path: 'project',
        data: { pageTitle: 'Projects' },
        loadChildren: () => import('./project/project.module').then(m => m.ProjectModule),
      },
      // {
      //   path: 'accounts',
      //   data: { pageTitle: 'Accounts' },
      //   loadChildren: () => import('./accounts/accounts.module').then(m => m.AccountsModule),
      // },
      // {
      //   path: 'geometry',
      //   data: { pageTitle: 'Geometries' },
      //   loadChildren: () => import('./geometry/geometry.module').then(m => m.GeometryModule),
      // },
      // {
      //   path: 'job',
      //   data: { pageTitle: 'Jobs' },
      //   loadChildren: () => import('./job/job.module').then(m => m.JobModule),
      // },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
