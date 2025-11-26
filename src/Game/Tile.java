package Game;

/**
 * Abstract base class for all types of tiles on the game board.
 *
 * Concrete subclasses define:
 *  - whether the tile is accessible (party can stand on it)
 *  - whether it behaves like a market
 *  - how it is rendered in the ASCII map
 *  - a short text description
 *
 * Examples of subclasses:
 *  - {@link CommonTile}
 *  - {@link InaccessibleTile}
 *  - {@link MarketTile}
 */
public abstract class Tile {

    /**
     * Reset all terminal color/formatting.
     */
    public static final String reset = "\u001B[0m";

    /**
     * Red foreground color (used for inaccessible tiles).
     */
    public static final String colorRed = "\u001B[31m";

    /**
     * Green foreground color (used for the hero party marker).
     */
    public static final String colorGreen = "\u001B[32m";

    /**
     * Yellow foreground color (used for market tiles).
     */
    public static final String colorYellow = "\u001B[33m";

    /**
     * @return true if the party can stand on this tile, false if it is blocked.
     */
    public abstract boolean isAccessible();

    /**
     * @return true if this tile hosts a market, false otherwise.
     *         Default implementation is false; {@link MarketTile} overrides it.
     */
    public boolean isMarket() {
        return false;
    }

    /**
     * How this tile looks when there is NO party on it.
     * Implementations should return a fixed-width string (5 characters)
     * so the board grid stays aligned. The {@link Board#print(Party)} method
     * relies on this width to draw a clean ASCII map.
     *
     * @return 5-character string representing this tile in the map.
     */
    public abstract String render();

    /**
     * Short description used in log / status messages.
     * Examples: "market", "common", "inaccessible".
     *
     * @return a short label describing this tile type.
     */
    public abstract String getDescription();
}
