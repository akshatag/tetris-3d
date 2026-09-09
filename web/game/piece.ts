export type Axis = "x" | "y" | "z";

export interface Cell {
  x: number;
  y: number;
  z: number;
}

export type Template = boolean[][][];

const SIZE = 3;
const CENTER = 1;

const PALETTE = [
  0xff5d73, 0x4cc9f0, 0xffd166, 0x06d6a0, 0xc77dff, 0xff9f1c, 0x4361ee,
];

function emptyTemplate(): Template {
  return Array.from({ length: SIZE }, () =>
    Array.from({ length: SIZE }, () => Array.from({ length: SIZE }, () => false)),
  );
}

function chance(probability: number): boolean {
  return Math.random() <= probability;
}

/**
 * Builds a random blob around the centre cell, growing outwards with a
 * decreasing probability so most pieces stay small.
 */
function randomTemplate(): Template {
  const t = emptyTemplate();
  t[CENTER][CENTER][CENTER] = true;

  const arms: Cell[] = [
    { x: CENTER - 1, y: CENTER, z: CENTER },
    { x: CENTER + 1, y: CENTER, z: CENTER },
    { x: CENTER, y: CENTER, z: CENTER - 1 },
    { x: CENTER, y: CENTER, z: CENTER + 1 },
    { x: CENTER, y: CENTER + 1, z: CENTER },
  ];

  let step = 0;
  for (const arm of arms) {
    if (chance(0.6 - step)) {
      t[arm.x][arm.y][arm.z] = true;
      step += 0.1;
    }
  }

  return t;
}

export class Piece {
  readonly color: number;
  private template: Template;
  private origin: Cell;

  constructor(origin: Cell, color?: number, template?: Template) {
    this.origin = { ...origin };
    this.color = color ?? PALETTE[Math.floor(Math.random() * PALETTE.length)];
    this.template = template ?? randomTemplate();
  }

  get position(): Cell {
    return { ...this.origin };
  }

  /** Absolute matrix cells currently occupied by the piece. */
  cells(offset: Cell = { x: 0, y: 0, z: 0 }): Cell[] {
    const out: Cell[] = [];
    for (let i = 0; i < SIZE; i++) {
      for (let j = 0; j < SIZE; j++) {
        for (let k = 0; k < SIZE; k++) {
          if (!this.template[i][j][k]) continue;
          out.push({
            x: this.origin.x + (i - CENTER) + offset.x,
            y: this.origin.y + (j - CENTER) + offset.y,
            z: this.origin.z + (k - CENTER) + offset.z,
          });
        }
      }
    }
    return out;
  }

  translate(offset: Cell): void {
    this.origin = {
      x: this.origin.x + offset.x,
      y: this.origin.y + offset.y,
      z: this.origin.z + offset.z,
    };
  }

  /** A copy of this piece rotated a quarter turn about `axis` (dir 1 = CW). */
  rotated(axis: Axis, dir: 1 | -1): Piece {
    const next = emptyTemplate();
    for (let i = 0; i < SIZE; i++) {
      for (let j = 0; j < SIZE; j++) {
        for (let k = 0; k < SIZE; k++) {
          if (!this.template[i][j][k]) continue;
          const a = i - CENTER;
          const b = j - CENTER;
          const c = k - CENTER;

          let na = a;
          let nb = b;
          let nc = c;

          if (axis === "x") {
            nb = dir === 1 ? -c : c;
            nc = dir === 1 ? b : -b;
          } else if (axis === "y") {
            na = dir === 1 ? c : -c;
            nc = dir === 1 ? -a : a;
          } else {
            na = dir === 1 ? -b : b;
            nb = dir === 1 ? a : -a;
          }

          next[na + CENTER][nb + CENTER][nc + CENTER] = true;
        }
      }
    }
    return new Piece(this.origin, this.color, next);
  }
}
