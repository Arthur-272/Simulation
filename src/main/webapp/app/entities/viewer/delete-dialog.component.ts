import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from 'app/_alert';
import { JhiEventManager } from 'ng-jhipster';
import { ViewerService } from './viewer.service';

@Component({
  selector: 'jhi-delete-dialog',
  templateUrl: './delete-dialog.component.html',
})
export class DeleteDialogComponent {
  id?: string;
  type?: string;
  options = {
    autoClose: true,
    keepAfterRouteChange: true,
  };

  constructor(
    protected viewerService: ViewerService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager,
    protected alertService: AlertService
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(): void {
    if (this.id && this.type) {
      if (this.type === 'job') {
        this.viewerService.cancelJob(this.id).subscribe(res => {
          if (res.status === 200) {
            this.eventManager.broadcast('jobListModification');
            this.activeModal.close();
          }
        });
      } else if (this.type === 'geometry') {
        this.viewerService.deleteModel(this.id).subscribe(() => {
          this.eventManager.broadcast('modelListModification');
          this.activeModal.close();
        });
      } else if (this.type === 'mesh') {
        this.viewerService.deleteMesh(this.id).subscribe(() => {
          this.eventManager.broadcast('meshListModification');
          this.activeModal.close();
        });
      } else {
        this.alertService.error('Please select an appropriate model', this.options);
      }
    }
  }
}
