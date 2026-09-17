import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * InfoPanel
 * ----------
 * Right-hand sidebar showing LIVE information about the data structures
 * that power the game. All values are pushed in from GamePanel every
 * tick via setData(), so nothing here is hardcoded:
 *
 *   - Queue status   = cells currently in the BFS frontier (max reached)
 *   - Path length    = length of the path BFS just found
 *   - Linked List    = the real snake body (length / head / tail)
 */
public class InfoPanel extends JPanel {

    private static final Color TITLE_COLOR    = new Color(255, 200, 60);
    private static final Color KEY_COLOR      = new Color(150, 155, 170);
    private static final Color VALUE_COLOR    = Color.WHITE;
    private static final Color DIVIDER_COLOR  = new Color(70, 74, 86);

    // Values set by GamePanel
    private int snakeLength;
    private Point head = new Point(0, 0);
    private Point tail = new Point(0, 0);
    private int nodesExplored;
    private int maxQueueSize;
    private int pathLength = -1;
    private String lastMove = "-";
    private String gameState = "READY";
    private int score;
    private int speedLevel;

    public InfoPanel() {
        setPreferredSize(new Dimension(235, 0));
        setBackground(new Color(24, 26, 34));
    }

    /** Called by GamePanel every tick with the latest real values. */
    public void setData(int snakeLength, Point head, Point tail,
                        int nodesExplored, int maxQueueSize, int pathLength,
                        String lastMove, String gameState, int score, int speedLevel) {
        this.snakeLength = snakeLength;
        this.head = head;
        this.tail = tail;
        this.nodesExplored = nodesExplored;
        this.maxQueueSize = maxQueueSize;
        this.pathLength = pathLength;
        this.lastMove = lastMove;
        this.gameState = gameState;
        this.score = score;
        this.speedLevel = speedLevel;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 18;
        int y = 26;

        sectionTitle(g2, x, y, "AI INFORMATION");
        y += 24;
        divider(g2, x, y);
        y += 18;
        row(g2, x, y, "Algorithm", "BFS");
        y += 22;
        row(g2, x, y, "Queue status", maxQueueSize + " nodes");
        y += 22;
        row(g2, x, y, "Nodes explored", String.valueOf(nodesExplored));
        y += 22;
        row(g2, x, y, "Path length", pathLength < 0 ? "-" : pathLength + " cells");
        y += 22;
        row(g2, x, y, "Last move", lastMove);
        y += 24;

        sectionTitle(g2, x, y, "SNAKE");
        y += 24;
        divider(g2, x, y);
        y += 18;
        row(g2, x, y, "Data Structure", "Linked List");
        y += 22;
        row(g2, x, y, "Length", String.valueOf(snakeLength));
        y += 22;
        row(g2, x, y, "Head", head.toString());
        y += 22;
        row(g2, x, y, "Tail", tail.toString());
        y += 24;

        sectionTitle(g2, x, y, "GAME");
        y += 24;
        divider(g2, x, y);
        y += 18;
        row(g2, x, y, "State", gameState);
        y += 22;
        row(g2, x, y, "Score", String.valueOf(score));
        y += 22;
        row(g2, x, y, "Speed level", String.valueOf(speedLevel));

        g2.dispose();
    }

    private void sectionTitle(Graphics2D g2, int x, int y, String text) {
        g2.setColor(TITLE_COLOR);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.drawString(text, x, y);
    }

    private void divider(Graphics2D g2, int x, int y) {
        g2.setColor(DIVIDER_COLOR);
        g2.drawLine(x, y, getWidth() - x, y);
    }

    private void row(Graphics2D g2, int x, int y, String key, String value) {
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(KEY_COLOR);
        g2.drawString(key, x, y);
        g2.setColor(VALUE_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, getWidth() - x - fm.stringWidth(value), y);
    }
}
