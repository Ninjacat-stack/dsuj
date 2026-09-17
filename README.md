# AI Snake Game — Data Structures Project

A fully playable Snake game written in **Java (Swing)** where an **AI** plays
instead of a human. The snake finds the food on its own using
**Breadth-First Search (BFS)** pathfinding.

This project was built to demonstrate two classic data structures, both
implemented **from scratch** (no `java.util.LinkedList`, no `java.util.Queue`):

| Data Structure | Where it is used |
|----------------|------------------|
| **Linked List** (`SnakeLinkedList`, `SnakeNode`) | Stores the snake's entire body. Every movement really adds a head node / removes the tail node. |
| **Queue** (`Queue`, `QueueNode`) | The BFS frontier. Every grid cell the AI still needs to explore is enqueued. |

The gameplay logic truly depends on these structures — nothing is faked
just to satisfy the requirement.

---

## Requirements

- JDK 8 or newer (built and tested on JDK 25)
- Nothing else — no external libraries, no build tools

## How to run

Open a terminal in this folder and type:

```bash
javac -encoding UTF-8 *.java     # compile
java Main                        # run
```

Optional: `java Main --autostart` starts the game immediately (useful for
demos and testing).

## How to use the UI

- **Start** — begins the game; the AI takes over and plays by itself.
- **Pause / Resume** — freezes / continues the AI game loop.
- **Restart** — resets score and snake, starts a fresh game.
- The on-board overlays (START GAME / RESUME / RESTART) are clickable too.
- **Header chips** show Score, Snake length, Speed level and the current
  AI status (e.g. `AI: Searching for Food`).
- The **right sidebar** shows live data-structure state: BFS queue status,
  path length, Linked List length / head / tail.
- The window is resizable; the board scales and stays centered.
- The faint yellow cells show the path the AI is currently following.

## Game rules

- 20 x 20 grid. The snake starts with length 4.
- The snake automatically moves toward the food each tick.
- Eating food: +10 score, length +1, speed increases, new food spawns.
- Game over: the snake crashes into its own body or has no safe move left.
- Win: the snake fills the entire board.

---

## How the game loop works

```
Swing Timer (every 220ms at start, faster as score rises)
    |
    v
BFSPathFinder.findPath(head, food)      <- BFS using the custom Queue
    |
    v
next cell = first step of the path
    |
    v
Snake.move(next, grow)                  <- custom Linked List operations
    |
    v
board repaints -> the snake visibly moves
```

The AI recalculates the full BFS path **every tick**, so it always reacts
to the latest board state.

## Data structure 1: Linked List (the snake body)

`SnakeLinkedList` is a singly linked list written from scratch.

```
head -> (10,5) -> (9,5) -> (8,5) -> (7,5) <- tail -> null
```

| Operation | What it does in the game | Complexity |
|-----------|--------------------------|------------|
| `addFirst(Point)` | new head segment when the snake moves | O(1) |
| `removeLast()` | removes the tail when the snake moves without eating | O(n) |
| `contains(Point)` | self-collision detection | O(n) |
| `containsExceptTail(Point)` | collision check ignoring the tail (the tail moves away) | O(n) |
| `size()`, `getHead()`, `getTail()` | length / head / tail shown in the UI | O(1) |

**Eating food = `addFirst` without `removeLast`**, so the list grows by one
node — the length increase you see is the linked list really growing.

## Data structure 2: Queue (the BFS frontier)

`Queue` is a FIFO queue built from linked nodes (`enqueue` / `dequeue` /
`isEmpty`), used only inside `BFSPathFinder`.

```
enqueue -> [ ... ][ ... ][ ... ] -> dequeue
            rear              front
```

## How BFS finds the food

1. Put the snake head into the queue.
2. Dequeue a cell. If it is the food, stop — path found.
3. Try all 4 neighbours (UP, DOWN, LEFT, RIGHT). Skip cells that are:
   - outside the board (walls),
   - part of the snake body (obstacles; the tail is allowed because it
     moves away this tick),
   - already visited.
4. Mark valid neighbours as visited, remember their **parent** cell, and
   enqueue them.
5. Repeat from step 2 until the food is found or the queue is empty.
6. Rebuild the path by walking parent pointers from the food back to the
   head, then reversing.

Because the queue is FIFO, BFS explores level by level, so the first time
the food is reached is guaranteed to be via the **shortest path**.
Overall BFS complexity: **O(rows x cols)** per tick.

**Fallback:** if BFS finds no path (food is walled off), the AI tries
UP, DOWN, LEFT, RIGHT and picks the first safe cell instead of crashing.

## Project structure

```
AISnakeGame/
|
|-- Main.java              Entry point (javac *.java && java Main)
|-- GameFrame.java         Main window: header chips, buttons, layout
|-- GamePanel.java         Board rendering + game loop + overlays
|-- InfoPanel.java         Live data-structure sidebar
|
|-- Snake.java             Snake behaviour (uses the linked list)
|-- SnakeNode.java         Linked list node (one body segment)
|-- SnakeLinkedList.java   CUSTOM linked list for the snake body
|
|-- Queue.java             CUSTOM FIFO queue for BFS
|-- QueueNode.java         Queue node (one grid cell)
|
|-- BFSPathFinder.java     BFS shortest-path algorithm (uses the Queue)
|-- Food.java              Food position + random spawning
|-- Point.java             One (x, y) grid cell
```

## Time complexity summary

- Snake moves (`addFirst` + `removeLast`): **O(n)** — n = snake length
- Collision check: **O(n)**
- BFS per tick: **O(rows x cols)**
- Food spawning: O(1) expected (random cell + linked-list contains check)
