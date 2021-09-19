import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  faCloudDownloadAlt,
  faPlay,
  faCloudUploadAlt,
  faSync,
  faSyncAlt,
  faTrashAlt,
  faChevronDown,
  faChevronRight,
  faQuestionCircle,
  faInfo,
  faEye,
  faEyeSlash,
  faSpinner,
} from '@fortawesome/free-solid-svg-icons';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { IGeometry, InputFormats } from 'app/shared/model/geometry.model';
import { IJob } from 'app/shared/model/job.model';
import { IMesh, OutputFormats } from 'app/shared/model/mesh.model';
import { AlertService } from 'app/_alert';
import { JhiEventManager } from 'ng-jhipster';
import { interval, Subscription } from 'rxjs';
import { ViewerService } from './viewer.service';
import { GeometryUploadComponent } from './geometry-upload.component';
import { MeshSettingsComponent } from './mesh-settings.component';
import { MeshDownloadComponent } from './mesh-download.component';
import { HttpResponse } from '@angular/common/http';
import { DeleteDialogComponent } from './delete-dialog.component';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';

interface GeometryNode {
  id?: string;
  name?: string;
  inputLink?: InputFormats;
  children?: GeometryNode[];
}

interface MeshNode {
  id?: string;
  outputLink?: OutputFormats;
  model?: IGeometry;
  edgeLength?: number;
  epsilon?: number;
  children?: MeshNode[];
}
interface JobNode {
  id?: string;
  jobId?: string;
  status?: string;
  mesh?: IMesh;
  children?: JobNode[];
}

@Component({
  selector: 'jhi-viewer',
  templateUrl: './viewer.component.html',
  styleUrls: ['./viewer.component.scss'],
})
export class ViewerComponent implements OnInit {
  faCloudDownloadAlt = faCloudDownloadAlt;
  faPlay = faPlay;
  faCloudUploadAlt = faCloudUploadAlt;
  faSync = faSync;
  faSyncAlt = faSyncAlt;
  faTrashAlt = faTrashAlt;
  faChevronDown = faChevronDown;
  faChevronRight = faChevronRight;
  faQuestionCircle = faQuestionCircle;
  faInfo = faInfo;
  faEye = faEye;
  faEyeSlash = faEyeSlash;

  name: string;
  models: IGeometry[] = [];
  visibility = new Map<string, boolean>();
  changeInName:string;
  changeInVisibility:boolean;
  meshes: IMesh[] = [];
  model: IGeometry | null = null;
  modelSelected: IGeometry;
  meshSelected?: IMesh;
  jobSelected?: IJob;
  eventSubscriber?: Subscription;
  currentProjectId!: string | null;
  jobs: IJob[] = [];
  jobSubscription!: Subscription;
  showModels = false;
  showJobs = false;
  content!: string;
  outputLink!: OutputFormats;
  requiredFormat = 'msh';
  renderFormat!: string;
  model_data: GeometryNode[] = [];
  mesh_data: MeshNode[] = [];
  job_data: JobNode[] = [];
  options = {
    autoClose: true,
    keepAfterRouteChange: true,
  };
  helpOptions: string[] = ['AboutUs', 'FAQs', 'Demo'];

  treeControl = new NestedTreeControl<GeometryNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<GeometryNode>();

  treeControlMesh = new NestedTreeControl<MeshNode>(node => node.children);
  dataSourceMesh = new MatTreeNestedDataSource<MeshNode>();

  treeControlJob = new NestedTreeControl<JobNode>(node => node.children);
  dataSourceJob = new MatTreeNestedDataSource<JobNode>();

  constructor(
    protected modelService: ViewerService,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal,
    private route: ActivatedRoute,
    protected alertService: AlertService
  ) {
    this.model_data.push({ id: '0', name: 'Models', inputLink: {}, children: this.models });
    this.mesh_data.push({ id: 'Meshes', outputLink: {}, model: {}, edgeLength: 0, epsilon: 0 });
    this.job_data.push({ id: 'Jobs', jobId: '', status: '', mesh: {}, children: this.jobs });
  }

  onModelSelected(modelSelected: IGeometry): void {
    console.log(modelSelected.name);
    this.modelSelected = modelSelected;
    this.meshSelected = undefined;
    this.jobSelected = undefined;
    // this.downloadModelContent('drc', );
  }

  onMeshSelected(meshSelected: IMesh): void {
    this.meshSelected = meshSelected;
    this.jobSelected = undefined;
    this.modelSelected = undefined!;
    // this.modelSelected = undefined;
    // this.downloadMeshContent('drc', meshSelected);
    this.onPressRun();
  }

  onJobSelected(jobSelected: IJob): void {
    this.jobSelected = jobSelected;
    // this.modelSelected = undefined;
    this.meshSelected = undefined;
  }

  downloadModelContent(format: string, node: IGeometry): void {
    if (node) {
      if (node.id) {
        this.modelService.renderModel(node.id, format).subscribe(res => {
          if (res != null) {
            this.content = res;
            if(node){
              this.visibility.set(node.name!, true);
              this.name = node.name!;
            }
            this.renderFormat = format;
            this.name = node.name!;
          }
        });
      }
    }
  }

  downloadMeshContent(format: string, node: IMesh): void {
    if (node) {
      if (node.id) {
        this.modelService.renderMesh(node.id, format).subscribe(res => {
          if (res != null) {
            this.content = res;
            if(node.model) {
              this.name = node.model.name!  + "_m";
              this.visibility.set(this.name, true);
              console.log(this.name);
            }
            this.meshSelected = node;
            this.renderFormat = format;
          }
        });
      }
    }
  }

  onPressUpload(): void {
    this.modalService.open(GeometryUploadComponent, { size: 'lg', backdrop: 'static' });
  }

  onPressRun(): void {
    if (this.modelSelected || this.meshSelected) {
      const modalRef = this.modalService.open(MeshSettingsComponent, { size: 'lg', backdrop: 'static' });
      modalRef.componentInstance.model = this.modelSelected;
      modalRef.componentInstance.mesh = this.meshSelected;
    } else {
      this.alertService.error('Please select an appropriate model', this.options);
    }
  }

  onPressDownload(): void {
    if (this.meshSelected) {
      if (this.meshSelected.id) {
        const modalRef = this.modalService.open(MeshDownloadComponent, { size: 'lg', backdrop: 'static' });
        modalRef.componentInstance.id = this.meshSelected.id;
      }
    } else {
      this.alertService.error('Please select a mesh to download.', this.options);
    }
  }

  onPressTerminate(): void {
    const modalRef = this.modalService.open(DeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    if (this.jobSelected) {
      modalRef.componentInstance.id = this.jobSelected.jobId;
      modalRef.componentInstance.type = 'job';
    } else if (this.modelSelected) {
      modalRef.componentInstance.id = this.modelSelected.id;
      modalRef.componentInstance.type = 'geometry';
    } else if (this.meshSelected) {
      modalRef.componentInstance.id = this.meshSelected.id;
      modalRef.componentInstance.type = 'mesh';
    }
    //modalRef.componentInstance.model = model;
  }

  onPressRefresh(): void {
    this.loadAllJobs();
    this.loadAllMeshes();
    this.loadAllModels();
  }

  ngOnInit(): void {
    this.loadAllModels();
    this.loadAllMeshes();
    this.loadAllJobs();
    this.registerChangeInModels();

    const getJobStatus = interval(150000);
    this.jobSubscription = getJobStatus.subscribe(res => {
      this.loadAllJobs();
      this.loadAllMeshes();
    });
  }

  ngOnDestroy(): void {
    if (this.jobSubscription) {
      this.jobSubscription.unsubscribe();
    }
  }

  trackId(index: number, item: IGeometry): string {
    return item.id!;
  }

  registerChangeInModels(): void {
    this.eventSubscriber = this.eventManager.subscribe('modelListModification', () => this.loadAllModels());
    this.eventSubscriber = this.eventManager.subscribe('jobListModification', () => this.loadAllJobs());
    this.eventSubscriber = this.eventManager.subscribe('meshListModification', () => this.loadAllMeshes());
  }

  loadAllModels(): void {
    const hasProjectId: boolean = this.route.snapshot.paramMap.has('projectId');

    if (hasProjectId) {
      this.currentProjectId = this.route.snapshot.paramMap.get('projectId');
    }
    if (this.currentProjectId !== null) {
      this.modelService.query(this.currentProjectId).subscribe(
        (res: HttpResponse<IGeometry[]>) => (
          (this.models = res.body || []),
          this.models.forEach(node => {
            this.downloadModelContent("drc", node);
          }),
          (this.model_data[0].children = this.models),
          (this.dataSource.data = []),
          (this.dataSource.data = this.model_data)
        )
      );
    }
  }

  loadAllMeshes(): void {
    const hasProjectId: boolean = this.route.snapshot.paramMap.has('projectId');

    if (hasProjectId) {
      this.currentProjectId = this.route.snapshot.paramMap.get('projectId');
    }
    if (this.currentProjectId !== null) {
      this.modelService.queryMesh(this.currentProjectId).subscribe(
        (res: HttpResponse<IMesh[]>) => (
          (this.meshes = res.body || []),
          this.meshes.forEach(node => {
            this.downloadMeshContent("drc", node);
          }),
          (this.mesh_data[0].children = this.meshes),
          (this.dataSourceMesh.data = []),
          (this.dataSourceMesh.data = this.mesh_data)
        )
      );
    }
  }

  loadAllJobs(): void {
    const hasProjectId: boolean = this.route.snapshot.paramMap.has('projectId');
    if (hasProjectId) {
      this.currentProjectId = this.route.snapshot.paramMap.get('projectId');
    }
    if (this.currentProjectId !== null) {
      this.modelService
        .queryJob(this.currentProjectId)
        .subscribe(
          (res: HttpResponse<IJob[]>) => (
            (this.jobs = res.body || []),
            (this.job_data[0].children = this.jobs),
            (this.dataSourceJob.data = []),
            (this.dataSourceJob.data = this.job_data)
          )
        );
    }
  }

  delete(model: IGeometry): void {
    const modalRef = this.modalService.open(DeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.model = model;
  }

  cancel(jobId: string | undefined): void {
    if (jobId) {
      this.modelService.cancelJob(jobId).subscribe(
        res => {
          if (res.status === 200) {
            this.eventManager.broadcast('jobListModification');
            this.alertService.success('Your job is stopped successfully', this.options);
          }
        },
        _err => {
          this.alertService.error("Your job couldn't be stopped. Please try in a while.");
        }
      );
    }
  }

  hasChild = (_: number, node: GeometryNode) => !!node.children && node.children.length > 0;
  hasChildMesh = (_: number, node: MeshNode) => !!node.children && node.children.length > 0;
  hasChildJob = (_: number, node: JobNode) => !!node.children && node.children.length > 0;

  onClickVisibility(name: string) {
    if (name) {
      this.visibility.set(name, !this.visibility.get(name));
      this.changeInName = name;
      this.changeInVisibility = this.visibility.get(name)!;
      console.log(this.visibility);
      // console.log(this.changeInName);
    }
  }
}
