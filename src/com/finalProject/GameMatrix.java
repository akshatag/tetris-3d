package com.finalProject;

import static org.lwjgl.opengl.GL11.*;
import java.awt.Color;
import java.util.Collection;
import java.util.Stack;
import org.lwjgl.*;

public class GameMatrix {

    // Represent the center of the bottom of the matrix
    private int baseX;
    private int baseY;
    private int baseZ;
    private int spaceSize;

    private GameLogic game;

    private long prevTime;
    private Piece nextPiece;

    private static int DESCENT_INTERVAL;
    private static int MATRIX_WIDTH;
    private static int MATRIX_LENGTH;
    private static int MATRIX_HEIGHT;

    private boolean gameFinished;

    Color[][][] pieces;
    Stack<Collection<Cube>> undo;

    public GameMatrix(int x, int y, int z, int s, GameLogic g) {
        baseX = x;
        baseY = y;
        baseZ = z;
        spaceSize = s;

        game = g;

        DESCENT_INTERVAL = 1000;
        MATRIX_WIDTH = 5;
        MATRIX_LENGTH = 5;
        MATRIX_HEIGHT = 10;

        pieces = new Color[MATRIX_WIDTH][MATRIX_LENGTH][MATRIX_HEIGHT];
        Color c = new Color(255, 0, 0);

        pieces[0][0][0] = c;
        pieces[0][MATRIX_LENGTH - 1][0] = c;
        pieces[MATRIX_WIDTH - 1][0][0] = c;
        pieces[MATRIX_WIDTH - 1][MATRIX_LENGTH - 1][0] = c;

        nextPiece = new Piece(baseX, baseY + 10 * spaceSize + spaceSize / 2,
                baseZ, spaceSize);
        undo = new Stack<Collection<Cube>>();
    }

    public int getBaseZ() {
        return baseZ;
    }

    public Piece getNextPiece() {
        return nextPiece;
    }

    public void draw3D() {
        checkFinishedLevel();

        glPushMatrix();
        drawNextPiece();
        glPopMatrix();

        glPushMatrix();
        drawBase();
        glPopMatrix();

        glPushMatrix();

        for (int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces[i].length; j++) {
                for (int k = 0; k < pieces[i][j].length; k++) {
                    if (pieces[i][j][k] != null) {
                        int cx = baseX + (i - 2) * spaceSize;
                        int cz = baseZ + (j - 2) * spaceSize;
                        int cy = baseY + (k - 0) * spaceSize + spaceSize / 2;
                        Cube c = new Cube(cx, cy, cz, spaceSize / 2,
                                pieces[i][j][k]);
                        c.draw3D();
                    }
                }
            }
        }

        glPopMatrix();
    }

    public void drawNextPiece() {
        Collection<Cube> cubes = nextPiece.getCubes();
        boolean collision = false;

        for (Cube c : cubes) {
            if (detectCollision(c, 0, -1, 0)) {
                collision = true;
            }
        }

        if (collision) {
            for (Cube c : cubes) {
                int x = parseX(c.getX());
                int y = parseY(c.getY());
                int z = parseZ(c.getZ());

                try {
                    pieces[x][z][y] = c.getColor();

                    if (y == MATRIX_HEIGHT - 1) {
                        gameFinished = true;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            game.addPiecePlacedBonus(cubes.size());
            resetNextPiece();
            undo.push(cubes);

        } else if (Sys.getTime() - prevTime >= DESCENT_INTERVAL) {
            nextPiece.translate(0, -spaceSize, 0);
            prevTime = Sys.getTime();
        }

        nextPiece.draw3D();
    }

    public void drawBase() {
        glBegin(GL_QUADS);

        glColor4f(0f, 0f, 1f, 0.1f);

        glVertex3f((float) (baseX - 2.5 * spaceSize), (float) (baseY),
                (float) (baseZ - 2.5 * spaceSize));
        glVertex3f((float) (baseX + 2.5 * spaceSize), (float) (baseY),
                (float) (baseZ - 2.5 * spaceSize));
        glVertex3f((float) (baseX + 2.5 * spaceSize), (float) (baseY),
                (float) (baseZ + 2.5 * spaceSize));
        glVertex3f((float) (baseX - 2.5 * spaceSize), (float) (baseY),
                (float) (baseZ + 2.5 * spaceSize));

        glEnd();
    }

    public void rotateNextPiece(int x, int y, int z) {
        nextPiece.rotate(x, y, z);
        Collection<Cube> cubes = nextPiece.getCubes();

        for (Cube c : cubes) {
            if (detectCollision(c, 0, 0, 0)) {
                nextPiece.rotate(-x, -y, -z);
                break;
            }
        }
    }

    public boolean detectCollision(Cube c, int x, int y, int z) {
        int cx = c.getX();
        int cy = c.getY();
        int cz = c.getZ();

        int x2 = parseX(cx);
        int y2 = parseY(cy);
        int z2 = parseZ(cz);

        try {
            return pieces[x2 + x][z2 + z][y2 + y] != null;
        } catch (ArrayIndexOutOfBoundsException e) {
            if (y2 > MATRIX_HEIGHT && x2 >= 0 && x2 < MATRIX_WIDTH && z2 >= 0
                    && z2 < MATRIX_LENGTH) {
                return false;
            }
            return true;
        }
    }

    public void resetNextPiece() {
        nextPiece = new Piece(baseX, 10 * spaceSize + spaceSize / 2, baseZ,
                spaceSize);
    }

    public int parseX(int cx) {
        return MATRIX_WIDTH / 2 - (baseX - cx) / spaceSize;
    }

    public int parseY(int cy) {
        return (cy - spaceSize / 2) / spaceSize;
    }

    public int parseZ(int cz) {
        return MATRIX_LENGTH / 2 - (baseZ - cz) / spaceSize;
    }

    public void translateNextPiece(int x, int y, int z) {
        Collection<Cube> cubes = nextPiece.getCubes();

        boolean colX = false;
        boolean colY = false;
        boolean colZ = false;

        for (Cube c : cubes) {
            if (detectCollision(c, x, 0, 0)) {
                colX = true;
            }

            if (detectCollision(c, 0, y, 0)) {
                colY = true;
            }

            if (detectCollision(c, 0, 0, z)) {
                colZ = true;
            }
        }

        if (!colX) {
            nextPiece.translate(x * spaceSize, 0, 0);
        }

        if (!colY) {
            nextPiece.translate(0, y * spaceSize, 0);
        }

        if (!colZ) {
            nextPiece.translate(0, 0, z * spaceSize);
        }
    }

    public void undo() {
        if (!undo.isEmpty()) {
            game.addUndoPenalty();

            for (Cube c : undo.pop()) {

                int x = parseX(c.getX());
                int y = parseY(c.getY());
                int z = parseZ(c.getZ());

                try {
                    pieces[x][z][y] = null;
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public boolean checkFinishedLevel() {
        boolean finished = true;
        int k = pieces[0][0].length - 1; // level

        for (k = k; k >= 0; k--) {
            finished = true;
            for (int i = 0; i < pieces.length; i++) {
                for (int j = 0; j < pieces[i].length; j++) {
                    if (pieces[i][j][k] == null) {
                        finished = false;
                    }
                }
            }

            if (finished) {
                break;
            }
        }

        if (finished) {

            if (DESCENT_INTERVAL >= 200) {
                DESCENT_INTERVAL *= 0.75;
            }
            game.addLevelFinishedBonus();

            // for (int l = k; l < pieces[0][0].length - 1; l++) {
            // for (int i = 0; i < pieces.length; i++) {
            // for (int j = 0; j < pieces[i].length; j++) {
            // pieces[i][j][l] = pieces[i][j][l + 1];
            // }
            // }
            // }

            for (int i = 0; i < pieces.length; i++) {
                for (int j = 0; j < pieces[i].length; j++) {
                    pieces[i][j][k] = null;
                }
            }

            for (int i = 0; i < pieces.length; i++) {
                for (int j = 0; j < pieces[i].length; j++) {
                    Color[] replace = new Color[pieces[i][j].length];
                    int index = 0;
                    for (int l = 0; l < pieces.length; l++) {
                        if (pieces[i][j][l] != null) {
                            replace[index] = pieces[i][j][l];
                            index++;
                        }
                    }

                    pieces[i][j] = replace;
                }
            }

        }

        return finished;
    }

    public GameLogic getGame() {
        return game;
    }

    public boolean checkGameFinished() {
        return gameFinished;
    }
}
