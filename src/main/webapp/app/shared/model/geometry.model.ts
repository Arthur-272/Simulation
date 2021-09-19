export interface InputFormats {
  stl?: string;
  drc?: string;
}
export interface IGeometry {
  id?: string;
  name?: string;
  inputLink?: InputFormats;
}

export class Geometry implements IGeometry {
  constructor(public id?: string, public name?: string, public inputLink?: InputFormats) {}
}
