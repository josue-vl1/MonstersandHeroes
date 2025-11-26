package Items;

/**
 * Small interface for anything that can be rendered as a row
 * in a text-based table (e.g., market lists, inventory screens).
 *
 * Implementations (like {@link Item} and its subclasses) provide:
 *  - a set of column headers (shared across all rows of that type)
 *  - the values for one specific row
 *
 * The {@link Game.Market} class uses this to print generic tables
 * without caring about the concrete item type.
 */
public interface TableDisplayable {

    /**
     * Column headers for this type in the market / inventory table.
     * Example for a Weapon:
     *  ["Name", "Price", "Level", "Damage", "Hands"]
     *
     * @return array of header strings, in display order.
     */
    String[] getColumnHeaders();

    /**
     * Values for a single row, aligned with {@link #getColumnHeaders()}.
     * Example for a specific weapon instance:
     *  ["Sword", "100", "1", "50", "1"]
     *
     * @return array of column values as strings.
     */
    String[] getColumnValues();
}
