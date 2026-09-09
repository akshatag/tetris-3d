export const MATRIX_WIDTH = 5;
export const MATRIX_DEPTH = 5;
export const MATRIX_HEIGHT = 10;

/** Milliseconds between automatic descents at level 1. */
export const BASE_DESCENT_INTERVAL = 1000;
/** Descent interval is scaled by this factor per level, down to MIN_DESCENT_INTERVAL. */
export const DESCENT_SPEEDUP = 0.75;
export const MIN_DESCENT_INTERVAL = 200;

export const SOFT_DROP_INTERVAL = 60;

export const POINTS_PER_CUBE = 2;
export const POINTS_PER_SOFT_DROP = 1;
export const LAYER_CLEARED_BONUS = 150;
export const LAYER_BONUS_INCREMENT = 50;
export const UNDO_PENALTY = 100;
export const UNDO_PENALTY_GROWTH = 1.2;
