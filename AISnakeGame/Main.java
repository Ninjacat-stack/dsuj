import javax.swing.SwingUtilities;

/**
 * Main
 * ------
 * Entry point of the AI Snake Game.
 *
 * Run:   java Main              -> opens the window, press Start Game
 *        java Main --autostart  -> the AI starts playing immediately
 */
public class Main {

    public static void main(String[] args) {
        boolean autoStart = args.length > 0 && args[0].equals("--autostart");
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
            if (autoStart) {
                frame.startGame();
            }
        });
    }
}
