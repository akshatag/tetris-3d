import {
  BASE_DESCENT_INTERVAL,
  DESCENT_SPEEDUP,
  LAYER_BONUS_INCREMENT,
  LAYER_CLEARED_BONUS,
  MATRIX_DEPTH,
  MATRIX_HEIGHT,
  MATRIX_WIDTH,
  MIN_DESCENT_INTERVAL,
  POINTS_PER_CUBE,
  POINTS_PER_SOFT_DROP,
  SOFT_DROP_INTERVAL,
  UNDO_PENALTY,
  UNDO_PENALTY_GROWTH,
} from "./constants";
import { Axis, Cell, Piece } from "./piece";

export interface FilledCell extends Cell {
  color: number;
}

interface UndoEntry {
  cells: Cell[];
  score: number;
  layersCleared: number;
}

/**
 * The 3D playfield: a MATRIX_WIDTH x MATRIX_DEPTH x MATRIX_HEIGHT grid of
 * settled cubes plus the piece currently falling into it.
 */
export class Game {
  private grid: (number | null)[][][] = [];
  private undoStack: UndoEntry[] = [];
  private undoPenaltyFactor = 0;
  private layerBonus = LAYER_CLEARED_BONUS;
  private descentTimer = 0;

  piece: Piece;
  score = 0;
  layersCleared = 0;
  over = false;

  constructor() {
    this.grid = Array.from({ length: MATRIX_WIDTH }, () =>
      Array.from({ length: MATRIX_HEIGHT }, () =>
        Array.from({ length: MATRIX_DEPTH }, () => null as number | null),
      ),
    );
    this.piece = this.spawn();
  }

  get level(): number {
    return this.layersCleared + 1;
  }

  get descentInterval(): number {
    return Math.max(
      MIN_DESCENT_INTERVAL,
      BASE_DESCENT_INTERVAL * DESCENT_SPEEDUP ** this.layersCleared,
    );
  }

  get canUndo(): boolean {
    return this.undoStack.length > 0;
  }

  settledCells(): FilledCell[] {
    const out: FilledCell[] = [];
    for (let x = 0; x < MATRIX_WIDTH; x++) {
      for (let y = 0; y < MATRIX_HEIGHT; y++) {
        for (let z = 0; z < MATRIX_DEPTH; z++) {
          const color = this.grid[x][y][z];
          if (color !== null) out.push({ x, y, z, color });
        }
      }
    }
    return out;
  }

  /** Where the falling piece would come to rest if dropped straight down. */
  ghostCells(): Cell[] {
    let drop = 0;
    while (!this.collides(this.piece.cells({ x: 0, y: -(drop + 1), z: 0 }))) {
      drop++;
    }
    return this.piece.cells({ x: 0, y: -drop, z: 0 });
  }

  /** Advances gravity; call once per frame with the elapsed milliseconds. */
  tick(elapsed: number, softDrop: boolean): void {
    if (this.over) return;

    this.descentTimer += elapsed;
    const interval = softDrop
      ? Math.min(this.descentInterval, SOFT_DROP_INTERVAL)
      : this.descentInterval;

    while (this.descentTimer >= interval) {
      this.descentTimer -= interval;
      if (softDrop) this.score += POINTS_PER_SOFT_DROP;
      this.descend();
      if (this.over) return;
    }
  }

  move(dx: number, dz: number): boolean {
    const offset = { x: dx, y: 0, z: dz };
    if (this.collides(this.piece.cells(offset))) return false;
    this.piece.translate(offset);
    return true;
  }

  rotate(axis: Axis, dir: 1 | -1): boolean {
    const rotated = this.piece.rotated(axis, dir);
    if (this.collides(rotated.cells())) return false;
    this.piece = rotated;
    return true;
  }

  hardDrop(): void {
    if (this.over) return;
    while (!this.collides(this.piece.cells({ x: 0, y: -1, z: 0 }))) {
      this.piece.translate({ x: 0, y: -1, z: 0 });
    }
    this.descentTimer = 0;
    this.descend();
  }

  undo(): boolean {
    const entry = this.undoStack.pop();
    if (!entry) return false;

    for (const cell of entry.cells) {
      this.grid[cell.x][cell.y][cell.z] = null;
    }

    this.score = entry.score - Math.round(this.undoPenaltyFactor * UNDO_PENALTY);
    this.layersCleared = entry.layersCleared;
    this.undoPenaltyFactor =
      this.undoPenaltyFactor === 0 ? 1 : this.undoPenaltyFactor * UNDO_PENALTY_GROWTH;

    this.piece = this.spawn();
    this.over = false;
    return true;
  }

  private descend(): void {
    const offset = { x: 0, y: -1, z: 0 };
    if (!this.collides(this.piece.cells(offset))) {
      this.piece.translate(offset);
      return;
    }
    this.lock();
  }

  private lock(): void {
    const cells = this.piece.cells();
    const snapshot: UndoEntry = {
      cells,
      score: this.score,
      layersCleared: this.layersCleared,
    };

    for (const cell of cells) {
      if (!this.inBounds(cell)) {
        this.over = true;
        return;
      }
      this.grid[cell.x][cell.y][cell.z] = this.piece.color;
    }

    this.score += POINTS_PER_CUBE * cells.length;
    this.undoStack.push(snapshot);

    const cleared = this.clearFullLayers();
    if (cleared > 0) {
      this.undoStack = [];
      this.layersCleared += cleared;
      for (let i = 0; i < cleared; i++) {
        this.score += this.layerBonus;
        this.layerBonus += LAYER_BONUS_INCREMENT;
      }
    }

    this.piece = this.spawn();
    if (this.collides(this.piece.cells())) {
      this.over = true;
    }
  }

  /** Removes every completely filled horizontal layer, collapsing the rest down. */
  private clearFullLayers(): number {
    let cleared = 0;

    for (let y = 0; y < MATRIX_HEIGHT; y++) {
      let full = true;
      for (let x = 0; x < MATRIX_WIDTH && full; x++) {
        for (let z = 0; z < MATRIX_DEPTH && full; z++) {
          if (this.grid[x][y][z] === null) full = false;
        }
      }
      if (!full) continue;

      cleared++;
      for (let above = y; above < MATRIX_HEIGHT - 1; above++) {
        for (let x = 0; x < MATRIX_WIDTH; x++) {
          for (let z = 0; z < MATRIX_DEPTH; z++) {
            this.grid[x][above][z] = this.grid[x][above + 1][z];
          }
        }
      }
      for (let x = 0; x < MATRIX_WIDTH; x++) {
        for (let z = 0; z < MATRIX_DEPTH; z++) {
          this.grid[x][MATRIX_HEIGHT - 1][z] = null;
        }
      }
      y--;
    }

    return cleared;
  }

  private spawn(): Piece {
    this.descentTimer = 0;
    return new Piece(this.spawnOrigin());
  }

  private spawnOrigin(): Cell {
    return {
      x: Math.floor(MATRIX_WIDTH / 2),
      y: MATRIX_HEIGHT - 2,
      z: Math.floor(MATRIX_DEPTH / 2),
    };
  }

  private collides(cells: Cell[]): boolean {
    return cells.some(
      (cell) => !this.inBounds(cell) || this.grid[cell.x][cell.y][cell.z] !== null,
    );
  }

  private inBounds(cell: Cell): boolean {
    return (
      cell.x >= 0 &&
      cell.x < MATRIX_WIDTH &&
      cell.y >= 0 &&
      cell.y < MATRIX_HEIGHT &&
      cell.z >= 0 &&
      cell.z < MATRIX_DEPTH
    );
  }
}
