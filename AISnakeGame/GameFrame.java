import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * GameFrame
 * ----------
 * The main game window:
 *
 *   NORTH  - header: title + live status chips (score, length, speed, AI)
 *   CENTER - GamePanel (the board)
 *   EAST   - InfoPanel (live data-structure sidebar)
 *   SOUTH  - control bar: [ Start ] [ Pause ] [ Restart ]
 */
public class GameFrame extends JFrame {

    private static final Color BG          = new Color(24, 26, 34);
    private static final Color ACCENT      = new Color(255, 200, 60);
    private static final Color SUBTLE      = new Color(150, 155, 170);
    private static final Color CHIP_BG     = new Color(38, 42, 54);

    private final GamePanel gamePanel;
    private final InfoPanel infoPanel;
    private JLabel scoreLabel;
    private JLabel lengthLabel;
    private JLabel speedLabel;
    private JLabel aiLabel;
    private RoundedButton startButton;
    private RoundedButton pauseButton;
    private RoundedButton restartButton;

    public GameFrame() {
        setTitle("AI Snake Game - Data Structures Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 600));

        // Build order matters: GamePanel's constructor immediately pushes
        // stats/info/control updates, so the labels, sidebar and buttons
        // must already exist.
        JPanel header = buildHeader();      // 1: creates the status chips
        infoPanel = new InfoPanel();        // 2: the DS sidebar
        JPanel controls = buildControls();  // 3: creates the buttons
        gamePanel = new GamePanel(this);    // 4: safe to push updates now

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);   // center the window on the screen
    }

    // ===================== HEADER =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new javax.swing.BoxLayout(titleBox, javax.swing.BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("AI SNAKE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel("Linked List  +  Queue  +  BFS Pathfinding");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(SUBTLE);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        chips.setOpaque(false);

        scoreLabel  = makeChip("Score: 0",     ACCENT);
        lengthLabel = makeChip("Length: 4",    new Color(105, 230, 130));
        speedLabel  = makeChip("Speed: 1",     new Color(120, 170, 255));
        aiLabel     = makeChip("AI: Idle",     new Color(130, 220, 255));

        chips.add(scoreLabel);
        chips.add(lengthLabel);
        chips.add(speedLabel);
        chips.add(aiLabel);
        header.add(chips, BorderLayout.EAST);

        return header;
    }

    private static JLabel makeChip(String text, Color textColor) {
        JLabel chip = new JLabel(text);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chip.setForeground(textColor);
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, CHIP_BG),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        return chip;
    }

    // ===================== CONTROLS =====================

    private JPanel buildControls() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        bar.setBackground(BG);

        startButton   = new RoundedButton("Start",  new Color(46, 160, 90));    // green
        pauseButton   = new RoundedButton("Pause",  new Color(56, 130, 246));   // blue
        restartButton = new RoundedButton("Restart", new Color(120, 90, 220));  // purple

        startButton.addActionListener(e -> gamePanel.startGame());
        pauseButton.addActionListener(e -> gamePanel.togglePause());
        restartButton.addActionListener(e -> gamePanel.restartGame());

        bar.add(startButton);
        bar.add(pauseButton);
        bar.add(restartButton);
        return bar;
    }

    // ===================== UPDATES FROM THE GAME LOOP =====================

    /** Refresh the header status chips. Called by GamePanel every tick. */
    public void updateStats(int score, int length, int speedLevel, String aiStatus) {
        scoreLabel.setText("Score: " + score);
        lengthLabel.setText("Length: " + length);
        speedLabel.setText("Speed: " + speedLevel);
        aiLabel.setText(aiStatus);
    }

    /** Forward live DS values to the sidebar. */
    public void updateInfo(int snakeLength, Point head, Point tail,
                           int nodesExplored, int maxQueueSize, int pathLength,
                           String lastMove, String gameState, int score, int speedLevel) {
        infoPanel.setData(snakeLength, head, tail, nodesExplored, maxQueueSize,
                          pathLength, lastMove, gameState, score, speedLevel);
    }

    /** Enable/disable the buttons to match the current game state. */
    public void updateControls(boolean running, boolean paused) {
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running || paused);
        pauseButton.setText(paused ? "Resume" : "Pause");
    }

    // ===================== ACCESSORS =====================

    /** Used by Main (--autostart flag) and by the UI self-test. */
    public void startGame() {
        gamePanel.startGame();
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    // ===================== SMALL UI HELPERS =====================

    /** A JButton with a rounded background and hover feedback. */
    static class RoundedButton extends JButton {

        private final Color base;
        private final Color hover;
        private boolean hovering;

        RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            this.hover = base.brighter();
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(9, 26, 9, 26));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hovering = true;  repaint(); }
                @Override
                public void mouseExited(MouseEvent e)  { hovering = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill;
            Color text;
            if (!isEnabled()) {
                fill = new Color(56, 60, 72);
                text = new Color(130, 135, 150);
            } else {
                fill = hovering ? hover : base;
                text = Color.WHITE;
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(text);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(),
                          (getWidth() - fm.stringWidth(getText())) / 2,
                          (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** A border that paints a rounded rectangle background (for the chips). */
    static class RoundedBorder implements Border {

        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 14, 5, 14);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
    }
}
