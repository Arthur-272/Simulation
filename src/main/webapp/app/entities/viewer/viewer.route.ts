import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router, Routes } from '@angular/router';
import { Authority } from 'app/config/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Geometry, IGeometry } from 'app/shared/model/geometry.model';
import { EMPTY, Observable, of } from 'rxjs';
import { flatMap } from 'rxjs/operators';
import { ViewerComponent } from './viewer.component';
import { ViewerService } from './viewer.service';

@Injectable({ providedIn: 'root' })
export class ViewerResolve implements Resolve<IGeometry> {
  constructor(private service: ViewerService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IGeometry> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((model: HttpResponse<Geometry>) => {
          if (model.body) {
            return of(model.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Geometry());
  }
}

export const viewerRoute: Routes = [
  {
    path: 'viewer',
    component: ViewerComponent,
    data: {
      authorities: [Authority.USER],
      pageTitle: 'Models',
    },
    canActivate: [UserRouteAccessService],
  },
  //   {
  //     path: ':id/view',
  //     component: ModelDetailComponent,
  //     resolve: {
  //       model: ModelResolve,
  //     },
  //     data: {
  //       authorities: [Authority.USER],
  //       pageTitle: 'Models',
  //     },
  //     canActivate: [UserRouteAccessService],
  //   },
  //   {
  //     path: 'new',
  //     component: ModelUpdateComponent,
  //     resolve: {
  //       model: ModelResolve,
  //     },
  //     data: {
  //       authorities: [Authority.USER],
  //       pageTitle: 'Models',
  //     },
  //     canActivate: [UserRouteAccessService],
  //   },
  //   {
  //     path: ':id/edit',
  //     component: ModelUpdateComponent,
  //     resolve: {
  //       model: ModelResolve,
  //     },
  //     data: {
  //       authorities: [Authority.USER],
  //       pageTitle: 'Models',
  //     },
  //     canActivate: [UserRouteAccessService],
  //   },
  {
    path: 'project/:projectId/model',
    component: ViewerComponent,
    data: {
      authorities: [Authority.USER],
      pageTitle: 'Viewer',
    },
    canActivate: [UserRouteAccessService],
  },
];
