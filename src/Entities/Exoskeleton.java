package Entities;

/**
 * Exoskeleton is a specific type of Monster.
 * It uses the Builder pattern so that we can construct Exoskeleton
 * objects in a clear and flexible way from the monster database
 * (Exoskeletons.txt), setting its stats step by step.
 */
public class Exoskeleton extends Monster {

    /**
     * Private constructor.
     * Only the nested Builder is allowed to call this constructor.
     * This enforces the use of the Builder pattern when creating
     * Exoskeleton instances.
     *
     * @param b the Builder that holds all initialization values
     */
    private Exoskeleton(Exoskeleton.Builder b) {
        super(
                b.name,
                b.level,
                b.baseHP,
                b.baseDamage,
                b.defense,
                b.dodge
        );
    }

    /**
     * Builder for Exoskeleton objects.
     * Example usage:
     * Exoskeleton e = new Exoskeleton.Builder("Kraxos", 5)
     *                      .baseHP(500)
     *                      .baseDamage(60)
     *                      .defense(80)
     *                      .dodge(10)
     *                      .build();
     */
    public static class Builder {

        private final String name;
        private final int level;
        private int baseHP;
        private int baseDamage;
        private int defense;
        private int dodge;

        /**
         * Builder constructor with the mandatory fields.
         *
         * @param name  the name of the exoskeleton
         * @param level the level of the exoskeleton (used to scale its stats)
         */
        public Builder(String name, int level) {
            this.name = name;
            this.level = level;
        }

        /**
         * Set the base HP (maximum health) for this exoskeleton.
         *
         * @param baseHP the base health points
         * @return this Builder instance (for method chaining)
         */
        public Exoskeleton.Builder baseHP(int baseHP) {
            this.baseHP = baseHP;
            return this;
        }

        /**
         * Set the base damage this exoskeleton can deal.
         *
         * @param baseDamage the base damage value
         * @return this Builder instance (for method chaining)
         */
        public Exoskeleton.Builder baseDamage(int baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        /**
         * Set the defense value (how much incoming damage is reduced).
         *
         * @param defense the defense stat
         * @return this Builder instance (for method chaining)
         */
        public Exoskeleton.Builder defense(int defense) {
            this.defense = defense;
            return this;
        }

        /**
         * Set the dodge chance (percentage chance to avoid an attack).
         *
         * @param dodge the dodge chance
         * @return this Builder instance (for method chaining)
         */
        public Exoskeleton.Builder dodge(int dodge) {
            this.dodge = dodge;
            return this;
        }

        /**
         * Final step of the Builder pattern: actually create the Exoskeleton.
         *
         * @return a fully constructed Exoskeleton instance
         */
        public Exoskeleton build() {
            return new Exoskeleton(this);
        }
    }
}
