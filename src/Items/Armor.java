package Items;

/**
 * Armor item that reduces incoming physical damage to a hero.
 *
 * Extends the generic {@link Item} with an extra stat:
 *  - damageReduction: how much protection this armor provides.
 *
 * Used by heroes as their equipped armor in battle.
 */
public class Armor extends Item {

    /**
     * Amount of damage reduction this armor provides.
     * Higher values mean better protection.
     */
    private final int damageReduction;

    /**
     * Construct a new Armor instance.
     *
     * @param name            display name of the armor (e.g. "Platinum_Shield")
     * @param price           gold cost to buy this armor
     * @param level           minimum hero level required to equip it
     * @param damageReduction damage reduction value provided by this armor
     */
    public Armor(String name, int price, int level, int damageReduction) {
        super(name, price, level);
        this.damageReduction = damageReduction;
    }

    /**
     * @return the armor's damage reduction value.
     */
    public int getDamageReduction() {
        return damageReduction;
    }

    /**
     * Column headers for printing armor in tables (market, inventory, etc.).
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] { "Name", "Price", "Level", "Defense" };
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
                String.valueOf(damageReduction)
        };
    }

    /**
     * Debug-friendly string representation.
     */
    @Override
    public String toString() {
        return "Armor{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", Level=" + level +
                ", damageReduction=" + damageReduction +
                '}';
    }
}
