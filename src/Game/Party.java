package Game;

import Entities.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the heroes travelling together on the board.
 *
 * Responsibilities:
 *  - track which heroes are in the party
 *  - track the party's position on the board
 *  - apply movement rules (but not handle input or board printing)
 */
public class Party {

    /**
     * Maximum number of heroes allowed in one party.
     */
    public static final int maxMembers = 3;

    /**
     * List of heroes currently in the party.
     */
    private final List<Hero> members;

    /**
     * Current row of the party on the board.
     */
    private int row;

    /**
     * Current column of the party on the board.
     */
    private int col;

    /**
     * Construct an empty party at the given starting coordinates.
     *
     * @param startRow initial row on the board
     * @param startCol initial column on the board
     */
    public Party(int startRow, int startCol) {
        this(new ArrayList<Hero>(), startRow, startCol);
    }

    /**
     * Construct a party with an initial set of heroes and a starting position.
     *
     * @param initialMembers heroes to add to the party (may be null or partial)
     * @param startRow       initial row on the board
     * @param startCol       initial column on the board
     */
    public Party(List<Hero> initialMembers, int startRow, int startCol) {
        this.members = new ArrayList<Hero>();
        if (initialMembers != null) {
            for (Hero hero : initialMembers) {
                addMember(hero);
            }
        }
        this.row = startRow;
        this.col = startCol;
    }

    /**
     * Attempt to add a hero to the party.
     *
     * @param hero hero to add
     * @return true if the hero was successfully added; false if the hero is null,
     *         the party is full, or the hero is already in the party.
     */
    public boolean addMember(Hero hero) {
        if (hero == null) {
            return false;
        }
        if (members.size() >= maxMembers) {
            return false;
        }
        if (members.contains(hero)) {
            return false;
        }
        members.add(hero);
        return true;
    }

    /**
     * Get an unmodifiable view of the current party members.
     * This protects encapsulation: callers cannot directly modify the
     * underlying list.
     *
     * @return read-only list of heroes
     */
    public List<Hero> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * @return how many heroes are currently in the party.
     */
    public int size() {
        return members.size();
    }

    /**
     * @return true if the party has reached {@link #maxMembers}.
     */
    public boolean isFull() {
        return members.size() >= maxMembers;
    }

    /**
     * @return current row of the party on the board.
     */
    public int getRow() {
        return row;
    }

    /**
     * @return current column of the party on the board.
     */
    public int getCol() {
        return col;
    }

    /**
     * Moves the party in the given direction if the target tile is valid.
     * This method:
     *     Translates WASD into row/column offsets.
     *     Asks the {@link Board} whether the target is inside the map.
     *     Checks if the target tile is accessible.
     *     Updates the party's position on success.
     * It does not print or manage the board, only the party coordinates.
     *
     * @param direction character command: W/A/S/D (case-insensitive)
     * @param board     current board to validate movement against
     */
    public void move(char direction, Board board) {
        direction = Character.toUpperCase(direction);
        int newRow = row;
        int newCol = col;

        switch (direction) {
            case 'W': newRow--; break;
            case 'S': newRow++; break;
            case 'A': newCol--; break;
            case 'D': newCol++; break;
            default:
                System.out.println("Unknown command. Use W/A/S/D.");
                return;
        }

        if (!board.isInside(newRow, newCol)) {
            System.out.println("You can’t move outside the map!");
            return;
        }

        Tile target = board.getTile(newRow, newCol);
        if (!target.isAccessible()) {
            System.out.println("That space is inaccessible. Pick a different direction.");
            return;
        }

        row = newRow;
        col = newCol;

        System.out.println("You moved onto a " + target.getDescription() + " tile.");
    }
}
