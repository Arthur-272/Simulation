import { HttpClient, HttpErrorResponse, HttpEvent, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { SERVER_API_URL } from 'app/app.constants';
import { AlertService } from 'app/_alert';
import { IGeometry } from 'app/shared/model/geometry.model';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { IMesh } from 'app/shared/model/mesh.model';
import { IJob } from 'app/shared/model/job.model';

type EntityResponseType = HttpResponse<IGeometry>;
type EntityArrayResponseType = HttpResponse<IGeometry[]>;

@Injectable({ providedIn: 'root' })
export class ViewerService {
  public resourceUrl = SERVER_API_URL + 'api/models';

  currentProjectId!: string | null;

  constructor(protected http: HttpClient, private alertService: AlertService) {}

  create(model: IGeometry): Observable<EntityResponseType> {
    return this.http.post<IGeometry>(this.resourceUrl, model, { observe: 'response' });
  }

  update(model: IGeometry): Observable<EntityResponseType> {
    return this.http.put<IGeometry>(this.resourceUrl, model, { observe: 'response' });
  }

  find(id: string): Observable<EntityResponseType> {
    return this.http.get<IGeometry>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(projectId: string): Observable<EntityArrayResponseType> {
    this.currentProjectId = projectId;
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${projectId}/models`;
    return this.http.get<IGeometry[]>(urlModel, { observe: 'response' });
  }

  queryMesh(projectId: string): Observable<EntityArrayResponseType> {
    this.currentProjectId = projectId;
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${projectId}/meshes`;
    return this.http.get<IMesh[]>(urlModel, { observe: 'response' });
  }

  queryJob(projectId: string): Observable<EntityArrayResponseType> {
    this.currentProjectId = projectId;
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${projectId}/jobs`;
    return this.http.get<IJob[]>(urlModel, { observe: 'response' });
  }

  uploadModel(fd: FormData): Observable<HttpEvent<any>> {
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${this.currentProjectId}/models`;
    return this.http.post(urlModel, fd, { reportProgress: true, observe: 'events' }).pipe(catchError(this.errorMgmt));
  }

  runSimulation(modelId: string, data: any): Observable<HttpResponse<Object>> {
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${this.currentProjectId}/model/${modelId}/simulate`;
    return this.http.post(urlModel, data, { observe: 'response' });
  }

  downloadMesh(meshId: String, format: string): Observable<string> {
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel} /${this.currentProjectId}/mesh/${meshId}
    /download?format=${format}`;
    return this.http.get(urlModel, { responseType: 'text' });
  }

  renderModel(modelId: String, format: string): Observable<string> {
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${this.currentProjectId}/model/${modelId}/getModel?format=${format}`;
    return this.http.get(urlModel, { responseType: 'text' });
  }

  renderMesh(meshId: String, format: string): Observable<string> {
    let urlModel = SERVER_API_URL + 'api/project';
    urlModel = `${urlModel}/${this.currentProjectId}/mesh/${meshId}/getMesh?format=${format}`;
    return this.http.get(urlModel, { responseType: 'text' });
  }

  deleteModel(id: string): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  deleteMesh(id: string): Observable<HttpResponse<{}>> {
    const urlModel = SERVER_API_URL + 'api/meshes';
    return this.http.delete(`${urlModel}/${id}`, { observe: 'response' });
  }

  terminateCluster(): Observable<HttpResponse<{}>> {
    return this.http.get(`${SERVER_API_URL}/api/terminateCluster`, { observe: 'response' });
  }

  cancelJob(jobId: string): Observable<HttpResponse<{}>> {
    return this.http.get(`${SERVER_API_URL}/api/cancelJob?jobId=${jobId}`, { observe: 'response' });
  }

  errorMgmt(error: HttpErrorResponse): Observable<never> {
    let errorMessage = '';
    if (error.error instanceof ErrorEvent) {
      // Get client-side error
      errorMessage = error.error.message;
    } else {
      // Get server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    this.alertService.error(errorMessage);
    return throwError(errorMessage);
  }
}
