/**
 * SnakeLinkedList
 * ----------------
 * A CUSTOM singly linked list that stores the snake's entire body.
 * It is written from scratch (no java.util.LinkedList) because this
 * project must demonstrate the Linked List data structure.
 *
 *   head -> (10,5) -> (9,5) -> (8,5) -> tail(7,5) -> null
 *
 * How the game uses this list:
 *   1. addFirst()   - every time the snake moves, the NEW HEAD cell is
 *                     added to the front of the list.
 *   2. removeLast() - when the snake moves WITHOUT eating, the tail is
 *                     removed from the end of the list (length unchanged).
 *   3. contains()   - collision detection: checks whether any body
 *                     segment already occupies a given cell.
 */
public class SnakeLinkedList {

    private SnakeNode head;   // first node = the snake's head
    private SnakeNode tail;   // last node  = the snake's tail
    private int size;         // number of nodes = snake length

    public SnakeLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    /** Add a new segment to the FRONT of the snake (its new head). O(1) */
    public void addFirst(Point position) {
        SnakeNode newNode = new SnakeNode(position);
        if (head == null) {            // the list is empty
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;       // old head becomes the second node
            head = newNode;
        }
        size++;
    }

    /**
     * Remove the LAST segment (the tail) and return it. O(n)
     * We must walk the list to find the node just before the tail
     * so we can unlink it.
     */
    public Point removeLast() {
        if (head == null) {            // nothing to remove
            return null;
        }
        Point removed = tail.position;
        if (head == tail) {            // only one node left
            head = null;
            tail = null;
        } else {
            SnakeNode current = head;
            while (current.next != tail) {   // walk to the node before the tail
                current = current.next;
            }
            current.next = null;
            tail = current;
        }
        size--;
        return removed;
    }

    /** Return true if any segment of the snake is on the given cell. O(n) */
    public boolean contains(Point position) {
        SnakeNode current = head;
        while (current != null) {
            if (current.position.equals(position)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Return true if any segment EXCEPT the tail is on the given cell. O(n)
     * Used when the snake moves WITHOUT eating: the tail moves away this
     * tick, so stepping into the tail cell is actually safe.
     */
    public boolean containsExceptTail(Point position) {
        SnakeNode current = head;
        while (current != null) {
            if (current != tail && current.position.equals(position)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public Point getHead() {
        return head == null ? null : head.position;
    }

    public Point getTail() {
        return tail == null ? null : tail.position;
    }

    public int size() {
        return size;
    }

    /** The first node itself (lets the Snake walk the list with .next). */
    public SnakeNode getFirstNode() {
        return head;
    }

    /**
     * Copy all body cells into a plain array, head first.
     * (Arrays are fine to use; only the snake storage itself is the
     * custom linked list. The array is just a convenience for drawing.)
     */
    public Point[] toArray() {
        Point[] array = new Point[size];
        SnakeNode current = head;
        int i = 0;
        while (current != null) {
            array[i++] = current.position;
            current = current.next;
        }
        return array;
    }
}
