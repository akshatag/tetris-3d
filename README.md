# Tetrahedris — Tetris in 3D

A browser rebuild of the original Java/LWJGL "Tetrahedris" project, using
[three.js](https://threejs.org/), TypeScript and Vite.

Pieces fall into a 5 × 5 × 10 well. Fill a complete 5 × 5 horizontal layer and it
clears, everything above collapses down, and the game speeds up. The game ends
when a newly spawned piece has nowhere to go.

## Running it

```bash
npm install
npm run dev      # http://localhost:5173
```

```bash
npm run build    # typecheck + production bundle into dist/
npm run preview  # serve the production bundle
```

## Controls

| Key | Action |
| --- | --- |
| Arrow keys | Move the piece (relative to the current camera angle) |
| Hold `R` + arrow keys | Rotate about the screen's horizontal / depth axis |
| `Space` | Rotate about the vertical axis |
| `Shift` | Soft drop (small point bonus per step) |
| `Enter` | Hard drop |
| `W` / `S` | Raise / lower the camera |
| `A` / `D` | Orbit the camera |
| Mouse drag / wheel | Orbit / zoom the camera |
| `X` | Undo the last landed piece (escalating point penalty) |
| `P` | Pause |

## Layout

- `web/game/` — rendering-independent game model: `piece.ts` (3×3×3 templates and
  rotations), `game.ts` (the well, collisions, layer clearing, scoring, undo),
  `constants.ts` (board size, speeds, scoring).
- `web/scene.ts` — three.js scene, board cage, cube meshes, ghost preview, camera.
- `web/main.ts` — input handling, HUD, menu/pause/game-over overlay, game loop.

## Original Java version

The original LWJGL desktop implementation is preserved under `src/com/finalProject/`,
along with its design notes in `src/README`.
