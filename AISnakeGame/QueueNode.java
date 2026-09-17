/**
 * QueueNode
 * ----------
 * One node of the custom Queue (a linked-node FIFO queue).
 * Each node holds a grid cell (Point) that BFS still needs to explore.
 *
 *   front -> (5,5) -> (5,4) -> (5,6) -> rear -> null
 */
public class QueueNode {

    public Point data;       // the grid cell stored in this node
    public QueueNode next;   // link to the node behind it in the queue

    public QueueNode(Point data) {
        this.data = data;
        this.next = null;
    }
}
