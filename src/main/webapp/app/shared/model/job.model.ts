import { IMesh } from './mesh.model';

export interface IJob {
  id?: string;
  jobId?: string;
  mesh?: IMesh;
  jobStatus?: string;
}

export class Job implements IJob {
  constructor(id?: string, jobId?: string, mesh?: IMesh, status?: string) {}
}
