package Entities;

/**
 * Dragon is a specific type of Monster.
 * It uses the Builder pattern so that we can construct Dragon objects
 * in a readable way from the monster database (Dragons.txt), where
 * different stats (HP, damage, defense, dodge) are set step by step.
 */
public class Dragon extends Monster {

    /**
     * Private constructor.
     * Only the nested Builder is allowed to call this constructor.
     * This enforces the use of the Builder pattern when creating Dragons.
     */
    private Dragon(Dragon.Builder b) {
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
     * Builder for Dragon objects.
     * Example usage:
     * Dragon d = new Dragon.Builder("Natsunomeryu", 3)
     *                  .baseHP(300)
     *                  .baseDamage(50)
     *                  .defense(40)
     *                  .dodge(25)
     *                  .build();
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
         * @param name  the name of the dragon
         * @param level the level of the dragon (used to scale its stats)
         */
        public Builder(String name, int level) {
            this.name = name;
            this.level = level;
        }

        /**
         * Set the base HP (maximum health) for this dragon.
         */
        public Dragon.Builder baseHP(int baseHP) {
            this.baseHP = baseHP;
            return this;
        }

        /**
         * Set the base damage this dragon can deal.
         */
        public Dragon.Builder baseDamage(int baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        /**
         * Set the defense value (how much damage is reduced when attacked).
         */
        public Dragon.Builder defense(int defense) {
            this.defense = defense;
            return this;
        }

        /**
         * Set the dodge chance (percentage chance to avoid an attack).
         */
        public Dragon.Builder dodge(int dodge) {
            this.dodge = dodge;
            return this;
        }

        /**
         * Final step of the Builder pattern: actually create the Dragon.
         *
         * @return a fully constructed Dragon instance
         */
        public Dragon build() {
            return new Dragon(this);
        }
    }
}
