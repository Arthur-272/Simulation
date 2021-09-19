import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { IGeometry } from 'app/shared/model/geometry.model';
import { AlertService } from 'app/_alert';
import { JhiEventManager } from 'ng-jhipster';
import { ViewerService } from './viewer.service';
import { FormBuilder } from '@angular/forms';
import { IMesh } from 'app/shared/model/mesh.model';

@Component({
  selector: 'jhi-mesh-settings',
  templateUrl: './mesh-settings.component.html',
  styleUrls: ['./mesh-settings.component.scss'],
})
export class MeshSettingsComponent implements OnInit {
  model?: IGeometry;
  isModel = false;
  edgeLength = 0.05;
  tolerance = 0.001;

  @Input()
  mesh: IMesh;
  options = {
    autoClose: true,
    keepAfterRouteChange: true,
  };

  constructor(
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager,
    protected alertService: AlertService,
    protected activatedRoute: ActivatedRoute,
    protected viewerService: ViewerService,
    protected formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    if (this.mesh !== undefined) {
      this.isModel = false;
    } else if (this.model !== undefined) {
      this.isModel = true;
    }
  }

  previousState(): void {
    window.history.back();
  }

  // passBack(): void {
  //   this.activeModal.close(this.jobIds);
  // }

  // onSubmit(data: any): void{
  //   // window.alert(data);
  //   this.viewerService.runSimulation(data).subscribe(res => {
  //             if (res.status === 200) {
  //               this.alertService.success('Your job is running. Getting your output ready.');
  //               this.eventManager.broadcast('jobListModification');
  //               this.activeModal.close();
  //             } else if (res.status === 404 || res.status === 500 || res.status === 400) {
  //               this.alertService.error('There was some problem.Please try again later', this.options);
  //             }
  //           });
  //   // this.onSimulate(data.edgeLength.toString(), data.tolerance.toString(), data.iteration.toString());
  // }

  onSimulate(data: any): void {
    if (!this.model && this.mesh) {
      this.model = this.mesh.model;
    }
    this.alertService.info('Your job is being submitted.', this.options);
    if (this.model) {
      if (this.model.id) {
        this.viewerService.runSimulation(this.model.id, data).subscribe(res => {
          if (res.status === 200) {
            this.alertService.success('Your job is running. Getting your output ready.');
            this.eventManager.broadcast('jobListModification');
            this.activeModal.close();
          } else if (res.status === 404 || res.status === 500 || res.status === 400) {
            this.alertService.error('There was some problem.Please try again later', this.options);
          }
        });
      }
    }
  }

  previous(): void {
    window.history.back();
  }

  cancel(): void {
    this.activeModal.dismiss();
  }
}
