package com.finalProject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;

public class Game implements MouseListener {

    JFrame container;
    MenuPanel menu;

    RenderFrame rndFrame;

    public Game() {

        container = new JFrame();
        container.setResizable(false);
        container.setSize(640, 480);
        container.setFocusable(true);
        container.addMouseListener(this);

        container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Calculations to render JFrame to center of screen
        Dimension dm = Toolkit.getDefaultToolkit().getScreenSize();
        int w = container.getWidth();
        int h = container.getHeight();

        container.setLocation(dm.width / 2 - w / 2, dm.height / 2 - h / 2);

        menu = new MenuPanel();

        container.getContentPane().add(menu);
        container.setVisible(true);

        while (true) {
            // System.out.println(".");
            if (rndFrame != null) {
                rndFrame.run();
                rndFrame = null;
                System.out.println("reached");
            }
            System.out.println(".");
        }
    }

    public static void main(String[] args) {
        new Game();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (menu.inNEWGAME(e.getX(), e.getY())) {
            if (rndFrame == null) {
                try {
                    rndFrame = new RenderFrame();
                    // runNewGame();
                } catch (LWJGLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(container,
                            "Could not laod new game");
                }
            }
        } else if (menu.inINSTRUCTS(e.getX(), e.getY())) {
            showInstructions();
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void showInstructions() {
        String msg = "Tetrahedris (Tetris in 3D) is similar in concept to the well known classic Tetris."
                + "\n\nThe objective of the game is to arrange the falling pieces so as to cover the entire board. You lose the game if the pile of pieces reaches a certain height."
                + "\n\nTo move the incoming piece, use the UP, DOWN, LEFT, and RIGHT arrows. To rotate the piece, hold the R key and then press the UP, DOWN, LEFT, or RIGHT arrows."
                + "The A, S, D, and W keys can be used to rotate the camera perspective."
                + "\n\nThe SPACE bar can be used to drop the piece faster. You will earn a small amount of bonus points for speeding up the game with the SPACE bar."
                + "The X key can be used to undo a move. Be warned however, this will cost points!!";

        JTextArea text = new JTextArea(6, 25);
        text.setText(msg);
        text.setLineWrap(true);
        text.setEditable(false);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(400, 400));

        JOptionPane.showMessageDialog(container, scroll, "Instructions",
                JOptionPane.PLAIN_MESSAGE);
    }

    private class MenuPanel extends JPanel {

        Rectangle NEWGAME;
        Rectangle INSTRUCTS;

        public MenuPanel() {

        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;

            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("src/bg_image"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            g2D.drawImage(img, 0, 0, null);

            FontMetrics fm = g2D.getFontMetrics();

            String title = "TETRAHEDRIS";
            String aside = "Tetris in 3D";
            String newGame = "NEW GAME";
            String instructs = "INSTRUCTIONS";

            int w;
            int h;
            int x;
            int y;

            Font fnt0 = new Font("Courier", Font.PLAIN, 40);
            g2D.setFont(fnt0);
            g2D.setColor(Color.RED);

            fm = g2D.getFontMetrics(fnt0);

            w = (int) fm.getStringBounds(title, g2D).getWidth();
            h = (int) fm.getStringBounds(title, g2D).getHeight();

            x = this.getWidth() / 2 - w / 2;
            y = 100;

            g2D.drawString(title, x, y);

            Font fnt1 = new Font("Courier", Font.ITALIC, 20);
            g2D.setFont(fnt1);

            fm = g2D.getFontMetrics(fnt1);

            w = (int) fm.getStringBounds(aside, g2D).getWidth();
            h = (int) fm.getStringBounds(aside, g2D).getHeight();

            x = this.getWidth() / 2 - w / 2;
            y = 125;

            g2D.drawString(aside, x, y);

            Font fnt2 = new Font("Courier", Font.PLAIN, 20);
            g2D.setFont(fnt2);
            g2D.setColor(Color.WHITE);

            fm = g2D.getFontMetrics(fnt1);

            w = (int) fm.getStringBounds(newGame, g2D).getWidth();
            h = (int) fm.getStringBounds(newGame, g2D).getHeight();

            x = this.getWidth() / 2 - w / 2;
            y = 220;

            NEWGAME = new Rectangle(x, y, w, h);
            g2D.drawString(newGame, x, y);

            w = (int) fm.getStringBounds(instructs, g2D).getWidth();
            h = (int) fm.getStringBounds(instructs, g2D).getHeight();

            x = this.getWidth() / 2 - w / 2;
            y = 250;

            INSTRUCTS = new Rectangle(x, y, w, h);
            g2D.drawString(instructs, x, y);
        }

        public boolean inNEWGAME(int x, int y) {
            return NEWGAME == null ? false : NEWGAME.contains(new Point(x, y));
        }

        public boolean inINSTRUCTS(int x, int y) {
            return INSTRUCTS == null ? false : INSTRUCTS.contains(new Point(x,
                    y));
        }

    }

}
