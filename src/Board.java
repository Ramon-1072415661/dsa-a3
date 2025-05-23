import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Board extends JPanel implements ActionListener {
    private static final int ROWS = 20, COLS = 10, CELL_SIZE = 48, CELL_SHADOW_BEVEL = CELL_SIZE / 5;
    private static int DELAY = 400;

    private Color[][] grid = new Color[ROWS][COLS];
    private final Timer timer = new Timer(DELAY, this);
    private Tetromino current_p1 = getInitialTetromino(2);
    private Tetromino current_p2 = getInitialTetromino(7);

    private final CellRenderer cellRenderer = new CellRenderer(CELL_SIZE, CELL_SHADOW_BEVEL);

    // Game state flag
    private boolean gameOver = false;

    private void startGame() {
        current_p1 = getInitialTetromino(2);
        current_p2 = getInitialTetromino(7);
        grid = new Color[ROWS][COLS];
        gameOver = false;
        timer.start();
    }

    private Tetromino getInitialTetromino(int initialPosition) {
        Tetromino tetromino = Tetromino.random();
        tetromino.x = initialPosition;
        return tetromino;
    }

    public Board() {
        setFocusable(true);
        startGame();

        // Add key listener for game controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();

                // If game is over, check for space key to restart
                if (gameOver) {
                    if (keyCode == KeyEvent.VK_SPACE) {
                        startGame();
                    }
                    return;
                }

                // Normal game controls
                if (keyCode == KeyEvent.VK_LEFT) {
                    move(-1, current_p2);
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    move(1, current_p2);
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    drop(current_p2);
                } else if (keyCode == KeyEvent.VK_UP) {
                    rotate(current_p2);
                } else if (keyCode == KeyEvent.VK_A) {
                    move(-1, current_p1);
                } else if (keyCode == KeyEvent.VK_D) {
                    move(1, current_p1);
                } else if (keyCode == KeyEvent.VK_S) {
                    drop(current_p1);
                } else if (keyCode == KeyEvent.VK_W) {
                    rotate(current_p1);
                }
                repaint();
            }
        });

        // Add mouse listener for click to restart
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameOver) {
                    startGame();
                    repaint();
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        boolean p1Collided = collides(0, 1, current_p1);
        boolean p2Collided = collides(0, 1, current_p2);

        if (!p1Collided) {
            current_p1.y++;
        } else {
            fixToGrid(current_p1);
            clearLines();
            current_p1 = Tetromino.random();
            current_p1.x = 2;
        }

        if (!p2Collided) {
            current_p2.y++;
        } else {
            fixToGrid(current_p2);
            clearLines();
            current_p2 = Tetromino.random();
            current_p2.x = 7;
        }

        if ((p1Collided && collides(0, 0, current_p1)) || (p2Collided && collides(0, 0, current_p2))) {
            gameOver = true;
            timer.stop();
        }

        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintBackground(g);
        paintFilledCells(g);

        if (!gameOver) {
            paintCurrentShapeCells(g, current_p1);
            paintCurrentShapeCells(g, current_p2);
        } else {
            paintGameOverOverlay(g);
        }
    }

    private void paintGameOverOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, COLS * CELL_SIZE, ROWS * CELL_SIZE);

        // Game over message
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(Color.WHITE);
        String gameOverText = "GAME OVER";
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(gameOverText);
        g2d.drawString(gameOverText, (COLS * CELL_SIZE - textWidth) / 2, ROWS * CELL_SIZE / 2 - 20);

        // Retry message
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String retryText = "Press SPACE or click to retry";
        textWidth = fontMetrics.stringWidth(retryText);
        FontMetrics smallerMetrics = g2d.getFontMetrics();
        textWidth = smallerMetrics.stringWidth(retryText);
        g2d.drawString(retryText, (COLS * CELL_SIZE - textWidth) / 2, ROWS * CELL_SIZE / 2 + 20);

        g2d.dispose();
    }

    private void fixToGrid(Tetromino tetromino) {
        for (int row = 0; row < tetromino.shape.length; row++) {
            for (int col = 0; col < tetromino.shape[0].length; col++) {
                if (tetromino.shape[row][col] == 1) {
                    grid[tetromino.y + row][tetromino.x + col] = tetromino.color;
                }
            }
        }
    }

    private void clearLines() {
        for (int row = ROWS - 1; row >= 0; row--) {
            boolean full = true;

            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == null) {
                    full = false;
                    break;
                }
            }

            if (full) {
                for (int r = row; r > 0; r--) {
                    System.arraycopy(grid[r - 1], 0, grid[r], 0, COLS);
                }
                grid[0] = new Color[COLS];
                row++;
            }
        }
    }

    private void paintBackground(Graphics g) {
        int finalX = CELL_SIZE * COLS;
        int finalY = CELL_SIZE * ROWS;
        int strokeWidth = 4;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0x21222c));
        g2.fillRect(0, 0, finalX, finalY);

        g2.setStroke(new BasicStroke(strokeWidth));
        g2.setColor(new Color(0xBD93F9));
        g2.drawRect(0, 0, finalX + strokeWidth, finalY + strokeWidth);

        g2.dispose();
    }

    private void paintFilledCells(Graphics g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int cellX = col * CELL_SIZE;
                int cellY = row * CELL_SIZE;

                g.setColor(Color.BLACK);
                g.drawRect(cellX, cellY, CELL_SIZE, CELL_SIZE);

                if (grid[row][col] != null) {
                    cellRenderer.drawCell(g, col, row, grid[row][col]);
                }

                if ((row == ROWS - 1) && col == 5) {
                    g.setColor(Color.GREEN);
                    g.drawLine(cellX, 0, cellX, cellY + CELL_SIZE);
                }
            }
        }
    }

    private void paintCurrentShapeCells(Graphics g, Tetromino tetromino) {
        for (int row = 0; row < tetromino.shape.length; row++) {
            for (int col = 0; col < tetromino.shape[0].length; col++) {
                if (tetromino.shape[row][col] == 1) {
                    cellRenderer.drawCell(g, tetromino.x + col, tetromino.y + row, tetromino.color);
                }
            }
        }
    }

    private boolean collides(int dx, int dy, Tetromino tetromino) {
        for (int row = 0; row < tetromino.shape.length; row++) {
            for (int col = 0; col < tetromino.shape[0].length; col++) {
                if (tetromino.shape[row][col] == 1) {
                    int newX = tetromino.x + col + dx;
                    int newY = tetromino.y + row + dy;

                    if (newX < 0 || newX >= COLS || newY >= ROWS || (newY >= 0 && grid[newY][newX] != null))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean canMoveWithoutCollision(int dx, int dy, Tetromino tetromino) {
        for (int row = 0; row < tetromino.shape.length; row++) {
            for (int col = 0; col < tetromino.shape[0].length; col++) {
                if (tetromino.shape[row][col] == 1) {
                    int newX = tetromino.x + col + dx;
                    int newY = tetromino.y + row + dy;

                    if (newX < 0 || newX >= COLS || newY >= ROWS) return false;
                    if (newY >= 0 && grid[newY][newX] != null) return false;
                }
            }
        }
        return true;
    }

    private void move(int dx, Tetromino tetromino) {
        int width = tetromino.shape[0].length;

        if (tetromino == current_p1 && (tetromino.x + dx >= 0 && tetromino.x + dx + width <= COLS / 2)) {
            if (canMoveWithoutCollision(dx, 0, tetromino)) {
                tetromino.x += dx;
            }
        } else if (tetromino == current_p2 && (tetromino.x + dx >= COLS / 2 && tetromino.x + dx + width <= COLS)) {
            if (canMoveWithoutCollision(dx, 0, tetromino)) {
                tetromino.x += dx;
            }
        }
    }

    private void drop(Tetromino tetromino) {
        while (!collides(0, 1, tetromino)) {
            tetromino.y++;
        }
    }

    private void rotate(Tetromino tetromino) {
        // Store the current rotation state
        int originalRotationState = tetromino.getRotationState();
        int newRotationState = (originalRotationState + 1) % 4;

        // Create rotated tetromino
        int[][] rotatedShape = tetromino.rotate();
        Tetromino rotated = new Tetromino(rotatedShape, tetromino.color);
        rotated.x = tetromino.x;
        rotated.y = tetromino.y;

        // Define wall kick offsets according to SRS (Super Rotation System)
        // Format: [rotation_state][test_number][x_offset, y_offset]
        int[][][] kickData = getKickData(tetromino);

        // Try each wall kick offset
        for (int[] offset : kickData[originalRotationState]) {
            int testX = rotated.x + offset[0];
            int testY = rotated.y + offset[1];

            if (!collides(testX - rotated.x, testY - rotated.y, rotated)) {
                // Success - apply the rotation and the offset
                tetromino.shape = rotatedShape;
                tetromino.x = testX;
                tetromino.y = testY;
                tetromino.setRotationState(newRotationState);
                return;
            }
        }

        // If we get here, no wall kick succeeded
        // Rotation fails and the piece remains unchanged
    }

    private int[][][] getKickData(Tetromino tetromino) {
        int[][][] wallKickData = {
                // 0>>1, 1>>2, 2>>3, 3>>0 (clockwise)
                {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
                {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
                {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
                {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
        };

        // I-piece has different offsets
        int[][][] wallKickDataI = {
                // 0>>1, 1>>2, 2>>3, 3>>0 (clockwise)
                {{0, 0}, {-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
                {{0, 0}, {-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
                {{0, 0}, {2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
                {{0, 0}, {1, 0}, {-2, 0}, {1, -2}, {-2, 1}}
        };

        // Choose which kick data to use based on tetromino type
        int[][][] kickData = isIShape(tetromino) ? wallKickDataI : wallKickData;
        return kickData;
    }

    // Helper method to determine if this is an I-piece
    private boolean isIShape(Tetromino tetromino) {
        // I-piece is typically 4x1 or 1x4
        return (tetromino.shape.length == 4 && tetromino.shape[0].length == 1) || (tetromino.shape.length == 1 && tetromino.shape[0].length == 4);
    }
}