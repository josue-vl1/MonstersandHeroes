package Items;

/**
 * Abstract base class for all spells in the game.
 *
 * Every spell is an {@link Item} with:
 *  - a base damage value
 *  - a mana cost to cast
 *  - an element type (Fire, Ice, Lightning, etc.)
 *
 * Concrete subclasses:
 *  - {@link FireSpell}
 *  - {@link IceSpell}
 *  - {@link LightningSpell}
 *
 * In battle, spells are cast in {@code Battle.handleHeroCastSpell},
 * which uses {@link #getDamage()}, {@link #getManaCost()}, and the
 * concrete element type to apply extra debuffs.
 */
public abstract class Spell extends Item {

    /**
     * Base damage dealt by this spell before dexterity scaling.
     */
    protected final int damage;

    /**
     * Mana points required to cast this spell once.
     */
    protected final int manaCost;

    /**
     * Protected constructor; only concrete spell types can create instances.
     *
     * @param name     display name of the spell
     * @param price    gold cost to buy this spell
     * @param level    minimum hero level required to use it
     * @param damage   base damage value
     * @param manaCost MP cost per cast
     */
    protected Spell(String name, int price, int level, int damage, int manaCost) {
        super(name, price, level);
        this.damage = damage;
        this.manaCost = manaCost;
    }

    /**
     * @return the spell's base damage.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @return how much MP is required to cast this spell.
     */
    public int getManaCost() {
        return manaCost;
    }

    /**
     * Element type of this spell.
     * <p>
     * Implemented by subclasses:
     *  - "Fire"
     *  - "Ice"
     *  - "Lightning"
     *
     * @return a string describing the element.
     */
    public abstract String getElement();

    /**
     * Column headers for printing spells in tables (market, inventory).
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] { "Name", "Price", "Level", "Damage", "Mana", "Element" };
    }

    /**
     * Column values that correspond to {@link #getColumnHeaders()}.
     */
    @Override
    public String[] getColumnValues() {
        return new String[] {
                name,
                String.valueOf(price),
                String.valueOf(level),
                String.valueOf(damage),
                String.valueOf(manaCost),
                getElement()
        };
    }

    /**
     * Debug-friendly string representation, prefixed by the element.
     */
    @Override
    public String toString() {
        return getElement() + "Spell{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", lvl=" + level +
                ", damage=" + damage +
                ", manaCost=" + manaCost +
                '}';
    }
}
