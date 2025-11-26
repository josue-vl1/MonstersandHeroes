package Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the game board (grid) for the world of play.
 * The board is a square grid of {@link Tile} objects, which can be:
 *     Inaccessible tiles (cannot be entered).
 *     Common tiles (normal walkable tiles, where random battles can occur).
 *     Market tiles (where heroes can buy/sell items).
 * The board is generated randomly with a fixed percentage of inaccessible tiles
 * and a fixed number of market tiles placed on otherwise common tiles.
 */
public class Board {

    /**
     * Probability that a tile is inaccessible (e.g., 0.20 = 20%).
     */
    private static final double inaccesibleRate = 0.20;

    /**
     * Number of market tiles to create on the board.
     * These are chosen randomly among the common tiles.
     */
    private static final int marketAmount = 5;

    /**
     * The dimension of the board (size x size).
     */
    private final int size;

    /**
     * 2D grid of tiles that make up the board.
     */
    private final Tile[][] tiles;

    /**
     * Random generator used to place inaccessible and market tiles.
     */
    private final Random random = new Random();

    /**
     * List of coordinates [row, col] where market tiles are placed.
     */
    private final List<int[]> marketPositions = new ArrayList<>();
    private int startRow;
    private int startCol;

    /**
     * Construct a Board of the given size.
     * This constructor:
     *     Generates the tile layout (inaccessible, common, market).
     *     Chooses a starting position on the first accessible tile found.
     *
     * @param size the width/height of the square board
     */
    public Board(int size) {
        this.size = size;
        this.tiles = new Tile[size][size];
        generateTiles();
        chooseStartPosition();
    }

    /**
     * Randomly generate the layout of the board:
     *     First, each cell is either Inaccessible or Common based on
     *     {@link #inaccesibleRate}.
     *     Then, a fixed number of these common tiles are converted into
     *     Market tiles (chosen randomly).
     */
    private void generateTiles() {
        List<int[]> commonCoords = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                double d = random.nextDouble();
                if (d < inaccesibleRate) {
                    tiles[r][c] = new InaccessibleTile();
                } else {
                    tiles[r][c] = new CommonTile();
                    commonCoords.add(new int[]{r, c});
                }
            }
        }

        if (!commonCoords.isEmpty()) {
            Collections.shuffle(commonCoords, random);
            for (int i = 0; i < marketAmount; i++) {
                int[] pos = commonCoords.get(i);
                tiles[pos[0]][pos[1]] = new MarketTile();
                marketPositions.add(new int[]{pos[0], pos[1]});
            }
        }
    }

    /**
     * Choose a starting position for the party.
     * The logic scans from top-left to bottom-right and picks the first
     * accessible tile. If, for some reason, no tiles are accessible,
     * it forces (0, 0) to be a common, accessible tile.
     */
    private void chooseStartPosition() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (tiles[r][c].isAccessible()) {
                    startRow = r;
                    startCol = c;
                    return;
                }
            }
        }
        startRow = 0;
        startCol = 0;
        tiles[0][0] = new CommonTile();
    }

    /**
     * Check whether a given (row, col) coordinate is inside the board.
     */
    public boolean isInside(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    /**
     * Get the tile at the given coordinates.
     *
     * @param row row index
     * @param col column index
     * @return the Tile at (row, col)
     */
    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    /**
     * @return the size of the board (size x size)
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the starting row index for the party
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * @return the starting column index for the party
     */
    public int getStartCol() {
        return startCol;
    }

    /**
     * @return an unmodifiable list of all market tile coordinates [row, col]
     */
    public List<int[]> getMarketPositions() {
        return Collections.unmodifiableList(marketPositions);
    }

    /**
     * Print an ASCII representation of the board to the console.
     * Each cell is drawn as a box with:
     *     "H" (green) for the party position.
     *     "M" (yellow) for a Market tile.
     *     "X" (red) for an Inaccessible tile.
     *     Blank for a Common tile.
     * A legend is displayed under the board explaining the symbols.
     *
     * @param party the current party (used to draw the "H" marker), can be null
     */
    public void print(Party party) {
        System.out.println();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                System.out.print("+-----");
            }
            System.out.println("+");

            for (int c = 0; c < size; c++) {
                System.out.print("|");
                boolean hasParty = (party != null
                        && party.getRow() == r
                        && party.getCol() == c);

                String cellContent;
                if (hasParty) {
                    cellContent = "  " + Tile.colorGreen + "H" + Tile.reset + "  ";
                } else {
                    cellContent = tiles[r][c].render();
                }

                System.out.print(cellContent);
            }
            System.out.println("|");
        }

        for (int c = 0; c < size; c++) {
            System.out.print("+-----");
        }
        System.out.println("+");
        System.out.println("Legend:");
        System.out.println("  " + Tile.colorGreen + "H" + Tile.reset + "  : Hero party");
        System.out.println("  " + Tile.colorYellow + "M" + Tile.reset + "  : Market");
        System.out.println("  " + Tile.colorRed + "X" + Tile.reset + "  : Inaccessible");
        System.out.println("       (blank) : Common\n");
    }
}
