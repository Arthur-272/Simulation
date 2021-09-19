import { Component, OnDestroy, OnInit } from '@angular/core';
import { JhiEventManager } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { IProject } from 'app/shared/model/project.model';
import { Subscription } from 'rxjs';
import { ProjectService } from './project.service';
import { HttpResponse } from '@angular/common/http';
import { ProjectDeleteDailogComponent } from './project-delete-dailog.component';

@Component({
  selector: 'jhi-project',
  templateUrl: './project.component.html',
  styleUrls: ['./project.component.scss'],
})
export class ProjectComponent implements OnInit, OnDestroy {
  projects?: IProject[];
  eventSubscriber?: Subscription;

  constructor(protected projectService: ProjectService, protected eventManager: JhiEventManager, protected modalService: NgbModal) {}

  loadAll(): void {
    this.projectService.query().subscribe((res: HttpResponse<IProject[]>) => (this.projects = res.body ?? []));
  }

  ngOnInit(): void {
    this.loadAll();

    this.registerChangeInProjects();
  }

  ngOnDestroy(): void {
    if (this.eventSubscriber) {
      this.eventManager.destroy(this.eventSubscriber);
    }
  }

  trackId(index: number, item: IProject): string {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInProjects(): void {
    this.eventSubscriber = this.eventManager.subscribe('projectListModification', () => this.loadAll());
  }

  delete(project: IProject): void {
    const modalRef = this.modalService.open(ProjectDeleteDailogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.project = project;
  }
}
