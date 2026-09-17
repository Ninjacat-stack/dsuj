/**
 * SnakeNode
 * ----------
 * One node of the custom linked list that stores the snake's body.
 * Each node holds ONE segment of the snake as a Point on the grid.
 *
 *   Head node -> node -> node -> Tail node -> null
 */
public class SnakeNode {

    public Point position;   // the (x, y) grid cell of this snake segment
    public SnakeNode next;   // link to the segment behind it (towards the tail)

    public SnakeNode(Point position) {
        this.position = position;
        this.next = null;
    }
}
