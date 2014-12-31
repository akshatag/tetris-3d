package com.finalProject;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

public class RenderFrame {

    Frame frame;
    Renderer rnd;
    Sidebar sb;

    public RenderFrame() throws LWJGLException {
        frame = new Frame("Tetrahedris!");

        frame.setSize(840, 480);
        frame.setResizable(false);
        frame.setFocusable(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("closed");
                // Display.destroy();
                frame.dispose();
                // System.out.println("disposed");
            }
        });

        Canvas canvas = new Canvas();
        canvas.setSize(new Dimension(Display.getWidth(), Display.getHeight()));
        Display.setParent(canvas);

        frame.add(canvas, BorderLayout.CENTER);
        canvas.requestFocus();

        sb = new Sidebar();
        sb.repaint();

        frame.add(sb, BorderLayout.EAST);

        frame.setVisible(true);

        canvas.requestFocus();

    }

    public void run() {
        rnd = new Renderer();

        do {
            rnd.run();
            sb.repaint();
        } while (frame != null && frame.isVisible()
                && !Display.isCloseRequested() && !rnd.checkGameFinished());

        System.out.println("did it get here");

        if (rnd.checkGameFinished() && frame != null) {
            JOptionPane.showMessageDialog(frame, "You Lost!");
        }

        Display.destroy();
        if (frame != null) {
            frame.dispose();
        }
        System.out.println("finished run loop");
    }

    private class Sidebar extends JPanel {

        public Sidebar() {
            this.setSize(new Dimension(200, 480));
            this.setPreferredSize(new Dimension(200, 480));
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;

            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("src/bg_image"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            g2D.drawImage(img, 0, 0, null);

            FontMetrics fm = g2D.getFontMetrics();
            String scoreLabel = "";

            if (rnd != null) {
                scoreLabel = "Score: " + rnd.getScore();
            } else {
                scoreLabel = "Score: 0";
            }
            String restart = "Tetris in 3D";
            String gameStart = "NEW GAME";
            String instructs = "INSTRUCTIONS";

            int w;
            int h;
            int x;
            int y;

            Font fnt0 = new Font("Courier", Font.PLAIN, 20);
            g2D.setFont(fnt0);
            g2D.setColor(Color.WHITE);

            fm = g2D.getFontMetrics(fnt0);

            w = (int) fm.getStringBounds(scoreLabel, g2D).getWidth();
            h = (int) fm.getStringBounds(scoreLabel, g2D).getHeight();

            x = this.getWidth() / 2 - w / 2;
            y = 100;

            g2D.drawString(scoreLabel, x, y);
        }
    }
}
