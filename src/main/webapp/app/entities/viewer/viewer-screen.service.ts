import * as THREE from 'three';
import {ElementRef, Injectable, NgZone, OnDestroy} from '@angular/core';
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls';
import {DRACOLoader} from 'three/examples/jsm/loaders/DRACOLoader';
import {Box3, Object3D} from "three";

@Injectable({providedIn: 'root'})
export class ViewerScreenService implements OnDestroy {
  private canvas: HTMLCanvasElement;
  private renderer: THREE.WebGLRenderer;
  private camera: THREE.PerspectiveCamera;
  private scene: THREE.Scene;
  private light: THREE.AmbientLight;

  private url!: string;

  private frameId: number;

  private controls: any;

  private geometry: any = [];

  private visibility = new Map<string, boolean>();

  private material: any;

  private renderingMode: any;

  public constructor(private ngZone: NgZone) {
  }

  public ngOnDestroy(): void {
    if (this.frameId != null) {
      cancelAnimationFrame(this.frameId);
    }
  }

  public createScene(canvas: ElementRef<HTMLCanvasElement>, links: string[], name: string, mode: string): void {
    // The first step is to get the reference of the canvas element from our HTML document

    const draco_loader = new DRACOLoader();
    draco_loader.setDecoderPath('../../../content/draco/');

    this.material = new THREE.MeshPhysicalMaterial({
      color: 0xb2ffc8,
      metalness: 0.25,
      //roughness: 0.1,
      transparent: false,
      transmission: 1.0,
      side: THREE.DoubleSide,
      clearcoat: 1.0,
      clearcoatRoughness: 0.25,
    });

    this.canvas = canvas.nativeElement;

    this.renderer = new THREE.WebGLRenderer({
      canvas: this.canvas,
      alpha: true, // transparent background
      antialias: true, // smooth edges
    });
    this.renderer.setSize(window.innerWidth, window.innerHeight);

    // create the scene
    this.scene = new THREE.Scene();
    // this.scene.visible = visibility;

    this.camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 1000);

    this.camera.position.z = 5;
    this.scene.add(this.camera);

    // soft white light
    this.light = new THREE.AmbientLight(0x333333);
    this.light.position.z = 10;
    this.scene.add(this.light);



    // draco_loader.preload();
    //
    // draco_loader.load(this.url, (geometry: any) => {
    //   //const mesh = new THREE.Mesh(geometry, material);
    //
    //   this.geometry = geometry;
    //   // if (mode === 'wireframe') {
    //   //   this.wireframe(geometry, material);
    //   // } else if (mode === 'shadedWireframe') {
    //   //   this.shadedWireframe(geometry, material);
    //   // } else if (mode === 'shaded') {
    //   //   this.shaded(geometry, material);
    //   // }
    //   this.shadedWireframe(this.geometry, this.material);
    //   //this.shaded(this.geometry,this.material)
    //   this.fitToScreen();
    // });
    if (links[links.length - 1]) {
      this.url = links[links.length - 1].substr(1, links[links.length - 1].length - 2);
      draco_loader.load(this.url, (geometry: any) => {
        if(name !== undefined) {
          geometry.name = name;
          this.geometry.push(geometry);
          this.shadedWireFrame(geometry, this.material);
        }
      });
    }
    // this.shadedWireFrame1(this.geometry1, this.material);

    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;

    this.animate();
  }

  public animate(): void {
    // We have to run this outside angular zones,
    // because it could trigger heavy changeDetection cycles.
    this.ngZone.runOutsideAngular(() => {
      if (document.readyState !== 'loading') {
        this.controls.update();
        this.render();
      } else {
        window.addEventListener('DOMContentLoaded', () => {
          this.controls.update();
          this.render();
        });
      }

      this.displayMode(this.renderingMode);

      window.addEventListener('resize', () => {
        this.resize();
      });
    });
  }

  public render(): void {
    this.frameId = requestAnimationFrame(() => {
      this.render();
    });

    // this.cube.rotation.x += 0.01;
    // this.cube.rotation.y += 0.01;
    this.renderer.render(this.scene, this.camera);
  }

  public resize(): void {
    const width = window.innerWidth;
    const height = window.innerHeight;

    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();

    this.renderer.setSize(width, height);
  }

  public setMode(mode: string): void {
    this.renderingMode = mode;
    this.displayMode(this.renderingMode);
  }

  public displayMode(mode: string): void {
    if (mode === 'wireframe') {
      this.wireframe();
    } else if (mode === 'shadedWireframe') {
      this.shadedWireframe(this.material);
    } else if (mode === 'shaded') {
      this.shaded(this.material);
    }
  }

  public wireframe(): void {
    this.scene.clear();
    let wireframe, lines, preVisibility;
    this.geometry.forEach(
      (geometry: any) =>{
        wireframe = new THREE.WireframeGeometry(geometry);
        lines = new THREE.LineSegments(
          wireframe,
          new THREE.LineBasicMaterial({
            color: 0x000000,
          })
        );
        lines.name = geometry.name;
        lines.visible = this.visibility.get(lines.name)!;
        this.scene.add(lines);
      }
    )
  }

  public shadedWireframe(material: any): void {
    this.scene.clear();
    const wireframeMaterial = new THREE.MeshBasicMaterial({color: 0x000000, wireframe: true, transparent: true});
    let mesh, wireframe;
    this.geometry.forEach(
      (geometry: any) => {
        mesh = new THREE.Mesh(geometry, material);
        wireframe = new THREE.Mesh(geometry, wireframeMaterial);
        mesh.add(wireframe);
        mesh.name = geometry.name;
        mesh.visible = this.visibility.get(mesh.name)!;
        this.scene.add(mesh);
      }
    );
  }

  public shadedWireFrame(geometry: any, material: any): void {
    const wireframeMaterial = new THREE.MeshBasicMaterial({color: 0x000000, wireframe: true, transparent: true});
    const mesh = new THREE.Mesh(geometry, material);
    const wireframe = new THREE.Mesh(geometry, wireframeMaterial);
    mesh.add(wireframe);
    mesh.name = geometry.name;
    this.visibility.set(mesh.name, true);
    console.log(mesh.name);

    this.scene.add(mesh);
    this.fitToScreen();
  }

  public shaded(material: any): void {
    this.scene.clear();
    let mesh, preVisibility;
    this.geometry.forEach(
      (geometry: any) => {
        mesh = new THREE.Mesh(geometry, material);
        mesh.name = geometry.name;
        mesh.visible = this.visibility.get(mesh.name)!;
        this.scene.add(mesh);
      }
    );
  }

  public fitToScreen(): void {
    const pos = this.scene.position;
    this.camera.position.set(pos.x, pos.y, pos.z);
    this.camera.lookAt(pos);
    let max_X = 0, max_Y = 0;
    let tempBoundingBox;
    this.visibility.forEach(
      (value: boolean, key) => {
        if(value){
          tempBoundingBox = new Box3().setFromObject(this.scene.getObjectByName(key)!);
          if(tempBoundingBox.max.x > max_X){
            max_X = tempBoundingBox.max.x;
          }
          if(tempBoundingBox.max.y > max_Y){
            max_Y = tempBoundingBox.max.y;
          }
        }
      }
    )
    console.log(max_Y, max_X);
    const height = Math.max(max_Y, max_X);


    // const boxFrmScene = new THREE.Box3().setFromObject(this.scene);
    // const height = Math.max(boxFrmScene.max.y, boxFrmScene.max.x);
    const fov = this.camera.fov * (Math.PI / 360);
    const distance = Math.abs(height / Math.sin(fov / 2));
    this.camera.position.set(pos.x, pos.y, distance + height / 2);
    this.camera.updateProjectionMatrix();
  }

  public changeVisibility(name: string, value: boolean): void {
    console.log(name);
    const obj = this.scene.getObjectByName(name)!;
    this.visibility.set(name, value);
    obj.visible = value;
  }
}
