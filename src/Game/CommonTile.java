package Game;

/**
 * A CommonTile represents a normal walkable tile on the board.
 * Heroes can move onto common tiles, and typically these are the tiles
 * where random battles can occur. Visually, this tile is rendered as a
 * blank cell in the ASCII board.
 */
public class CommonTile extends Tile {

    /**
     * Common tiles are always accessible.
     *
     * @return true, since the party can stand on this tile.
     */
    @Override
    public boolean isAccessible() {
        return true;
    }

    /**
     * Render this tile in the board view.
     * Common tiles are shown as a blank cell (just spaces), so that
     * markets and inaccessible tiles stand out more clearly.
     *
     * @return a fixed-width blank string representing this tile.
     */
    @Override
    public String render() {
        return "     ";
    }

    /**
     * @return a short description label for this tile type.
     */
    @Override
    public String getDescription() {
        return "common";
    }
}
