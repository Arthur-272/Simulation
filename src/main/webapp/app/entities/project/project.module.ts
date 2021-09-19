import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared/shared.module';
import { ProjectComponent } from './project.component';
// import { ProjectDetailComponent } from './project-detail.component';
// import { ProjectUpdateComponent } from './project-update.component';
// import { ProjectDeleteDialogComponent } from './project-delete-dialog.component';
import { projectRoute } from './project.route';
import { ProjectUpdateComponent } from './project-update.component';
import { ProjectDeleteDailogComponent } from './project-delete-dailog.component';

@NgModule({
  imports: [SharedModule, RouterModule.forChild(projectRoute)],
  declarations: [ProjectComponent, ProjectUpdateComponent, ProjectDeleteDailogComponent],
  entryComponents: [ProjectDeleteDailogComponent],
})
export class ProjectModule {}
