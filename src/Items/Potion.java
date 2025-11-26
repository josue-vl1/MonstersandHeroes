package Items;

/**
 * Potion item that temporarily or permanently boosts hero stats.
 *
 * Examples from your data files:
 *  - "Health_Potion" (affects HP)
 *  - "Magic_Potion"  (affects MP)
 *  - "Strength_Potion", "Dexterity_Potion", "Agility_Potion"
 *  - "All_Attributes_Potion" (affects multiple stats)
 *
 */
public class Potion extends Item {

    /**
     * The amount by which the affected attribute(s) are increased.
     * For HP/MP this is the number of points restored.
     */
    private final int effectIncrease;

    /**
     * Text label describing which attribute(s) this potion affects.
     * The game logic currently uses a simple string match, e.g.:
     *  - contains "health" or "hp"   -> affects HP
     *  - contains "mana"  or "mp"   -> affects MP
     *  - contains "strength", "dexterity", "agility"
     *  - equals "All"               -> affects all stats
     */
    private final String attributeAffected;

    /**
     * Construct a new Potion.
     *
     * @param name              display name of the potion
     * @param price             gold cost to buy this potion
     * @param level             minimum hero level required to use it
     * @param effectAmount      how much the potion increases the target stat(s)
     * @param attributeAffected which attribute(s) this potion modifies
     */
    public Potion(String name, int price, int level, int effectAmount, String attributeAffected) {
        super(name, price, level);
        this.effectIncrease = effectAmount;
        this.attributeAffected = attributeAffected;
    }

    /**
     * Amount by which the potion increases its target attribute(s).
     * Used directly in Battle when applying the effect.
     */
    public int getEffectIncrease() {
        return effectIncrease;
    }

    /**
     * String label for the affected attribute(s).
     * The Battle code interprets this value to decide what to boost.
     */
    public String getAttributeAffected() {
        return attributeAffected;
    }

    /**
     * Column headers for printing potions in tables (market, inventory).
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] { "Name", "Price", "Level", "Effect", "Affects" };
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
                String.valueOf(effectIncrease),
                attributeAffected
        };
    }

    /**
     * Debug-friendly string representation.
     */
    @Override
    public String toString() {
        return "Potion{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", level=" + level +
                ", effectAmount=" + effectIncrease +
                ", attributeAffected='" + attributeAffected + '\'' +
                '}';
    }
}
