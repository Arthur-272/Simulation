import { IGeometry } from './geometry.model';

export interface OutputFormats {
  msh?: string;
  obj?: string;
  drc?: string;
}

export interface IMesh {
  id?: string;
  outputLink?: OutputFormats;
  model?: IGeometry;
  edgeLength?: number;
  tolerance?: number;
}

export class Mesh implements IMesh {
  constructor(id?: string, outputLink?: OutputFormats, model?: IGeometry, edgeLength?: number, epsilon?: number) {}
}
