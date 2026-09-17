import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BFSPathFinder
 * --------------
 * Finds the shortest path from the snake's head to the food using
 * Breadth-First Search. BFS uses the CUSTOM Queue class - every grid
 * cell that still needs to be explored is stored in that queue.
 *
 * HOW BFS WORKS HERE:
 *   1. Put the head cell into the queue.
 *   2. Remove a cell from the FRONT of the queue and look at it.
 *   3. For each neighbour (UP, DOWN, LEFT, RIGHT) that is
 *        - inside the board (not a wall),
 *        - not part of the snake's body, and
 *        - not visited yet:
 *      mark it visited, remember its parent cell, and enqueue it.
 *   4. Repeat until the food is found or the queue becomes empty.
 *   5. If the food was found, walk backwards from the food to the head
 *      using the parent pointers and reverse the result -> the path.
 *
 * Because BFS explores level by level (FIFO queue), the first time the
 * food is reached is guaranteed to be via the SHORTEST path.
 */
public class BFSPathFinder {

    private final int cols;   // board width in cells
    private final int rows;   // board height in cells

    private int nodesExplored;   // how many cells BFS looked at (shown in the GUI)
    private int maxQueueSize;    // largest size the custom queue reached

    public BFSPathFinder(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    /**
     * Run BFS from the snake head to the food.
     *
     * @param start     the snake's head
     * @param target    the food cell
     * @param obstacles snake body cells that cannot be entered
     * @return the full path (start ... target), or null if no path exists
     */
    public List<Point> findPath(Point start, Point target, Point[] obstacles) {
        nodesExplored = 0;
        maxQueueSize = 0;

        boolean[][] visited = new boolean[rows][cols];
        Point[][] parent = new Point[rows][cols];   // remembers where we came from

        // ---------------- CUSTOM QUEUE (the BFS frontier) ----------------
        Queue queue = new Queue();
        queue.enqueue(start);                        // step 1: put head in queue
        visited[start.y][start.x] = true;

        // Exploration order: UP, DOWN, LEFT, RIGHT
        int[] dx = { 0, 0, -1, 1 };
        int[] dy = { -1, 1, 0, 0 };

        while (!queue.isEmpty()) {                   // step 2 & 4
            Point current = queue.dequeue();         // FIFO: explore oldest first
            nodesExplored++;

            if (current.equals(target)) {            // food reached!
                return reconstructPath(parent, start, target);
            }

            for (int i = 0; i < 4; i++) {            // step 3: try 4 directions
                Point next = new Point(current.x + dx[i], current.y + dy[i]);

                if (!inBounds(next)) continue;              // wall
                if (visited[next.y][next.x]) continue;      // already explored
                if (isObstacle(next, obstacles)) continue;  // snake body

                visited[next.y][next.x] = true;
                parent[next.y][next.x] = current;   // remember where we came from
                queue.enqueue(next);                // explore this cell later
                maxQueueSize = Math.max(maxQueueSize, queue.size());
            }
        }
        return null;   // queue empty and food never reached -> no path
    }

    /** Walk from the target back to the start using the parent pointers. */
    private List<Point> reconstructPath(Point[][] parent, Point start, Point target) {
        List<Point> path = new ArrayList<>();
        Point current = target;
        while (current != null) {
            path.add(current);
            if (current.equals(start)) {
                break;
            }
            current = parent[current.y][current.x];
        }
        Collections.reverse(path);   // now the path goes head -> food
        return path;
    }

    private boolean inBounds(Point p) {
        return p.x >= 0 && p.x < cols && p.y >= 0 && p.y < rows;
    }

    private boolean isObstacle(Point p, Point[] obstacles) {
        for (Point o : obstacles) {
            if (o.equals(p)) {
                return true;
            }
        }
        return false;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}
