import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { OutputFormats } from 'app/shared/model/mesh.model';
import { AlertService } from 'app/_alert';
import { JhiEventManager } from 'ng-jhipster';
import { ViewerService } from './viewer.service';

@Component({
  selector: 'jhi-mesh-download',
  templateUrl: './mesh-download.component.html',
})
export class MeshDownloadComponent {
  id!: string;
  requiredFormat = 'msh';
  outputLink!: OutputFormats;
  formats = [{ name: 'msh' }, { name: 'obj' }];
  options = {
    autoClose: true,
    keepAfterRouteChange: true,
  };

  constructor(
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager,

    protected alertService: AlertService,
    protected viewerService: ViewerService,
    protected activatedRoute: ActivatedRoute
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  onSelectFormat(e: any): void {
    this.requiredFormat = e.target.value;
  }

  onDownload(): void {
    if (this.requiredFormat) {
      this.viewerService.downloadMesh(this.id, this.requiredFormat).subscribe((res: any) => {
        if (res !== null) {
          try {
            window.open(res, '_blank');
          } catch (e) {
            this.alertService.error('There was some problem.Please try again later', this.options);
          }
        } else {
          this.alertService.error('There was some problem.Please try again later', this.options);
        }
      });
    } else {
      this.alertService.error('Please select an output format to download.', this.options);
    }
  }
}
