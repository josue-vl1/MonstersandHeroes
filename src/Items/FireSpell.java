package Items;

/**
 * Concrete spell type representing a fire-based spell.
 *
 * In battle, Fire spells typically reduce a monster's defense
 * (see Battle.handleHeroCastSpell and Monster.reduceDefensePercent).
 */
public final class FireSpell extends Spell {

    /**
     * Construct a new FireSpell.
     *
     * @param name     display name of the spell (e.g. "Flame_Tornado")
     * @param price    gold cost to buy this spell
     * @param level    minimum hero level required to use it
     * @param damage   base damage dealt by this spell
     * @param manaCost MP required to cast this spell
     */
    public FireSpell(String name, int price, int level, int damage, int manaCost) {
        super(name, price, level, damage, manaCost);
    }

    /**
     * @return the element type of this spell: "Fire".
     */
    @Override
    public String getElement() {
        return "Fire";
    }
}
