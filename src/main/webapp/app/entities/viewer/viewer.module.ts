import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from 'app/shared/shared.module';
import { ViewerComponent } from './viewer.component';
import { viewerRoute } from './viewer.route';
import { GeometryUploadComponent } from './geometry-upload.component';
import { MeshSettingsComponent } from './mesh-settings.component';
import { MeshDownloadComponent } from './mesh-download.component';
import { DeleteDialogComponent } from './delete-dialog.component';
import { ViewerScreenComponent } from './viewer-screen.component';
import { MatIconModule } from '@angular/material/icon';
import { MatTreeModule } from '@angular/material/tree';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import {IconsModule} from "angular-bootstrap-md";

@NgModule({
    imports: [
        SharedModule,
        RouterModule.forChild(viewerRoute),
        MatTreeModule,
        MatIconModule,
        MatSelectModule,
        MatCardModule,
        MatButtonModule,
        MatExpansionModule,
        IconsModule,
    ],
  exports: [MeshSettingsComponent, GeometryUploadComponent],
  declarations: [
    ViewerComponent,
    GeometryUploadComponent,
    MeshSettingsComponent,
    MeshDownloadComponent,
    DeleteDialogComponent,
    ViewerScreenComponent,
  ],
  // entryComponents: [ModelDeleteDialogComponent],
})
export class ViewerModule {}
