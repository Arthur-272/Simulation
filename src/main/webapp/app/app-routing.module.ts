import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { errorRoute } from './layouts/error/error.route';
import { navbarRoute } from './layouts/navbar/navbar.route';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';
import { Authority } from 'app/config/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ViewerComponent } from './entities/viewer/viewer.component';
import { AboutUsComponent } from 'app/entities/about-us/about-us.component';
import { DemoComponent } from 'app/entities/demo/demo.component';
import { FaqsComponent } from 'app/entities/faqs/faqs.component';

const LAYOUT_ROUTES = [navbarRoute, ...errorRoute];

@NgModule({
  imports: [
    RouterModule.forRoot(
      [
        {
          path: 'admin',
          data: {
            authorities: [Authority.ADMIN],
          },
          canActivate: [UserRouteAccessService],
          loadChildren: () => import('./admin/admin-routing.module').then(m => m.AdminRoutingModule),
        },
        {
          path: 'project/:projectId/model',
          data: {
            authorities: [Authority.USER],
          },
          canActivate: [UserRouteAccessService],
          component: ViewerComponent,
        },
        {
          path: 'AboutUs',
          data: {
            authorities: [Authority.USER],
          },
          canActivate: [UserRouteAccessService],
          component: AboutUsComponent,
        },
        {
          path: 'FAQs',
          data: {
            authorities: [Authority.USER],
          },
          canActivate: [UserRouteAccessService],
          component: FaqsComponent,
        },
        {
          path: 'Demo',
          data: {
            authorities: [Authority.USER],
          },
          canActivate: [UserRouteAccessService],
          component: DemoComponent,
        },
        {
          path: 'account',
          loadChildren: () => import('./account/account.module').then(m => m.AccountModule),
        },
        {
          path: 'login',
          loadChildren: () => import('./login/login.module').then(m => m.LoginModule),
        },
        ...LAYOUT_ROUTES,
      ],
      { enableTracing: DEBUG_INFO_ENABLED }
    ),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
