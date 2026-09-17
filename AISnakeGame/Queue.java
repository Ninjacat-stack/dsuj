/**
 * Queue
 * ------
 * A CUSTOM FIFO (first-in, first-out) queue built from linked nodes.
 * It is written from scratch (no java.util.Queue) because this project
 * must demonstrate the Queue data structure.
 *
 * WHERE IT IS USED:
 * Only by BFSPathFinder. During Breadth-First Search every grid cell
 * that still needs to be explored is enqueued, and cells are explored
 * in the order they were discovered (FIFO) - which is exactly what
 * makes BFS find the SHORTEST path.
 *
 *   enqueue -> [ ... ][ ... ][ ... ] -> dequeue
 *               rear              front
 */
public class Queue {

    private QueueNode front;   // where we dequeue from (the oldest cell)
    private QueueNode rear;    // where we enqueue to (the newest cell)
    private int size;          // current number of cells in the queue

    public Queue() {
        front = null;
        rear = null;
        size = 0;
    }

    /** Add a cell to the BACK of the queue. O(1) */
    public void enqueue(Point data) {
        QueueNode newNode = new QueueNode(data);
        if (rear == null) {          // queue is empty
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;     // link the old rear to the new node
            rear = newNode;          // the new node becomes the rear
        }
        size++;
    }

    /** Remove and return the cell at the FRONT of the queue. O(1) */
    public Point dequeue() {
        if (front == null) {         // queue is empty
            return null;
        }
        Point data = front.data;
        front = front.next;          // move the front pointer one step
        if (front == null) {         // the queue became empty
            rear = null;
        }
        size--;
        return data;
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }
}
