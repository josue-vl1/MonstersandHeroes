package Entities;

/**
 * Paladin hero class.
 * A Paladin is a type of Hero that favors both strength and dexterity
 * when leveling up (i.e., good with physical attacks and spells),
 * while agility grows at a normal rate.
 * This class uses the Builder pattern to construct Paladin instances
 * from the hero database (Paladins.txt) in a clear and flexible way.
 */
public class Paladin extends Hero {

    /**
     * Private constructor.
     * Only the nested Builder can create Paladin instances, enforcing
     * the use of the Builder pattern.
     *
     * @param b the Builder that holds all initialization values
     */
    private Paladin(Builder b) {
        super(
                b.name,
                b.level,
                b.baseHP,
                b.baseMP,
                b.strength,
                b.dexterity,
                b.agility,
                b.gold,
                b.exp
        );
    }

    /**
     * Builder for Paladin objects.
     * Example usage:
     * Paladin p = new Paladin.Builder("Sehanine_Moonbow", 1)
     *                    .baseHP(100)
     *                    .baseMP(200)
     *                    .strength(700)
     *                    .dexterity(700)
     *                    .agility(250)
     *                    .gold(3000)
     *                    .exp(0)
     *                    .build();
     */
    public static class Builder {

        // Required parameters
        private final String name;
        private final int level;

        // Configurable parameters (set through builder methods)
        private int baseHP;
        private int baseMP;
        private int strength;
        private int dexterity;
        private int agility;
        private int gold;
        private int exp;

        /**
         * Builder constructor with mandatory fields.
         *
         * @param name  the paladin's name
         * @param level the starting level
         */
        public Builder(String name, int level) {
            this.name = name;
            this.level = level;
        }

        /**
         * Set base HP (max health).
         */
        public Builder baseHP(int baseHP) {
            this.baseHP = baseHP;
            return this;
        }

        /**
         * Set base MP (max mana).
         */
        public Builder baseMP(int baseMP) {
            this.baseMP = baseMP;
            return this;
        }

        /**
         * Set base strength.
         */
        public Builder strength(int strength) {
            this.strength = strength;
            return this;
        }

        /**
         * Set base dexterity.
         */
        public Builder dexterity(int dexterity) {
            this.dexterity = dexterity;
            return this;
        }

        /**
         * Set base agility.
         */
        public Builder agility(int agility) {
            this.agility = agility;
            return this;
        }

        /**
         * Set starting gold.
         */
        public Builder gold(int gold) {
            this.gold = gold;
            return this;
        }

        /**
         * Set starting experience.
         */
        public Builder exp(int exp) {
            this.exp = exp;
            return this;
        }

        /**
         * Final step of the Builder pattern: create the Paladin instance.
         *
         * @return a fully constructed Paladin
         */
        public Paladin build() {
            return new Paladin(this);
        }
    }

    /**
     * Define how a Paladin's stats grow when leveling up.
     * Paladins favor strength and dexterity (both grow by 10%),
     * while agility grows at a normal rate (5%).
     */
    @Override
    protected void applyLevelUpStatGrowth() {
        strength  = (int) Math.round(strength  * 1.10); // favored
        dexterity = (int) Math.round(dexterity * 1.10); // favored
        agility   = (int) Math.round(agility   * 1.05); // normal
    }
}
