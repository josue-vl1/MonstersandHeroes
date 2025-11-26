package Entities;

/**
 * Spirit is a specific type of Monster.
 * It uses the Builder pattern so that we can construct Spirit
 * objects in a clear and flexible way from the monster database
 * (Spirits.txt), setting its stats step by step.
 */
public class Spirit extends Monster {

    /**
     * Private constructor.
     * Only the nested Builder is allowed to call this constructor.
     * This enforces the use of the Builder pattern when creating
     * Spirit instances.
     *
     * @param b the Builder that holds all initialization values
     */
    private Spirit(Spirit.Builder b) {
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
     * Builder for Spirit objects.
     * Example usage:
     * Spirit s = new Spirit.Builder("Andromalius", 4)
     *                   .baseHP(400)
     *                   .baseDamage(60)
     *                   .defense(50)
     *                   .dodge(30)
     *                   .build();
     */
    public static class Builder {

        // Required parameters
        private final String name;
        private final int level;

        // Configurable parameters (set via builder methods)
        private int baseHP;
        private int baseDamage;
        private int defense;
        private int dodge;

        /**
         * Builder constructor with mandatory fields.
         *
         * @param name  the spirit's name
         * @param level the spirit's level (used to scale its stats)
         */
        public Builder(String name, int level) {
            this.name = name;
            this.level = level;
        }

        /**
         * Set the base HP (maximum health) for this spirit.
         *
         * @param baseHP base health points
         * @return this Builder instance (for method chaining)
         */
        public Spirit.Builder baseHP(int baseHP) {
            this.baseHP = baseHP;
            return this;
        }

        /**
         * Set the base damage this spirit can deal.
         *
         * @param baseDamage base damage value
         * @return this Builder instance (for method chaining)
         */
        public Spirit.Builder baseDamage(int baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        /**
         * Set the defense value (how much incoming damage is reduced).
         *
         * @param defense defense stat
         * @return this Builder instance (for method chaining)
         */
        public Spirit.Builder defense(int defense) {
            this.defense = defense;
            return this;
        }

        /**
         * Set the dodge chance (percentage chance to avoid an attack).
         *
         * @param dodge dodge chance
         * @return this Builder instance (for method chaining)
         */
        public Spirit.Builder dodge(int dodge) {
            this.dodge = dodge;
            return this;
        }

        /**
         * Final step of the Builder pattern: actually create the Spirit.
         *
         * @return a fully constructed Spirit instance
         */
        public Spirit build() {
            return new Spirit(this);
        }
    }
}
