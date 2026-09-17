/**
 * Point
 * ------
 * Represents a single (x, y) cell on the game grid.
 *
 * This class is shared by both data structures in the project:
 *   - SnakeNode (Linked List) stores the snake segments as Points.
 *   - QueueNode (Queue)      stores BFS frontier cells as Points.
 */
public class Point {

    public final int x;   // column index (0 = left)
    public final int y;   // row index (0 = top)

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() {
        return x * 31 + y;
    }

    /** Used by the "Data Structures" info panel, e.g. "Head: (10, 5)". */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
