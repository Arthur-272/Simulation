import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { IProject } from 'app/shared/model/project.model';
import { JhiEventManager } from 'ng-jhipster';
import { ProjectService } from './project.service';

@Component({
  templateUrl: './project-delete-dailog.component.html',
})
export class ProjectDeleteDailogComponent {
  project?: IProject;

  constructor(protected projectService: ProjectService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.projectService.delete(id).subscribe(() => {
      this.eventManager.broadcast('projectListModification');
      this.activeModal.close();
    });
  }
}
