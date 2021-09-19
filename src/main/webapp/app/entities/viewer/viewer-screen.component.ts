import { Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { faCube, faPlay } from '@fortawesome/free-solid-svg-icons';
import { ViewerScreenService } from './viewer-screen.service';
import {map} from "rxjs/operators";

@Component({
  selector: 'jhi-viewer-screen',
  templateUrl: './viewer-screen.component.html',
  styleUrls: ['./viewer-screen.component.scss'],
})
export class ViewerScreenComponent implements OnInit, OnChanges {
  faPlay = faPlay;
  faCube = faCube;

  @ViewChild('rendererCanvas', { static: true })
  public rendererCanvas!: ElementRef<HTMLCanvasElement>;
  links = new Array<string>();
  @Input() content!: string;
  @Input() renderFormat!: string;
  @Input() name!: string;
  @Input() changeInName!: string;
  @Input() changeInVisibility!: boolean;
  visibility = new Map<string, boolean>();
  currentName: string;
  mode = 'shadedWireframe';

  public constructor(private engServ: ViewerScreenService) {}

  public ngOnInit(): void {
    this.engServ.createScene(this.rendererCanvas, this.links, this.name, this.mode);
    //this.engServ.animate();
  }

  public ngOnChanges(changes: SimpleChanges): void {
    if(changes.content !== undefined && changes.content.currentValue !== changes.content.previousValue){
      this.links.push(changes.content.currentValue);
      this.visibility.set(this.name, true);
    }

    if(
        changes.changeInName !== undefined &&
        changes.changeInName.currentValue !== changes.changeInName.previousValue
    ) {
      const name = changes.changeInName.currentValue;
      this.currentName = name;
      const value = !this.visibility.get(name);
      this.visibility.set(name, value);
      // console.log("Name changed");
      this.engServ.changeVisibility(name, value);
      return;
    } else if(
      changes.changeInVisibility !== undefined &&
      changes.changeInVisibility.currentValue !== changes.changeInVisibility.previousValue
    ){
      const name = this.currentName;
      const value = changes.changeInVisibility.currentValue;
      this.visibility.set(name, value);
      // console.log("Bool changed");
      this.engServ.changeVisibility(name, value);
      return;
    }

    this.engServ.createScene(this.rendererCanvas, this.links, this.name, this.mode);
  }

  public renderMode(mode: string): void {
    if (mode === 'fit') {
      this.engServ.fitToScreen();
    } else {
      //this.engServ.createScene(this.rendererCanvas, this.content, mode);
      this.engServ.setMode(mode);
      //this.engServ.animate();
    }
  }
}
