package Items;

/**
 * Concrete spell type representing an ice-based spell.
 *
 * In battle, Ice spells typically reduce a monster's base damage
 * (see Battle.handleHeroCastSpell and Monster.reduceDamagePercent).
 */
public final class IceSpell extends Spell {

    /**
     * Construct a new IceSpell.
     *
     * @param name     display name of the spell (e.g. "Snow_Cannon")
     * @param price    gold cost to buy this spell
     * @param level    minimum hero level required to use it
     * @param damage   base damage dealt by this spell
     * @param manaCost MP required to cast this spell
     */
    public IceSpell(String name, int price, int level, int damage, int manaCost) {
        super(name, price, level, damage, manaCost);
    }

    /**
     * @return the element type of this spell: "Ice".
     */
    @Override
    public String getElement() {
        return "Ice";
    }
}
