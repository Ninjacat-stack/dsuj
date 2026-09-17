/**
 * Snake
 * ------
 * The snake's body is stored in a CUSTOM linked list (SnakeLinkedList).
 * Every movement is a real linked-list operation:
 *
 *   - normal move:   addFirst(new head) + removeLast()  -> same length
 *   - eats food:     addFirst(new head) only            -> length + 1
 */
public class Snake {

    private final SnakeLinkedList body;   // the custom linked list of body cells

    public Snake(Point start, int initialLength) {
        body = new SnakeLinkedList();
        // Build the initial snake: head at `start`, body extending to the LEFT.
        // addFirst adds to the front, so we add from the tail end backwards
        // to make the head end up at `start`.
        for (int i = initialLength - 1; i >= 0; i--) {
            body.addFirst(new Point(start.x - i, start.y));
        }
    }

    /**
     * Move the snake into the newHead cell.
     *
     * @param newHead the cell the head enters this tick
     * @param grow    true when the snake just ate food
     * @return true if the snake crashed into its own body
     */
    public boolean move(Point newHead, boolean grow) {
        // Collision check against the CURRENT body.
        // If the snake grows, the tail stays, so even the tail cell is blocked.
        // If it does not grow, the tail moves away, so the tail cell is safe.
        boolean crashed = grow ? body.contains(newHead)
                               : body.containsExceptTail(newHead);

        body.addFirst(newHead);   // LINKED LIST: push the new head on the front
        if (!grow) {
            body.removeLast();    // LINKED LIST: pop the tail off the back
        }
        return crashed;
    }

    public Point getHead() {
        return body.getHead();
    }

    public Point getTail() {
        return body.getTail();
    }

    public int length() {
        return body.size();
    }

    /** True if the given cell is occupied by any body segment. */
    public boolean contains(Point p) {
        return body.contains(p);
    }

    /** All body cells, head first - used for drawing and the fallback move. */
    public Point[] getBodyArray() {
        return body.toArray();
    }

    /**
     * Cells that block the BFS pathfinder: the whole body EXCEPT
     *   - the head  (it is the starting cell of the search) and
     *   - the tail  (it moves away this tick, so it is safe to enter).
     *
     * Demonstrates walking the custom linked list with .next pointers.
     */
    public Point[] getObstaclesForBFS() {
        if (body.size() <= 2) {
            return new Point[0];
        }
        Point[] result = new Point[body.size() - 2];
        SnakeNode current = body.getFirstNode().next;   // skip the head
        int i = 0;
        while (current != null && current.next != null) { // stop before the tail
            result[i++] = current.position;
            current = current.next;
        }
        return result;
    }
}
