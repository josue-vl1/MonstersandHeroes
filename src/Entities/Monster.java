package Entities;

/**
 * Base class for all monster types (Dragon, Spirit, Exoskeleton, etc.).
 * A Monster is an Entity that has HP, base damage, defense, and dodge chance.
 * It also keeps track of its original combat stats so that temporary debuffs
 * (from spells) can be reset between battles.
 */
public class Monster extends Entity {

    // --- CORE COMBAT STATS ---

    /**
     * Base (maximum) HP and current HP.
     * HP represents how much damage the monster can take before dying.
     */
    protected int baseHP;
    protected int HP;

    /**
     * Base damage the monster deals with a normal attack.
     */
    protected int baseDamage;

    /**
     * Defense reduces incoming damage.
     */
    protected int defense;

    /**
     * Dodge chance, usually interpreted as a percentage (0–100),
     * representing the probability to avoid an incoming attack.
     */
    protected int dodge;

    // --- ORIGINAL STATS FOR RESETTING BETWEEN BATTLES ---

    /**
     * Original (unmodified) damage value. Used to restore the monster
     * after a battle, since spells can temporarily reduce its damage.
     */
    protected final int originalBaseDamage;

    /**
     * Original defense value before any debuffs.
     */
    protected final int originalDefense;

    /**
     * Original dodge chance before any debuffs.
     */
    protected final int originalDodge;

    /**
     * Construct a Monster with all core stats set.
     *
     * @param name       monster name
     * @param level      monster level
     * @param baseHP     base (max) HP
     * @param baseDamage base damage
     * @param defense    defense stat
     * @param dodge      dodge chance (percentage)
     */
    protected Monster(String name, int level,
                      int baseHP, int baseDamage,
                      int defense, int dodge) {
        super(name, level);
        this.baseHP = baseHP;
        this.HP = baseHP;
        this.baseDamage = baseDamage;
        this.defense = defense;
        this.dodge = dodge;

        // Save original stats so we can reset after the battle
        this.originalBaseDamage = baseDamage;
        this.originalDefense = defense;
        this.originalDodge = dodge;
    }

    // --- BASIC GETTERS ---

    public int getBaseHP() {
        return baseHP;
    }

    public int getHP() {
        return HP;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public int getDefense() {
        return defense;
    }

    public int getDodge() {
        return dodge;
    }

    // --- STATUS & DAMAGE HANDLING ---

    /**
     * @return true if the monster's HP has dropped to 0 or below
     */
    public boolean isDead() {
        return HP <= 0;
    }

    /**
     * Apply damage to the monster.
     * HP will not go below 0, and no damage is applied if amount is
     * non-positive or the monster is already dead.
     *
     * @param amount damage to apply
     */
    public void takeDamage(int amount) {
        if (amount <= 0 || isDead()) {
            return;
        }
        HP = Math.max(0, HP - amount);
    }

    /**
     * Reset the monster's stats before a new battle.
     * Restores HP to full and resets damage, defense, and dodge
     * to their original values (removing spell debuffs).
     */
    public void resetForBattle() {
        HP = baseHP;
        baseDamage = originalBaseDamage;
        defense = originalDefense;
        dodge = originalDodge;
    }

    // --- SPELL DEBUFF HELPERS ---

    /**
     * Reduce the monster's defense by a given percentage.
     * For example, percent = 0.2 means reduce defense by 20%.
     * Defense is clamped to be non-negative.
     *
     * @param percent fraction between 0 and 1 representing the reduction
     */
    public void reduceDefensePercent(double percent) {
        if (percent <= 0) return;
        defense = (int) Math.round(defense * (1.0 - percent));
        if (defense < 0) defense = 0;
    }

    /**
     * Reduce the monster's base damage by a given percentage.
     * Damage is clamped to be non-negative.
     *
     * @param percent fraction between 0 and 1 representing the reduction
     */
    public void reduceDamagePercent(double percent) {
        if (percent <= 0) return;
        baseDamage = (int) Math.round(baseDamage * (1.0 - percent));
        if (baseDamage < 0) baseDamage = 0;
    }

    /**
     * Reduce the monster's dodge chance by a given percentage.
     * Dodge is clamped to be non-negative.
     *
     * @param percent fraction between 0 and 1 representing the reduction
     */
    public void reduceDodgePercent(double percent) {
        if (percent <= 0) return;
        dodge = (int) Math.round(dodge * (1.0 - percent));
        if (dodge < 0) dodge = 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + getName() + '\'' +
                ", level=" + getLevel() +
                ", HP=" + HP + "/" + baseHP +
                ", dmg=" + baseDamage +
                ", def=" + defense +
                ", dodge=" + dodge +
                "%}";
    }
}
