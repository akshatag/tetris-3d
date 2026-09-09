import * as THREE from "three";
import { MATRIX_DEPTH, MATRIX_HEIGHT, MATRIX_WIDTH } from "./game/constants";
import { Game } from "./game/game";
import { Cell } from "./game/piece";

const CUBE_SIZE = 1;
const CUBE_GEOMETRY = new THREE.BoxGeometry(CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
const EDGE_GEOMETRY = new THREE.EdgesGeometry(CUBE_GEOMETRY);
const EDGE_MATERIAL = new THREE.LineBasicMaterial({ color: 0x0b1020, opacity: 0.6, transparent: true });

/** Converts a matrix cell into world-space coordinates centred on the board. */
function toWorld(cell: Cell): THREE.Vector3 {
  return new THREE.Vector3(
    cell.x - (MATRIX_WIDTH - 1) / 2,
    cell.y + CUBE_SIZE / 2,
    cell.z - (MATRIX_DEPTH - 1) / 2,
  );
}

export class Scene {
  private renderer: THREE.WebGLRenderer;
  private scene = new THREE.Scene();
  private camera: THREE.PerspectiveCamera;
  private cubes = new THREE.Group();
  private materials = new Map<number, THREE.MeshLambertMaterial>();

  private orbit = Math.PI / 4;
  private elevation = 0.55;
  private distance = 22;

  constructor(canvas: HTMLCanvasElement) {
    this.renderer = new THREE.WebGLRenderer({ canvas, antialias: true });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    this.scene.background = new THREE.Color(0x05070f);
    this.scene.fog = new THREE.Fog(0x05070f, 30, 60);

    this.camera = new THREE.PerspectiveCamera(45, 1, 0.1, 200);

    this.scene.add(new THREE.AmbientLight(0xffffff, 1.1));

    const key = new THREE.DirectionalLight(0xffffff, 2.1);
    key.position.set(8, 16, 10);
    this.scene.add(key);

    const rim = new THREE.DirectionalLight(0x4cc9f0, 1.2);
    rim.position.set(-10, 6, -8);
    this.scene.add(rim);

    this.scene.add(this.buildBoard());
    this.scene.add(this.cubes);

    this.resize();
    window.addEventListener("resize", () => this.resize());
  }

  private buildBoard(): THREE.Group {
    const board = new THREE.Group();

    const floor = new THREE.Mesh(
      new THREE.PlaneGeometry(MATRIX_WIDTH, MATRIX_DEPTH),
      new THREE.MeshBasicMaterial({
        color: 0x4361ee,
        transparent: true,
        opacity: 0.18,
        side: THREE.DoubleSide,
      }),
    );
    floor.rotation.x = -Math.PI / 2;
    board.add(floor);

    const grid = new THREE.GridHelper(MATRIX_WIDTH, MATRIX_WIDTH, 0x4cc9f0, 0x243b6b);
    grid.position.y = 0.01;
    board.add(grid);

    const cage = new THREE.LineSegments(
      new THREE.EdgesGeometry(
        new THREE.BoxGeometry(MATRIX_WIDTH, MATRIX_HEIGHT, MATRIX_DEPTH),
      ),
      new THREE.LineBasicMaterial({ color: 0x4cc9f0, transparent: true, opacity: 0.35 }),
    );
    cage.position.y = MATRIX_HEIGHT / 2;
    board.add(cage);

    return board;
  }

  private material(color: number): THREE.MeshLambertMaterial {
    let material = this.materials.get(color);
    if (!material) {
      material = new THREE.MeshLambertMaterial({ color });
      this.materials.set(color, material);
    }
    return material;
  }

  private addCube(cell: Cell, color: number, ghost: boolean): void {
    const mesh = ghost
      ? new THREE.Mesh(
          CUBE_GEOMETRY,
          new THREE.MeshBasicMaterial({
            color,
            transparent: true,
            opacity: 0.16,
            depthWrite: false,
          }),
        )
      : new THREE.Mesh(CUBE_GEOMETRY, this.material(color));

    mesh.position.copy(toWorld(cell));
    if (!ghost) mesh.add(new THREE.LineSegments(EDGE_GEOMETRY, EDGE_MATERIAL));
    this.cubes.add(mesh);
  }

  private clearCubes(): void {
    for (const child of this.cubes.children) {
      const mesh = child as THREE.Mesh;
      const material = mesh.material as THREE.Material;
      if (material instanceof THREE.MeshBasicMaterial) material.dispose();
    }
    this.cubes.clear();
  }

  get orbitAngle(): number {
    return this.orbit;
  }

  orbitBy(delta: number): void {
    this.orbit += delta;
  }

  elevateBy(delta: number): void {
    this.elevation = THREE.MathUtils.clamp(this.elevation + delta, -0.3, 1.3);
  }

  zoomBy(delta: number): void {
    this.distance = THREE.MathUtils.clamp(this.distance + delta, 10, 40);
  }

  render(game: Game): void {
    this.clearCubes();

    for (const cell of game.settledCells()) {
      this.addCube(cell, cell.color, false);
    }
    if (!game.over) {
      for (const cell of game.ghostCells()) {
        this.addCube(cell, game.piece.color, true);
      }
      for (const cell of game.piece.cells()) {
        this.addCube(cell, game.piece.color, false);
      }
    }

    const focus = new THREE.Vector3(0, MATRIX_HEIGHT * 0.45, 0);
    const horizontal = Math.cos(this.elevation) * this.distance;
    this.camera.position.set(
      focus.x + Math.cos(this.orbit) * horizontal,
      focus.y + Math.sin(this.elevation) * this.distance,
      focus.z + Math.sin(this.orbit) * horizontal,
    );
    this.camera.lookAt(focus);

    this.renderer.render(this.scene, this.camera);
  }

  private resize(): void {
    const width = window.innerWidth;
    const height = window.innerHeight;
    this.renderer.setSize(width, height, false);
    this.camera.aspect = width / height;
    this.camera.updateProjectionMatrix();
  }
}
