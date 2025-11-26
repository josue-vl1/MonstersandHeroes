import Utility.AllObjectsLoader;
import Utility.GameData;
import Game.Game;

import java.util.Scanner;

/**
 * Entry point for the Legends game.
 *
 * Responsibilities:
 *  - load all game data from the resource files
 *  - create a shared Scanner for console input
 *  - construct and start the {@link Game} loop
 */
public class Main {

    /**
     * Standard Java entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Load all heroes, monsters, and items from the text files.
        GameData data = AllObjectsLoader.loadAllObjects();

        // Use try-with-resources so the Scanner is closed automatically on exit.
        try (Scanner scanner = new Scanner(System.in)) {
            // Create the game coordinator with loaded data and user input.
            Game game = new Game(data, scanner);
            // Hand control over to the Game class.
            game.start();
        }
    }
}
