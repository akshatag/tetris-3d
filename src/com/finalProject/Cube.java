package com.finalProject;

import static org.lwjgl.opengl.GL11.*;
import java.awt.Color;

public class Cube implements Shape {

    private int centerX;
    private int centerY;
    private int centerZ;

    private int rotateX;
    private int rotateY;
    private int rotateZ;

    private int sideLength;
    private Color color;

    public Cube(int x, int y, int z, int s, Color c) {
        centerX = x;
        centerY = y;
        centerZ = z;

        rotateX = 0;
        rotateY = 0;
        rotateZ = 0;

        sideLength = s;
        color = c;
    }

    public int getX() {
        return centerX;
    }

    public int getY() {
        return centerY;
    }

    public int getZ() {
        return centerZ;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void translate(int x, int y, int z) {
        centerX += x;
        centerY += y;
        centerZ += z;
    }

    @Override
    public void rotate(int x, int y, int z) {
        rotateX = x;
        rotateY = y;
        rotateZ = z;
    }

    public void draw3D() {
        glPushMatrix();

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glColor3f(0f, 0f, 0f);
        glLineWidth(0.8f);

        drawVertices();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_POLYGON_OFFSET_FILL);
        glEnable(GL_POLYGON_SMOOTH);
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        glPolygonOffset(0f, 0f);
        glColor3f((float) color.getRed() / 255, (float) color.getGreen() / 255,
                (float) color.getBlue() / 255);

        drawVertices();

        glDisable(GL_POLYGON_SMOOTH);
        glDisable(GL_POLYGON_OFFSET_FILL);

        glPopMatrix();
    }

    // @Override
    public void drawVertices() {

        // For short hand purposes
        int x = centerX;
        int y = centerY;
        int z = centerZ;
        int s = sideLength;

        glPushMatrix();

        glTranslatef(centerX, centerY, centerZ);
        glRotatef(rotateX, 1f, 0f, 0f);
        glRotatef(rotateY, 0f, 1f, 0f);
        glRotatef(rotateZ, 0f, 0f, 1f);

        glBegin(GL_QUADS);

        // Front face
        glVertex3f(-s, -s, s); // Top Left
        glVertex3f(s, -s, s); // Top Right
        glVertex3f(s, s, s); // Bottom Right
        glVertex3f(-s, s, s); // Bottom Left

        // Bottom face
        glVertex3f(-s, s, s);
        glVertex3f(s, s, s);
        glVertex3f(s, s, -s);
        glVertex3f(-s, s, -s);

        // Back face
        glVertex3f(s, -s, -s);
        glVertex3f(-s, -s, -s);
        glVertex3f(-s, s, -s);
        glVertex3f(s, s, -s);

        // Top face
        glVertex3f(-s, -s, s);
        glVertex3f(s, -s, s);
        glVertex3f(s, -s, -s);
        glVertex3f(-s, -s, -s);

        // Left face
        glVertex3f(-s, -s, s);
        glVertex3f(-s, -s, -s);
        glVertex3f(-s, s, -s);
        glVertex3f(-s, s, s);

        // Right face
        glVertex3f(s, -s, s);
        glVertex3f(s, -s, -s);
        glVertex3f(s, s, -s);
        glVertex3f(s, s, s);

        glEnd();

        glPopMatrix();
    }

}
