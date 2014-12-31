package com.finalProject;

import org.lwjgl.Sys;

public class GameLogic {

    private final int UNIT_UNDO_PENALTY = 100;
    private final int LEVEL_FINISHED_BONUS = 150;

    private final int Z_AXIS_SPEED_BONUS = 1;
    private final int UNIT_PIECE_PLACED_BONUS = 2;

    private double LEVEL_BONUS_INCREMENT = 0;
    private double UNDO_PENALTY_INCREMENT = 0;

    private long GAME_START_TIME;
    private long LAST_LEVEL_FINISHED_TIME;

    private int score;

    public GameLogic() {
        score = 0;
    }

    public void start() {
        GAME_START_TIME = Sys.getTime();
        LAST_LEVEL_FINISHED_TIME = GAME_START_TIME;
    }

    public int getScore() {
        return score;
    }

    public void addZSpeedBonus() {
        score += Z_AXIS_SPEED_BONUS;
    }

    public void addPiecePlacedBonus(int n) {
        score += UNIT_PIECE_PLACED_BONUS * n;
    }

    public void addLevelFinishedBonus() {
        score += LEVEL_FINISHED_BONUS;
        score += Math.min(0,
                (120000 - (Sys.getTime() - LAST_LEVEL_FINISHED_TIME)) / 1000)
                * LEVEL_BONUS_INCREMENT;
        LEVEL_BONUS_INCREMENT += 50;
        LAST_LEVEL_FINISHED_TIME = Sys.getTime();
    }

    public void addUndoPenalty() {
        score -= (int) (UNDO_PENALTY_INCREMENT * UNIT_UNDO_PENALTY);
        if (UNDO_PENALTY_INCREMENT == 0) {
            UNDO_PENALTY_INCREMENT = 1;
        } else {
            UNDO_PENALTY_INCREMENT *= 1.2;
        }
    }

}
