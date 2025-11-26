package Items;

import java.util.List;

/**
 * Weapon item that a hero can equip to increase physical damage.
 *
 * Extends the generic {@link Item} with:
 *  - damageValue: how much damage this weapon contributes
 *  - handsRequired: how many hands are needed to wield it
 *
 * In your current rules (see Hero.equipWeapon):
 *  - handsRequired == 1 → one-handed weapon
 *  - handsRequired == 2 → two-handed weapon (occupies both hands)
 */
public class Weapon extends Item {

    /**
     * Base damage contributed by this weapon when the hero attacks.
     */
    private final int damageValue;

    /**
     * Number of hands required to use this weapon.
     * Typical values:
     *  - 1: one-handed
     *  - 2: two-handed
     */
    private final int handsRequired;

    /**
     * Construct a new Weapon instance.
     *
     * @param name          display name of the weapon
     * @param price         gold cost to buy this weapon
     * @param level         minimum hero level required to equip it
     * @param damageValue   damage contributed by this weapon
     * @param handsRequired how many hands are required (1 or 2)
     */
    public Weapon(String name, int price, int level, int damageValue, int handsRequired) {
        super(name, price, level);
        this.damageValue = damageValue;
        this.handsRequired = handsRequired;
    }

    /**
     * @return the weapon's damage value used in attack calculations.
     */
    public int getDamageValue() {
        return damageValue;
    }

    /**
     * @return how many hands are required to wield this weapon.
     */
    public int getHandsRequired() {
        return handsRequired;
    }

    /**
     * Debug-friendly description of this weapon.
     */
    @Override
    public String toString() {
        return "Weapon{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", Level=" + level +
                ", damage=" + damageValue +
                ", handsRequired=" + handsRequired +
                '}';
    }

    /**
     * Column headers for printing weapons in tables (market, inventory).
     */
    @Override
    public String[] getColumnHeaders() {
        return new String[] { "Name", "Price", "Level", "Damage", "Hands Req." };
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
                String.valueOf(damageValue),
                String.valueOf(handsRequired)
        };
    }
}
