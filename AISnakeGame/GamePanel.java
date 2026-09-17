import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * GamePanel
 * ----------
 * The game board. It draws the grid, the snake, the food, the planned
 * BFS path and state overlays (start / paused / game over / win).
 *
 * Game loop (Swing Timer):
 *     Timer -> AI calculates next movement (BFS + custom Queue)
 *           -> Snake Linked List updates
 *           -> board repaints -> the player sees the snake move.
 *
 * All movement logic lives in Snake / BFSPathFinder; this class only
 * triggers it and renders the result.
 */
public class GamePanel extends JPanel implements ActionListener {

    // Grid dimensions (in cells)
    public static final int COLS = 20;
    public static final int ROWS = 20;

    private static final int BASE_DELAY = 220;   // starting speed (ms per move)
    private static final int MIN_DELAY = 90;     // fastest allowed speed
    private static final int MIN_TILE = 10;      // smallest drawn cell size

    /** The visual state of the game. */
    public enum GameState { READY, RUNNING, PAUSED, GAME_OVER, WIN }

    // ---------- palette ----------
    private static final Color BOARD_BG       = new Color(16, 18, 26);
    private static final Color BOARD_BG_ALT   = new Color(20, 22, 32);
    private static final Color GRID_COLOR     = new Color(255, 255, 255, 10);
    private static final Color BOARD_BORDER   = new Color(255, 200, 60, 40);
    private static final Color SNAKE_HEAD     = new Color(105, 230, 130);
    private static final Color SNAKE_BODY     = new Color(44, 166, 91);
    private static final Color SNAKE_TAIL     = new Color(20, 96, 55);
    private static final Color EYE_COLOR      = new Color(20, 22, 30);
    private static final Color FOOD_COLOR     = new Color(255, 82, 82);
    private static final Color FOOD_GLOW      = new Color(255, 82, 82, 40);
    private static final Color FOOD_HIGHLIGHT = new Color(255, 200, 190, 130);
    private static final Color PATH_COLOR     = new Color(255, 205, 60, 35);
    private static final Color PANEL_BG       = new Color(30, 32, 42, 240);
    private static final Color PANEL_BORDER   = new Color(255, 200, 60, 60);
    private static final Color OVERLAY_BUTTON = new Color(56, 130, 246);
    private static final Color DIM_COLOR      = new Color(0, 0, 0, 150);

    private GameState state = GameState.READY;
    private Snake snake;
    private Food food;
    private final BFSPathFinder pathFinder;
    private final Timer timer;       // the game loop (moves the snake)
    private final Timer uiTimer;     // fast repaint for small animations
    private final GameFrame frame;

    private int score;
    private int steps;
    private int speedLevel = 1;

    // Live AI statistics (pushed to the sidebar every tick)
    private List<Point> lastPath;
    private int lastExplored;
    private int lastMaxQueue;
    private String lastMoveNote = "-";
    private Point lastDirection = new Point(1, 0);   // where the head is facing

    // "+10" popup when food is eaten
    private Point eatFxPos;
    private long eatFxTime;

    // The clickable button painted inside the overlay (start/resume/restart)
    private Rectangle overlayButtonRect;

    public GamePanel(GameFrame frame) {
        this.frame = frame;
        setBackground(BOARD_BG);

        pathFinder = new BFSPathFinder(COLS, ROWS);
        timer = new Timer(BASE_DELAY, this);

        // Lightweight timer so the food pulse / score popup animate even
        // on the start screen or while paused. The game itself only moves
        // when `timer` ticks and the state is RUNNING.
        uiTimer = new Timer(40, e -> repaint());
        uiTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleOverlayClick(e.getPoint());
            }
        });

        initGame();
    }

    // ===================== PUBLIC CONTROLS =====================

    /** Reset everything to a fresh game (not running yet). */
    public void initGame() {
        timer.stop();
        Point start = new Point(COLS / 2, ROWS / 2);
        snake = new Snake(start, 4);                 // body = custom Linked List
        food = new Food();
        food.spawnRandom(COLS, ROWS, snake);
        score = 0;
        steps = 0;
        speedLevel = 1;
        lastDirection = new Point(1, 0);
        lastPath = null;
        lastExplored = 0;
        lastMaxQueue = 0;
        lastMoveNote = "-";
        eatFxPos = null;
        state = GameState.READY;
        pushInfo();
        repaint();
    }

    /** Start (or restart) the game and the AI. */
    public void startGame() {
        initGame();
        state = GameState.RUNNING;
        timer.setDelay(BASE_DELAY);
        timer.start();
        pushInfo();
        repaint();
    }

    public void restartGame() {
        startGame();
    }

    /** Toggle between RUNNING and PAUSED. */
    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            timer.stop();
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            timer.start();
        }
        pushInfo();
        repaint();
    }

    public GameState getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getSnakeLength() {
        return snake.length();
    }

    // ===================== GAME LOOP =====================

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state != GameState.RUNNING) {
            return;
        }
        aiStep();
        repaint();
    }

    /**
     * One tick:
     *   1. run BFS from the snake head to the food (BFS uses the custom Queue),
     *   2. take the first step of the path (or use the safe fallback),
     *   3. move the snake (the body is the custom Linked List),
     *   4. handle eating, speed increase and collisions.
     */
    private void aiStep() {
        steps++;
        Point head = snake.getHead();
        Point target = food.getPosition();

        // 1) AI: BFS pathfinding with the custom Queue (see BFSPathFinder)
        List<Point> path = pathFinder.findPath(head, target, snake.getObstaclesForBFS());
        lastPath = path;
        lastExplored = pathFinder.getNodesExplored();
        lastMaxQueue = pathFinder.getMaxQueueSize();

        // 2) Decide the next cell
        Point next;
        if (path != null && path.size() >= 2) {
            next = path.get(1);                        // path[0] is the head itself
            lastMoveNote = "Path found (" + path.size() + " cells)";
        } else {
            next = fallbackMove(head);                 // BFS failed -> safe fallback
            lastMoveNote = "Fallback (no path)";
        }

        if (next == null) {                            // no safe move at all
            gameOver();
            pushInfo();
            return;
        }

        lastDirection = new Point(next.x - head.x, next.y - head.y);

        // 3) Move the snake (Linked List operations inside Snake.move)
        boolean grow = next.equals(target);
        boolean selfCollision = snake.move(next, grow);

        if (selfCollision) {
            gameOver();
            pushInfo();
            return;
        }

        // 4) Food eaten -> score, length, speed
        if (grow) {
            score += 10;
            eatFxPos = next;
            eatFxTime = System.currentTimeMillis();
            int delay = Math.max(MIN_DELAY, BASE_DELAY - score * 2);   // speed up
            timer.setDelay(delay);
            speedLevel = (BASE_DELAY - delay) / 15 + 1;
            if (!food.spawnRandom(COLS, ROWS, snake)) {                // board full -> win
                state = GameState.WIN;
                timer.stop();
            }
        }
        pushInfo();
    }

    /**
     * Fallback when BFS finds no path to the food:
     * try UP, DOWN, LEFT, RIGHT in order and pick the first safe cell.
     */
    private Point fallbackMove(Point head) {
        int[] dx = { 0, 0, -1, 1 };
        int[] dy = { -1, 1, 0, 0 };
        Point tail = snake.getTail();
        for (int i = 0; i < 4; i++) {
            Point p = new Point(head.x + dx[i], head.y + dy[i]);
            if (!inBounds(p)) continue;                        // wall
            if (snake.contains(p) && !p.equals(tail)) continue; // body (tail frees up)
            return p;
        }
        return null;
    }

    void gameOver() {
        state = GameState.GAME_OVER;
        timer.stop();
    }

    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < COLS && p.y >= 0 && p.y < ROWS;
    }

    private String aiStatusText() {
        switch (state) {
            case READY:     return "AI: Idle";
            case PAUSED:    return "AI: Paused";
            case GAME_OVER: return "AI: Stopped";
            case WIN:       return "AI: Victory!";
            default:        return lastMoveNote.startsWith("Path")
                                    ? "AI: Searching for Food"
                                    : "AI: Safe Fallback";
        }
    }

    /** Push score, DS info and button states to the frame / sidebar. */
    private void pushInfo() {
        frame.updateStats(score, snake.length(), speedLevel, aiStatusText());
        frame.updateInfo(snake.length(), snake.getHead(), snake.getTail(),
                lastExplored, lastMaxQueue,
                lastPath == null ? -1 : lastPath.size(),
                lastMoveNote, state.name(), score, speedLevel);
        frame.updateControls(state == GameState.RUNNING, state == GameState.PAUSED);
    }

    // ===================== DRAWING =====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int tile = computeTile();
        int boardPx = tile * COLS;
        int ox = (getWidth() - boardPx) / 2;   // keep the board centered
        int oy = (getHeight() - boardPx) / 2;

        drawBoard(g2, tile, ox, oy);
        drawPath(g2, tile, ox, oy);
        drawFood(g2, tile, ox, oy);
        drawSnake(g2, tile, ox, oy);
        drawEatEffect(g2, tile, ox, oy);
        drawOverlay(g2, tile, ox, oy);

        g2.dispose();
    }

    private int computeTile() {
        return Math.max(MIN_TILE, Math.min(getWidth() / COLS, getHeight() / ROWS));
    }

    private void drawBoard(Graphics2D g2, int tile, int ox, int oy) {
        g2.setColor(BOARD_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // subtle checkerboard
        g2.setColor(BOARD_BG_ALT);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if ((x + y) % 2 == 0) {
                    g2.fillRect(ox + x * tile, oy + y * tile, tile, tile);
                }
            }
        }

        // grid lines
        g2.setColor(GRID_COLOR);
        for (int x = 0; x <= COLS; x++) {
            g2.drawLine(ox + x * tile, oy, ox + x * tile, oy + ROWS * tile);
        }
        for (int y = 0; y <= ROWS; y++) {
            g2.drawLine(ox, oy + y * tile, ox + COLS * tile, oy + y * tile);
        }

        // board border
        g2.setColor(BOARD_BORDER);
        g2.drawRect(ox - 1, oy - 1, COLS * tile + 1, ROWS * tile + 1);
    }

    /** Faint preview of the path the AI currently plans to follow. */
    private void drawPath(Graphics2D g2, int tile, int ox, int oy) {
        if (state != GameState.RUNNING || lastPath == null) {
            return;
        }
        g2.setColor(PATH_COLOR);
        for (int i = 1; i < lastPath.size(); i++) {   // skip the head cell
            Point p = lastPath.get(i);
            g2.fillRoundRect(ox + p.x * tile + 3, oy + p.y * tile + 3,
                             tile - 6, tile - 6, tile / 2, tile / 2);
        }
    }

    private void drawFood(Graphics2D g2, int tile, int ox, int oy) {
        Point f = food.getPosition();
        int cx = ox + f.x * tile + tile / 2;
        int cy = oy + f.y * tile + tile / 2;

        // pulsing glow + body
        double pulse = (Math.sin(System.currentTimeMillis() / 180.0) + 1) / 2;
        int r = (int) (tile * 0.36 + pulse * tile * 0.05);

        g2.setColor(FOOD_GLOW);
        g2.fillOval(cx - tile / 2, cy - tile / 2, tile, tile);
        g2.setColor(FOOD_COLOR);
        g2.fillOval(cx - r, cy - r, 2 * r, 2 * r);

        // small highlight
        g2.setColor(FOOD_HIGHLIGHT);
        g2.fillOval(cx - r / 2, cy - r / 2, r / 2, r / 2);
    }

    private void drawSnake(Graphics2D g2, int tile, int ox, int oy) {
        Point[] body = snake.getBodyArray();   // head = body[0], tail = last

        // draw tail first so the head ends up on top
        for (int i = body.length - 1; i >= 0; i--) {
            Point p = body[i];
            Color color;
            if (i == 0) {
                color = SNAKE_HEAD;
            } else {
                // gradient from body green towards a darker tail green
                float t = body.length == 1 ? 0f : (float) i / (body.length - 1);
                color = lerp(SNAKE_BODY, SNAKE_TAIL, t);
            }
            int pad = i == 0 ? 1 : 2;
            g2.setColor(color);
            g2.fillRoundRect(ox + p.x * tile + pad, oy + p.y * tile + pad,
                             tile - 2 * pad, tile - 2 * pad, tile / 2, tile / 2);
        }

        drawEyes(g2, tile, ox, oy, body[0]);
    }

    /** Two small eyes on the head, looking in the direction of movement. */
    private void drawEyes(Graphics2D g2, int tile, int ox, int oy, Point head) {
        int cx = ox + head.x * tile + tile / 2;
        int cy = oy + head.y * tile + tile / 2;
        int dx = lastDirection.x;
        int dy = lastDirection.y;

        int fwd = tile / 4;     // forward offset
        int side = tile / 5;    // sideways offset
        int r = Math.max(2, tile / 10);

        int[] ex = { cx + dx * fwd - dy * side, cx + dx * fwd + dy * side };
        int[] ey = { cy + dy * fwd - dx * side, cy + dy * fwd + dx * side };

        g2.setColor(Color.WHITE);
        for (int i = 0; i < 2; i++) {
            g2.fillOval(ex[i] - r, ey[i] - r, 2 * r, 2 * r);
        }
        int pr = Math.max(1, r / 2);
        g2.setColor(EYE_COLOR);
        for (int i = 0; i < 2; i++) {
            g2.fillOval(ex[i] + dx * pr - pr, ey[i] + dy * pr - pr, 2 * pr, 2 * pr);
        }
    }

    /** Floating "+10" that fades out above the cell where food was eaten. */
    private void drawEatEffect(Graphics2D g2, int tile, int ox, int oy) {
        if (eatFxPos == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - eatFxTime;
        if (elapsed > 800) {
            eatFxPos = null;
            return;
        }
        float t = elapsed / 800f;
        int alpha = (int) (255 * (1 - t));
        int cx = ox + eatFxPos.x * tile + tile / 2;
        int cy = oy + eatFxPos.y * tile - (int) (t * tile * 0.9);

        g2.setColor(new Color(255, 255, 255, alpha));
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, tile / 2)));
        FontMetrics fm = g2.getFontMetrics();
        String text = "+10";
        g2.drawString(text, cx - fm.stringWidth(text) / 2, cy);
    }

    // ===================== OVERLAYS (start / paused / game over) =====================

    private void drawOverlay(Graphics2D g2, int tile, int ox, int oy) {
        overlayButtonRect = null;

        String title = null;
        String subtitle = null;
        String button = null;
        switch (state) {
            case READY:
                title = "AI SNAKE";
                subtitle = "Watch the AI find its food!";
                button = "START GAME";
                break;
            case PAUSED:
                title = "PAUSED";
                subtitle = "Take a breather...";
                button = "RESUME";
                break;
            case GAME_OVER:
                title = "GAME OVER";
                subtitle = "Score: " + score + "     Length: " + snake.length();
                button = "RESTART";
                break;
            case WIN:
                title = "YOU WIN!";
                subtitle = "Score: " + score + "     Length: " + snake.length();
                button = "RESTART";
                break;
            default:
                return;   // RUNNING -> no overlay
        }

        // dim the board
        g2.setColor(DIM_COLOR);
        g2.fillRect(ox, oy, tile * COLS, tile * ROWS);

        // centered rounded panel
        int panelW = Math.min(tile * COLS - 40, 330);
        int panelH = 200;
        int px = ox + (tile * COLS - panelW) / 2;
        int py = oy + (tile * ROWS - panelH) / 2;
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(px, py, panelW, panelH, 24, 24);
        g2.setColor(PANEL_BORDER);
        g2.drawRoundRect(px, py, panelW, panelH, 24, 24);

        // title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(20, Math.min(34, tile))));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, px + (panelW - fm.stringWidth(title)) / 2, py + 60);

        // subtitle
        g2.setColor(new Color(180, 185, 200));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(12, tile / 2 + 2)));
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, px + (panelW - fm.stringWidth(subtitle)) / 2, py + 96);

        // button
        int bw = 190;
        int bh = 46;
        int bx = px + (panelW - bw) / 2;
        int by = py + panelH - bh - 22;
        g2.setColor(OVERLAY_BUTTON);
        g2.fillRoundRect(bx, by, bw, bh, 23, 23);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        g2.drawString(button, bx + (bw - fm.stringWidth(button)) / 2,
                      by + (bh - fm.getHeight()) / 2 + fm.getAscent());
        overlayButtonRect = new Rectangle(bx, by, bw, bh);
    }

    /** Clicks on the overlay button behave like the bottom-bar buttons.
     *  Note: java.awt.Point is used fully qualified because the game has
     *  its own Point class (grid cells) in the default package. */
    private void handleOverlayClick(java.awt.Point click) {
        if (overlayButtonRect == null || !overlayButtonRect.contains(click)) {
            return;
        }
        switch (state) {
            case READY:     startGame();   break;
            case PAUSED:    togglePause(); break;
            case GAME_OVER:
            case WIN:       restartGame(); break;
        }
    }

    private static Color lerp(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }
}
