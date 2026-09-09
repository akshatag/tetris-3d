import { Game } from "./game/game";
import { Axis } from "./game/piece";
import { Scene } from "./scene";

const canvas = document.getElementById("scene") as HTMLCanvasElement;
const overlay = document.getElementById("overlay") as HTMLDivElement;
const overlayText = document.getElementById("overlay-text") as HTMLParagraphElement;
const startButton = document.getElementById("start") as HTMLButtonElement;
const instructionsToggle = document.getElementById("instructions-toggle") as HTMLButtonElement;
const instructions = document.getElementById("instructions") as HTMLDivElement;
const scoreLabel = document.getElementById("score") as HTMLElement;
const levelLabel = document.getElementById("level") as HTMLElement;
const layersLabel = document.getElementById("layers") as HTMLElement;

const scene = new Scene(canvas);
const held = new Set<string>();

let game = new Game();
let running = false;
let paused = false;
let lastFrame = performance.now();

/**
 * Arrow keys should feel like they move the piece across the screen, so screen
 * deltas (right, down) are resolved against the board axes using the camera's
 * orbit angle snapped to the nearest quarter turn.
 */
function screenToBoard(right: number, down: number): { x: number; z: number } {
  const quarter = Math.PI / 2;
  const angle = Math.round(scene.orbitAngle / quarter) * quarter;
  const sin = Math.round(Math.sin(angle));
  const cos = Math.round(Math.cos(angle));
  return {
    x: right * sin + down * cos,
    z: -right * cos + down * sin,
  };
}

/** The board axis (and spin direction) matching a screen-space rotation axis. */
function screenAxis(right: number, down: number): { axis: Axis; sign: 1 | -1 } {
  const board = screenToBoard(right, down);
  if (board.x !== 0) return { axis: "x", sign: board.x > 0 ? 1 : -1 };
  return { axis: "z", sign: board.z > 0 ? 1 : -1 };
}

function rotateAboutScreenAxis(right: number, down: number, dir: 1 | -1): void {
  const { axis, sign } = screenAxis(right, down);
  game.rotate(axis, (sign * dir) as 1 | -1);
}

function updateHud(): void {
  scoreLabel.textContent = String(game.score);
  levelLabel.textContent = String(game.level);
  layersLabel.textContent = String(game.layersCleared);
}

function showOverlay(title: string, text: string, button: string): void {
  (overlay.querySelector("h1") as HTMLHeadingElement).textContent = title;
  overlayText.textContent = text;
  startButton.textContent = button;
  overlay.hidden = false;
}

function startGame(): void {
  game = new Game();
  running = true;
  paused = false;
  overlay.hidden = true;
  instructions.hidden = true;
  lastFrame = performance.now();
  updateHud();
}

function endGame(): void {
  running = false;
  showOverlay(
    "GAME OVER",
    `The stack reached the top. Final score: ${game.score} with ${game.layersCleared} layer(s) cleared.`,
    "PLAY AGAIN",
  );
}

function handleKeyDown(event: KeyboardEvent): void {
  held.add(event.key.toLowerCase());

  if (!running) {
    if (event.key === "Enter") startGame();
    return;
  }

  if (event.key.toLowerCase() === "p") {
    paused = !paused;
    if (paused) {
      showOverlay("PAUSED", "Take a breath. The board is waiting.", "RESUME");
    } else {
      overlay.hidden = true;
      lastFrame = performance.now();
    }
    return;
  }

  if (paused) return;

  const rotating = held.has("r");

  switch (event.key) {
    case "ArrowLeft": {
      if (rotating) rotateAboutScreenAxis(0, -1, -1);
      else {
        const d = screenToBoard(-1, 0);
        game.move(d.x, d.z);
      }
      break;
    }
    case "ArrowRight": {
      if (rotating) rotateAboutScreenAxis(0, -1, 1);
      else {
        const d = screenToBoard(1, 0);
        game.move(d.x, d.z);
      }
      break;
    }
    case "ArrowUp": {
      if (rotating) rotateAboutScreenAxis(1, 0, 1);
      else {
        const d = screenToBoard(0, -1);
        game.move(d.x, d.z);
      }
      break;
    }
    case "ArrowDown": {
      if (rotating) rotateAboutScreenAxis(1, 0, -1);
      else {
        const d = screenToBoard(0, 1);
        game.move(d.x, d.z);
      }
      break;
    }
    case " ":
      game.rotate("y", 1);
      break;
    case "Enter":
      game.hardDrop();
      break;
    default:
      if (event.key.toLowerCase() === "x") game.undo();
      break;
  }

  if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", " ", "Enter"].includes(event.key)) {
    event.preventDefault();
  }

  updateHud();
}

function handleCamera(elapsed: number): void {
  const rate = elapsed / 1000;
  if (held.has("a")) scene.orbitBy(-1.6 * rate);
  if (held.has("d")) scene.orbitBy(1.6 * rate);
  if (held.has("w")) scene.elevateBy(0.9 * rate);
  if (held.has("s")) scene.elevateBy(-0.9 * rate);
}

function frame(now: number): void {
  const elapsed = Math.min(now - lastFrame, 200);
  lastFrame = now;

  handleCamera(elapsed);

  if (running && !paused) {
    game.tick(elapsed, held.has("shift"));
    updateHud();
    if (game.over) endGame();
  }

  scene.render(game);
  requestAnimationFrame(frame);
}

window.addEventListener("keydown", handleKeyDown);
window.addEventListener("keyup", (event) => held.delete(event.key.toLowerCase()));
window.addEventListener("blur", () => held.clear());

canvas.addEventListener("wheel", (event) => {
  event.preventDefault();
  scene.zoomBy(event.deltaY * 0.01);
}, { passive: false });

let dragging = false;
canvas.addEventListener("pointerdown", (event) => {
  dragging = true;
  canvas.setPointerCapture(event.pointerId);
});
canvas.addEventListener("pointerup", (event) => {
  dragging = false;
  canvas.releasePointerCapture(event.pointerId);
});
canvas.addEventListener("pointermove", (event) => {
  if (!dragging) return;
  scene.orbitBy(event.movementX * 0.005);
  scene.elevateBy(-event.movementY * 0.004);
});

startButton.addEventListener("click", () => {
  if (paused) {
    paused = false;
    overlay.hidden = true;
    lastFrame = performance.now();
    return;
  }
  startGame();
});

instructionsToggle.addEventListener("click", () => {
  instructions.hidden = !instructions.hidden;
});

updateHud();
requestAnimationFrame(frame);
