package Entities;

/**
 * Sorcerer hero class.
 * A Sorcerer is a type of Hero that favors dexterity (spell power)
 * and agility (dodge) when leveling up, while strength grows at
 * a normal rate.
 * This class uses the Builder pattern to construct Sorcerer instances
 * from the hero database (Sorcerers.txt) in a clear and flexible way.
 */
public class Sorcerer extends Hero {

    /**
     * Private constructor.
     * Only the nested Builder can create Sorcerer instances, enforcing
     * the use of the Builder pattern.
     *
     * @param b the Builder that holds all initialization values
     */
    private Sorcerer(Sorcerer.Builder b) {
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
     * Builder for Sorcerer objects.
     * Example usage:
     * Sorcerer s = new Sorcerer.Builder("Eunoia_Cyn", 1)
     *                     .baseHP(100)
     *                     .baseMP(550)
     *                     .strength(350)
     *                     .dexterity(800)
     *                     .agility(600)
     *                     .gold(2500)
     *                     .exp(0)
     *                     .build();
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
         * @param name  the sorcerer's name
         * @param level the starting level
         */
        public Builder(String name, int level) {
            this.name = name;
            this.level = level;
        }

        /**
         * Set base HP (max health).
         */
        public Sorcerer.Builder baseHP(int baseHP) {
            this.baseHP = baseHP;
            return this;
        }

        /**
         * Set base MP (max mana).
         */
        public Sorcerer.Builder baseMP(int baseMP) {
            this.baseMP = baseMP;
            return this;
        }

        /**
         * Set base strength.
         */
        public Sorcerer.Builder strength(int strength) {
            this.strength = strength;
            return this;
        }

        /**
         * Set base dexterity (spell power).
         */
        public Sorcerer.Builder dexterity(int dexterity) {
            this.dexterity = dexterity;
            return this;
        }

        /**
         * Set base agility (dodge).
         */
        public Sorcerer.Builder agility(int agility) {
            this.agility = agility;
            return this;
        }

        /**
         * Set starting gold.
         */
        public Sorcerer.Builder gold(int gold) {
            this.gold = gold;
            return this;
        }

        /**
         * Set starting experience.
         */
        public Sorcerer.Builder exp(int exp) {
            this.exp = exp;
            return this;
        }

        /**
         * Final step of the Builder pattern: create the Sorcerer instance.
         *
         * @return a fully constructed Sorcerer
         */
        public Sorcerer build() {
            return new Sorcerer(this);
        }
    }

    /**
     * Define how a Sorcerer's stats grow when leveling up.
     * Sorcerers favor dexterity and agility (both grow by 10%),
     * while strength grows at a normal rate (5%).
     */
    @Override
    protected void applyLevelUpStatGrowth() {
        dexterity = (int) Math.round(dexterity * 1.10); // favored
        agility   = (int) Math.round(agility   * 1.10); // favored
        strength  = (int) Math.round(strength  * 1.05); // normal
    }
}
