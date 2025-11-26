package Items;

/**
 * Concrete spell type representing a lightning-based spell.
 *
 * In battle, Lightning spells typically reduce a monster's dodge chance
 * (see Battle.handleHeroCastSpell and Monster.reduceDodgePercent).
 */
public final class LightningSpell extends Spell {

    /**
     * Construct a new LightningSpell.
     *
     * @param name     display name of the spell (e.g. "Lightning_Dagger")
     * @param price    gold cost to buy this spell
     * @param level    minimum hero level required to use it
     * @param damage   base damage dealt by this spell
     * @param manaCost MP required to cast this spell
     */
    public LightningSpell(String name, int price, int level, int damage, int manaCost) {
        super(name, price, level, damage, manaCost);
    }

    /**
     * @return the element type of this spell: "Lightning".
     */
    @Override
    public String getElement() {
        return "Lightning";
    }
}
