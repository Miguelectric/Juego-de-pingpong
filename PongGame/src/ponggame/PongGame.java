
package ponggame;

/*
 * PongGame.java
 * Juego Pong simple en una sola clase Java (Swing).
 * Controles:
 *  - Jugador izquierdo: W (arriba), S (abajo)
 *  - Jugador derecho: Flecha Arriba, Flecha Abajo
 *  - P: pausar/reanudar
 *  - R: reiniciar puntuaciones y posiciones
 *  - A: alternar IA para el jugador derecho
 *
 * Compilar y ejecutar:
 *  javac PongGame.java
 *  java PongGame
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PongGame extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;

    // Paddles
    private static final int PADDLE_WIDTH = 12;
    private static final int PADDLE_HEIGHT = 80;
    private double leftY = HEIGHT/2 - PADDLE_HEIGHT/2;
    private double rightY = HEIGHT/2 - PADDLE_HEIGHT/2;
    private double leftSpeed = 0;
    private double rightSpeed = 0;
    private static final double PADDLE_SPEED = 6.0;

    // Ball
    private double ballX = WIDTH/2.0;
    private double ballY = HEIGHT/2.0;
    private double ballVX = 4;
    private double ballVY = 3;
    private static final int BALL_SIZE = 14;

    // Score
    private int scoreLeft = 0;
    private int scoreRight = 0;

    // Game state
    private Timer timer;
    private boolean paused = false;
    private boolean rightIsAI = false;

    public PongGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(10, this);
        timer.start();
    }

    private void resetPositions() {
        leftY = HEIGHT/2 - PADDLE_HEIGHT/2;
        rightY = HEIGHT/2 - PADDLE_HEIGHT/2;
        ballX = WIDTH/2.0;
        ballY = HEIGHT/2.0;
        // Randomize initial direction
        ballVX = (Math.random() < 0.5) ? 4 : -4;
        ballVY = (Math.random() < 0.5) ? 3 : -3;
    }

    private void resetGame() {
        scoreLeft = 0;
        scoreRight = 0;
        resetPositions();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Center line
        g2.setColor(Color.DARK_GRAY);
        for (int y = 0; y < HEIGHT; y += 20) {
            g2.fillRect(WIDTH/2 - 2, y, 4, 12);
        }

        // Paddles
        g2.setColor(Color.WHITE);
        g2.fillRect(20, (int) leftY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2.fillRect(WIDTH - 20 - PADDLE_WIDTH, (int) rightY, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Ball
        g2.fillOval((int) ballX, (int) ballY, BALL_SIZE, BALL_SIZE);

        // Scores
        g2.setFont(new Font("Consolas", Font.BOLD, 36));
        String sLeft = String.valueOf(scoreLeft);
        String sRight = String.valueOf(scoreRight);
        int strW = g2.getFontMetrics().stringWidth(sLeft);
        g2.drawString(sLeft, WIDTH/2 - 50 - strW/2, 50);
        g2.drawString(sRight, WIDTH/2 + 50 - g2.getFontMetrics().stringWidth(sRight)/2, 50);

        // Info
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("W/S: Jugador izquierdo", 10, HEIGHT - 40);
        g2.drawString("Flechas Arriba/Abajo: Jugador derecho", 10, HEIGHT - 25);
        g2.drawString("P: Pausa | R: Reiniciar | A: Alternar IA (derecha)", 10, HEIGHT - 10);

        if (paused) {
            String msg = "PAUSADO";
            g2.setFont(new Font("Consolas", Font.BOLD, 48));
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, WIDTH/2 - w/2, HEIGHT/2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (paused) return;

        // Update paddles according to speed
        leftY += leftSpeed;
        rightY += rightSpeed;

        // Keep paddles inside bounds
        leftY = Math.max(0, Math.min(HEIGHT - PADDLE_HEIGHT, leftY));
        rightY = Math.max(0, Math.min(HEIGHT - PADDLE_HEIGHT, rightY));

        // Simple AI for right paddle if enabled
        if (rightIsAI) {
            double centerR = rightY + PADDLE_HEIGHT/2.0;
            if (centerR < ballY - 10) {
                rightY += 4.0; // AI speed
            } else if (centerR > ballY + 10) {
                rightY -= 4.0;
            }
            // Clamp
            rightY = Math.max(0, Math.min(HEIGHT - PADDLE_HEIGHT, rightY));
        }

        // Update ball
        ballX += ballVX;
        ballY += ballVY;

        // Wall collision
        if (ballY <= 0) {
            ballY = 0;
            ballVY = -ballVY;
        } else if (ballY + BALL_SIZE >= HEIGHT) {
            ballY = HEIGHT - BALL_SIZE;
            ballVY = -ballVY;
        }

        // Left paddle collision
        if (ballX <= 20 + PADDLE_WIDTH && ballX >= 20 - BALL_SIZE) {
            if (ballY + BALL_SIZE >= leftY && ballY <= leftY + PADDLE_HEIGHT) {
                ballX = 20 + PADDLE_WIDTH; // prevent sticking
                ballVX = Math.abs(ballVX); // ensure positive
                // modify Y velocity based on where it hits the paddle
                double hitPos = (ballY + BALL_SIZE/2.0) - (leftY + PADDLE_HEIGHT/2.0);
                ballVY = hitPos / (PADDLE_HEIGHT/2.0) * 5;
                // slightly increase speed
                ballVX *= 1.03;
            }
        }

        // Right paddle collision
        if (ballX + BALL_SIZE >= WIDTH - 20 - PADDLE_WIDTH && ballX + BALL_SIZE <= WIDTH - 20 + BALL_SIZE) {
            if (ballY + BALL_SIZE >= rightY && ballY <= rightY + PADDLE_HEIGHT) {
                ballX = WIDTH - 20 - PADDLE_WIDTH - BALL_SIZE;
                ballVX = -Math.abs(ballVX);
                double hitPos = (ballY + BALL_SIZE/2.0) - (rightY + PADDLE_HEIGHT/2.0);
                ballVY = hitPos / (PADDLE_HEIGHT/2.0) * 5;
                ballVX *= 1.03;
            }
        }

        // Score
        if (ballX < -BALL_SIZE) {
            scoreRight++;
            resetPositions();
        } else if (ballX > WIDTH + BALL_SIZE) {
            scoreLeft++;
            resetPositions();
        }

        repaint();
    }

    // KeyListener
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W) {
            leftSpeed = -PADDLE_SPEED;
        } else if (k == KeyEvent.VK_S) {
            leftSpeed = PADDLE_SPEED;
        } else if (k == KeyEvent.VK_UP) {
            rightSpeed = -PADDLE_SPEED;
            rightIsAI = false; // if player presses keys, turn off AI
        } else if (k == KeyEvent.VK_DOWN) {
            rightSpeed = PADDLE_SPEED;
            rightIsAI = false;
        } else if (k == KeyEvent.VK_P) {
            paused = !paused;
        } else if (k == KeyEvent.VK_R) {
            resetGame();
        } else if (k == KeyEvent.VK_A) {
            rightIsAI = !rightIsAI;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_S) {
            leftSpeed = 0;
        } else if (k == KeyEvent.VK_UP || k == KeyEvent.VK_DOWN) {
            rightSpeed = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pong - Juego en Java");
            PongGame game = new PongGame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // focus the panel so it receives keyboard events
            game.requestFocusInWindow();
        });
    }
}
