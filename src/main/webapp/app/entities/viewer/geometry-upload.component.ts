import { HttpEvent, HttpEventType } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from 'app/_alert';
import { JhiEventManager } from 'ng-jhipster';
import { ViewerService } from './viewer.service';

@Component({
  selector: 'jhi-geometry-upload',
  templateUrl: './geometry-upload.component.html',
})
export class GeometryUploadComponent implements OnInit {
  isSaving = false;

  uploadedObj!: File;
  progress = 0;
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

  onFileSelected(event: any): void {
    this.uploadedObj = event.target.files[0];
  }
  ngOnInit(): void {
    this.progress = 0;
  }

  onUpload(): void {
    const fd = new FormData();
    fd.append('object', this.uploadedObj);
    this.viewerService.uploadModel(fd).subscribe(
      (event: HttpEvent<any>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            if (event.total !== undefined) {
              this.progress = Math.round((event.loaded / event.total) * 100);
            }
            break;
          case HttpEventType.Response:
            setTimeout(() => {
              this.progress = 0;
              this.eventManager.broadcast('modelListModification');
              this.activeModal.close();
              this.alertService.success('File uploaded successfully', this.options);
            }, 1000);
        }
      },
      error => {
        this.alertService.error(`File upload failed.${error}`, this.options);
      }
    );
  }

  previousState(): void {
    window.history.back();
  }

  cancel(): void {
    this.activeModal.dismiss();
  }
}
