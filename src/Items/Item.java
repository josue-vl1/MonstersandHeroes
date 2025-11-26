package Items;

/**
 * Base class for all purchasable / ownable objects in the game.
 *
 * Every Item has:
 *  - a name (string identifier displayed in menus)
 *  - a price (gold cost)
 *  - a required level (minimum hero level to use/equip)
 *
 * Concrete subclasses include:
 *  - {@link Weapon}
 *  - {@link Armor}
 *  - {@link Potion}
 *  - {@link Spell} (and its element-specific subclasses)
 *
 * Implements {@link TableDisplayable} so items can be rendered in
 * generic tables (market lists, inventories, etc.).
 */
public abstract class Item implements TableDisplayable {

    /**
     * Display name of the item (e.g. "Sword", "Platinum_Shield").
     */
    protected final String name;

    /**
     * Gold cost of this item in shops.
     */
    protected final int price;

    /**
     * Minimum hero level required to use or equip this item.
     */
    protected final int level;

    /**
     * Protected constructor: only subclasses can create items.
     *
     * @param name  item name
     * @param price item price in gold
     * @param level required hero level
     */
    protected Item(String name, int price, int level) {
        this.name = name;
        this.price = price;
        this.level = level;
    }

    /** @return the item name. */
    public String getName()  { return name; }

    /** @return the item price in gold. */
    public int getPrice()    { return price; }

    /** @return the required hero level for this item. */
    public int getLevel()    { return level; }

    /**
     * Default table headers for all items.
     * <p>
     * Subclasses can override this to add extra columns, but most
     * will still include these three basics.
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] { "Name", "Price", "Level" };
    }

    /**
     * Default table values corresponding to {@link #getColumnHeaders()}.
     * Subclasses can append more values if they extend the headers.
     */
    @Override
    public String[] getColumnValues() {
        return new String[] {
                name,
                String.valueOf(price),
                String.valueOf(level)
        };
    }
}
