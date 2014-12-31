package com.finalProject;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.opengl.GL11.*;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.*;

public class Renderer {
    
    private final int height = 480; 
    private final int width = 640;
    
    private float viewParameter = -90f;
    private float cameraHeight = 210f; 
    
    private GameLogic game; 
    private GameMatrix gmx;
    
    private float gmxFocusX;
    private float gmxFocusZ;
    private float gmxFocusY;
    
    private char lastKey; 
       
    public Renderer(){
        try{
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle("Tetris 3D");
            Display.setVSyncEnabled(true);
            Display.create(new PixelFormat(0, 24, 8, 8));
        }catch(LWJGLException e){
            e.printStackTrace();
        }
        
        game = new GameLogic();
        gmx = new GameMatrix(0, 0, -500, 50, game);
        gmxFocusX = 0;
        gmxFocusY = 200;
        gmxFocusZ = gmx.getBaseZ();
        
        init();
    }
    
    public void init(){
        glViewport(0, 0, width, height);
        
        glMatrixMode(GL_PROJECTION); 
        glLoadIdentity();
        
        glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0f, 1000f);    
        //gluPerspective(100f, 640f/480f, 10f, 800f);
        //glOrtho(0f, 640f, 480f, 0f, -1f, 500f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        rotateCamera();
    }
    
    public void run() {
        checkInputs();
        redraw();

        Display.update();
        Display.sync(60);
    }
    
    public void checkInputs() {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            System.exit(0);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            cameraHeight += 6;
            rotateCamera();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            cameraHeight -= 6;
            rotateCamera();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            viewParameter += 2;
            rotateCamera();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            viewParameter -= 2;
            rotateCamera();
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
            if (lastKey == 'X') {
            } else {
                gmx.undo();
            }

            lastKey = 'X';
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            if (lastKey == 'L') {
            } else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                gmx.rotateNextPiece(0, 1, 0);
            } else {
                gmx.translateNextPiece(-1, 0, 0);
            }

            lastKey = 'L';
        } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            if (lastKey == 'R') {
            } else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                gmx.rotateNextPiece(0, -1, 0);
            } else {
                gmx.translateNextPiece(1, 0, 0);
            }

            lastKey = 'R';
        } else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            if (lastKey == 'U') {
            } else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                gmx.rotateNextPiece(1, 0, 0);
            } else {
                gmx.translateNextPiece(0, 0, -1);
            }

            lastKey = 'U';
        } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            if (lastKey == 'D') {
            } else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                gmx.rotateNextPiece(-1, 1, 0);
            } else {
                gmx.translateNextPiece(0, 0, 1);
            }

            lastKey = 'D';
        } else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            if (lastKey == 'S') {
            } else if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                gmx.rotateNextPiece(0, 0, 1);
            } else {
                gmx.translateNextPiece(0, -1, 0);
            }

            lastKey = 'S';
        } else {
            lastKey = ' ';
        }

    }
    
    public void rotateCamera(){
        float t = (float)Math.toRadians(viewParameter);
        
        float cameraX = (float)((Math.abs(gmx.getBaseZ()))*Math.cos(t));
        float cameraZ = (float)((gmx.getBaseZ()*Math.sin(t)) + gmx.getBaseZ());
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        gluLookAt(cameraX, cameraHeight, cameraZ, gmxFocusX, gmxFocusY, gmxFocusZ, 0f, 1f, 0f);
    }
    
    public void redraw(){
        
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        
        glEnable(GL_LINE_SMOOTH);
        glEnable(GL_POINT_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
        
        glClearColor(1, 1, 1, 0);
        glClearDepth(1f);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        
        gmx.draw3D();        
    }
    
    public int getScore(){
        return game.getScore();
    }
    
    public boolean checkGameFinished(){
        return gmx.checkGameFinished();
    }
    
}
