import java.util.Random;

/**
 * Food
 * ------
 * Holds the current food cell. When the snake eats the food, a new food
 * is spawned on a random free cell of the board.
 */
public class Food {

    private Point position;
    private final Random random = new Random();

    public Point getPosition() {
        return position;
    }

    /**
     * Spawn food on a random cell that is NOT occupied by the snake.
     *
     * @return false if the board is completely full (the player won)
     */
    public boolean spawnRandom(int cols, int rows, Snake snake) {
        if (snake.length() >= cols * rows) {
            return false;   // no free cell left
        }
        while (true) {
            Point candidate = new Point(random.nextInt(cols), random.nextInt(rows));
            if (!snake.contains(candidate)) {   // uses the linked list contains()
                position = candidate;
                return true;
            }
        }
    }
}
