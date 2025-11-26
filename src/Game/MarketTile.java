package Game;

/**
 * A MarketTile represents a walkable tile that contains a market.
 * When the party steps on this tile, they have the option to enter
 * the {@link Market} associated with this position on the board.
 * Visually, this tile is rendered with a yellow 'M' in the ASCII map.
 */
public class MarketTile extends Tile {

    /**
     * Market tiles are walkable, so the party can stand here.
     *
     * @return true, since this tile is accessible.
     */
    @Override
    public boolean isAccessible() {
        return true;
    }

    /**
     * Indicates that this tile hosts a market.
     *
     * @return true, since this tile is a market.
     */
    @Override
    public boolean isMarket() {
        return true;
    }

    /**
     * Render this tile in the board view.
     * Only the 'M' character is colored yellow; the surrounding spaces
     * keep the cell width consistent with other tiles.
     *
     * @return a fixed-width string containing a yellow 'M'.
     */
    @Override
    public String render() {
        return "  " + colorYellow + "M" + reset + "  ";
    }

    /**
     * @return a short description label for this tile type.
     */
    @Override
    public String getDescription() {
        return "market";
    }
}
