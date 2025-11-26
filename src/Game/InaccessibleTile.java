package Game;

/**
 * An InaccessibleTile represents a blocked cell on the board.
 * The party cannot move onto this tile. Visually, this tile is
 * rendered with a red 'X' in the ASCII map.
 */
public class InaccessibleTile extends Tile {

    /**
     * Inaccessible tiles cannot be entered by the party.
     *
     * @return false, since this tile is blocked.
     */
    @Override
    public boolean isAccessible() {
        return false;
    }

    /**
     * Render this tile in the board view.
     * Only the 'X' character is colored red; the surrounding spaces
     * keep the cell width consistent with other tiles.
     *
     * @return a fixed-width string containing a red 'X'.
     */
    @Override
    public String render() {
        return "  " + colorRed + "X" + reset + "  ";
    }

    /**
     * @return a short description label for this tile type.
     */
    @Override
    public String getDescription() {
        return "inaccessible";
    }
}
