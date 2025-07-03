import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class BrickBreakerGame extends JPanel implements KeyListener, ActionListener {
    javax.swing.Timer timer;
    int delay = 8;

    int score = 0;
    int totalBricks = 21;

    int playerX = 310;

    int ballPosX = 120;
    int ballPosY = 350;
    int ballDirX = -1;
    int ballDirY = -2;

    MapGenerator map;
    boolean play = false;
    boolean inMenu = true;
    int menuSelection = 0; // 0: Start, 1: How to Play, 2: Match History

    ArrayList<String> history = new ArrayList<>();

    public BrickBreakerGame() {
        map = new MapGenerator(3, 7);
        addKeyListener(this);
        setFocusable(true);
        loadHistory();
        timer = new javax.swing.Timer(delay, this);
        timer.start();
    }

    public void paint(Graphics g) {
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(1, 1, 692, 592);

        if (inMenu) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Brick Breaker Game", 200, 100);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString((menuSelection == 0 ? "-> " : "") + "Start Game", 250, 200);
            g.drawString((menuSelection == 1 ? "-> " : "") + "How to Play", 250, 250);
            g.drawString((menuSelection == 2 ? "-> " : "") + "Match History", 250, 300);
            return;
        }

        // Draw map
        map.draw((Graphics2D) g);

        // Borders
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 3, 592);
        g.fillRect(0, 0, 692, 3);
        g.fillRect(691, 0, 3, 592);

        // Scores
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Score: " + score, 540, 30);

        // Paddle
        g.setColor(Color.GREEN);
        g.fillRect(playerX, 550, 100, 8);

        // Ball
        g.setColor(Color.RED);
        g.fillOval(ballPosX, ballPosY, 20, 20);

        // Win
        if (totalBricks <= 0) {
            play = false;
            ballDirX = 0;
            ballDirY = 0;
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Level Cleared!", 230, 300);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press Enter for Next Level", 210, 350);
        }

        // Game Over
        if (ballPosY > 570) {
            play = false;
            ballDirX = 0;
            ballDirY = 0;
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over! Score: " + score, 170, 300);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press Enter to Restart", 230, 350);
            saveHistory();
        }

        g.dispose();
    }

    public void actionPerformed(ActionEvent e) {
        timer.start();

        if (play) {
            // Paddle collision
            if (new Rectangle(ballPosX, ballPosY, 20, 20).intersects(new Rectangle(playerX, 550, 100, 8))) {
                ballDirY = -ballDirY;
            }

            // Brick collision
            A:
            for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        Rectangle rect = new Rectangle(brickX, brickY, map.brickWidth, map.brickHeight);
                        Rectangle ballRect = new Rectangle(ballPosX, ballPosY, 20, 20);

                        if (ballRect.intersects(rect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += 5;

                            // Speed increases every 45 points
                            if (score % 45 == 0) {
                                if (ballDirX > 0) ballDirX++;
                                else ballDirX--;
                                if (ballDirY > 0) ballDirY++;
                                else ballDirY--;
                            }

                            if (ballPosX + 19 <= rect.x || ballPosX + 1 >= rect.x + rect.width) {
                                ballDirX = -ballDirX;
                            } else {
                                ballDirY = -ballDirY;
                            }
                            break A;
                        }
                    }
                }
            }

            ballPosX += ballDirX;
            ballPosY += ballDirY;

            if (ballPosX < 0) ballDirX = -ballDirX;
            if (ballPosY < 0) ballDirY = -ballDirY;
            if (ballPosX > 670) ballDirX = -ballDirX;
        }

        repaint();
    }

    public void keyPressed(KeyEvent e) {
        if (inMenu) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                menuSelection = (menuSelection + 2) % 3;
                repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                menuSelection = (menuSelection + 1) % 3;
                repaint();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (menuSelection == 0) {
                    inMenu = false;
                    resetGame();
                } else if (menuSelection == 1) {
                    JOptionPane.showMessageDialog(this,
                            "HOW TO PLAY:\n" +
                            "- Move Paddle: Left/Right Arrow Keys\n" +
                            "- Bounce Ball to Break Bricks\n" +
                            "- Clear All Bricks to Win Level\n" +
                            "- Ball Speed Increases Every 45 Points\n" +
                            "- Game Over if Ball Falls Below Paddle");
                } else if (menuSelection == 2) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : history) sb.append(s).append("\n");
                    if (sb.length() == 0) sb.append("No match history yet!");
                    JOptionPane.showMessageDialog(this, sb.toString(), "Match History", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (playerX >= 600) playerX = 600;
            else moveRight();
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (playerX <= 10) playerX = 10;
            else moveLeft();
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (!play && ballPosY > 570) {
                inMenu = true;
                repaint();
            } else if (!play && totalBricks <= 0) {
                nextLevel();
            }
        }
    }

    public void moveRight() {
        play = true;
        playerX += 20;
    }

    public void moveLeft() {
        play = true;
        playerX -= 20;
    }

    public void resetGame() {
        play = true;
        ballPosX = 120;
        ballPosY = 350;
        ballDirX = -1;
        ballDirY = -2;
        playerX = 310;
        score = 0;
        totalBricks = 21;
        map = new MapGenerator(3, 7);
        repaint();
    }

    public void nextLevel() {
        play = true;
        ballPosX = 120;
        ballPosY = 350;
        ballDirX = -1;
        ballDirY = -2;
        playerX = 310;
        totalBricks = 30;
        map = new MapGenerator(5, 6);  // New pattern
        repaint();
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void saveHistory() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("game_history.txt", true));
            writer.write("Score: " + score + "\n");
            writer.close();
            history.add("Score: " + score);
        } catch (IOException ex) {
            System.out.println("Error saving history.");
        }
    }

    public void loadHistory() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("game_history.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
            reader.close();
        } catch (IOException ex) {
            // First time, no file
        }
    }

    public static void main(String[] args) {
        JFrame obj = new JFrame();
        BrickBreakerGame game = new BrickBreakerGame();
        obj.setBounds(10, 10, 700, 600);
        obj.setTitle("Brick Breaker Game (Enhanced)");
        obj.setResizable(false);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(game);
        obj.setVisible(true);
    }

    class MapGenerator {
        int[][] map;
        int brickWidth;
        int brickHeight;

        public MapGenerator(int row, int col) {
            map = new int[row][col];
            for (int[] rowArr : map) {
                Arrays.fill(rowArr, 1);
            }
            brickWidth = 540 / col;
            brickHeight = 150 / row;
        }

        public void draw(Graphics2D g) {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (map[i][j] > 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(j * brickWidth + 80, i * brickHeight + 50, brickWidth, brickHeight);

                        g.setStroke(new BasicStroke(3));
                        g.setColor(Color.BLACK);
                        g.drawRect(j * brickWidth + 80, i * brickHeight + 50, brickWidth, brickHeight);
                    }
                }
            }
        }

        public void setBrickValue(int value, int row, int col) {
            map[row][col] = value;
        }
    }
}
