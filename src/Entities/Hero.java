package Entities;

import Items.Inventory;
import Items.Weapon;
import Items.Armor;

/**
 * Abstract base class for all hero types (Warrior, Paladin, Sorcerer, etc.).
 * <p>
 * A Hero is an Entity that has combat stats (HP, MP, strength, dexterity,
 * agility), resources (gold, experience), equipment (weapons, armor),
 * and an inventory of items. Concrete subclasses specify how stats grow
 * when leveling up.
 */
public abstract class Hero extends Entity {

    // --- CORE PROGRESSION STATS ---

    /**
     * Current experience points.
     * When this reaches the threshold for the next level,
     * the hero levels up.
     */
    protected int exp;

    /**
     * Base (maximum) HP and current HP.
     * HP represents the hero's health in battle.
     */
    protected int baseHP;
    protected int HP;

    /**
     * Base (maximum) MP and current MP.
     * MP is the mana used to cast spells.
     */
    protected int baseMP;
    protected int MP;

    /**
     * Strength affects physical damage.
     * Dexterity affects spell damage.
     * Agility affects dodge chance.
     */
    protected int strength;
    protected int dexterity;
    protected int agility;

    /**
     * Amount of gold the hero currently has.
     * Used to buy/sell items in markets.
     */
    protected int gold;

    // --- EQUIPMENT ---

    /**
     * Main-hand weapon.
     */
    protected Weapon equippedWeapon;

    /**
     * Off-hand weapon (for dual wielding two one-handed weapons).
     * If a two-handed weapon is equipped, this will be null.
     */
    protected Weapon offHandWeapon;

    /**
     * Equipped armor, which reduces incoming damage.
     */
    protected Armor equippedArmor;

    // --- INVENTORY ---

    /**
     * The hero's inventory of items (weapons, armor, potions, spells, etc.).
     */
    protected Inventory inventory = new Inventory();

    /**
     * Construct a Hero with all core stats set.
     *
     * @param name      hero name
     * @param level     initial level
     * @param baseHP    base (max) HP
     * @param baseMP    base (max) MP
     * @param strength  base strength
     * @param dexterity base dexterity
     * @param agility   base agility
     * @param gold      starting gold
     * @param exp       starting experience
     */
    protected Hero(String name, int level, int baseHP, int baseMP,
                   int strength, int dexterity, int agility,
                   int gold, int exp) {
        super(name, level);
        this.baseHP = baseHP;
        this.HP = baseHP;
        this.baseMP = baseMP;
        this.MP = baseMP;
        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;
        this.gold = gold;
        this.exp = exp;
    }

    // --- BASIC GETTERS ---

    public int getBaseHP() {
        return baseHP;
    }

    public int getHP() {
        return HP;
    }

    public int getBaseMP() {
        return baseMP;
    }

    public int getMP() {
        return MP;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getAgility() {
        return agility;
    }

    public int getGold() {
        return gold;
    }

    public int getExp() {
        return exp;
    }

    /**
     * @return the hero's inventory object
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * @return the main-hand weapon, or null if none
     */
    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    /**
     * @return the off-hand weapon (if dual wielding), or null if none
     */
    public Weapon getOffHandWeapon() {
        return offHandWeapon;
    }

    // --- WEAPON EQUIPPING (HAND LOGIC) ---

    /**
     * Equip a weapon, enforcing hand constraints:
     * <ul>
     *     <li>Two-handed weapon (2H): occupies both hands, clears off-hand.</li>
     *     <li>One-handed weapon (1H):
     *         <ul>
     *             <li>If currently using a 2H weapon, replace it with this 1H.</li>
     *             <li>Else, if main hand is empty, put it there.</li>
     *             <li>Else, if off-hand is empty, put it in off-hand (dual wield).</li>
     *             <li>Else, both hands have 1H weapons → replace main-hand.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param weapon the weapon to equip
     * @return true if equipping succeeded, false if weapon is null
     */
    public boolean equipWeapon(Weapon weapon) {
        if (weapon == null) {
            return false;
        }

        int hands = weapon.getHandsRequired();

        if (hands == 2) {
            // Two-handed weapon always occupies both hands
            this.equippedWeapon = weapon;
            this.offHandWeapon = null;
            return true;
        } else {
            // One-handed weapon
            if (equippedWeapon != null && equippedWeapon.getHandsRequired() == 2) {
                // We were using a two-handed weapon; replace it with this 1H
                this.equippedWeapon = weapon;
                this.offHandWeapon = null;
                return true;
            }

            if (equippedWeapon == null) {
                // Main hand empty
                this.equippedWeapon = weapon;
            } else if (offHandWeapon == null) {
                // Off hand empty, main hand already has a 1H
                this.offHandWeapon = weapon;
            } else {
                // Both hands already have 1H weapons -> replace main-hand
                this.equippedWeapon = weapon;
            }
            return true;
        }
    }



    /**
     * @return the currently equipped armor, or null if none
     */
    public Armor getEquippedArmor() {

        return equippedArmor;
    }

    /**
     * Equip the given armor (no special constraints).
     *
     * @param equippedArmor armor instance to equip
     */
    public void setEquippedArmor(Armor equippedArmor) {

        this.equippedArmor = equippedArmor;
    }

    /**
     * @return total damage provided by all equipped weapons (both hands)
     */
    public int getEquippedWeaponDamage() {
        int total = 0;
        if (equippedWeapon != null) {
            total += equippedWeapon.getDamageValue();
        }
        if (offHandWeapon != null) {
            total += offHandWeapon.getDamageValue();
        }
        return total;
    }

    /**
     * @return damage reduction provided by equipped armor, or 0 if none
     */
    public int getEquippedArmorReduction() {

        return (equippedArmor != null) ? equippedArmor.getDamageReduction() : 0;
    }

    // --- GOLD / MONEY MANAGEMENT ---

    /**
     * Increase the hero's gold.
     *
     * @param amount amount of gold to add
     */
    public void addGold(int amount) {

        gold += amount;
    }

    /**
     * Try to spend the given amount of gold.
     *
     * @param amount amount to spend
     * @return true if the hero had enough gold, false otherwise
     */
    public boolean spendGold(int amount) {
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    // --- HP / MP & STATUS ---

    /**
     * @return true if the hero's HP has dropped to 0 or below
     */
    public boolean isFainted() {

        return HP <= 0;
    }

    /**
     * Apply damage to the hero.
     * HP will not go below 0, and no damage is applied if amount is
     * non-positive or the hero has already fainted.
     *
     * @param amount damage amount
     */
    public void takeDamage(int amount) {
        if (amount <= 0 || isFainted()) {
            return;
        }
        HP = Math.max(0, HP - amount);
    }

    /**
     * Regenerate a portion of HP and MP at the end of a battle round.
     * <p>
     * By default, increases both HP and MP by 10%, capped at baseHP/baseMP.
     * Does nothing if the hero has fainted.
     */
    public void regenAfterRound() {
        if (isFainted()) return;

        HP = Math.min(baseHP, (int) Math.round(HP * 1.1));
        MP = Math.min(baseMP, (int) Math.round(MP * 1.1));
    }

    /**
     * Revive the hero after battle if they fainted.
     * <p>
     * Project rule: revive with half of base HP and base MP.
     */
    public void reviveAfterBattle() {
        if (isFainted()) {
            HP = baseHP / 2;
            MP = baseMP / 2;
        }
    }


    /**
     * Attempt to spend the given amount of MP for spell casting.
     *
     * @param amount mana cost
     * @return true if there was enough MP, false otherwise
     */
    public boolean spendMP(int amount) {
        if (MP < amount) {
            return false;
        }
        MP -= amount;
        return true;
    }

    // === POTION HELPERS ===

    /**
     * Increase HP by a certain amount.
     * <p>
     * According to the project rule, potions are allowed to raise HP
     * beyond baseHP (no cap here).
     */
    public void addHP(int amount) {

        HP += amount;
    }

    /**
     * Increase MP by a certain amount.
     * Potions can raise MP beyond baseMP.
     */
    public void addMP(int amount) {

        MP += amount;
    }

    /**
     * Increase strength by a certain amount.
     */
    public void addStrength(int amount) {

        strength += amount;
    }

    /**
     * Increase dexterity by a certain amount.
     */
    public void addDexterity(int amount) {

        dexterity += amount;
    }

    /**
     * Increase agility by a certain amount.
     */
    public void addAgility(int amount) {

        agility += amount;
    }

    // --- EXPERIENCE / LEVELING ---

    /**
     * Add experience points and check for level-ups.
     *
     * @param amount experience to add
     */
    public void gainExperience(int amount) {
        if (amount <= 0) return;
        exp += amount;
        // Level up as many times as needed if a large exp gain happens
        while (exp >= requiredExpForNextLevel()) {
            exp -= requiredExpForNextLevel();
            levelUp();
        }
    }

    /**
     * Apply a level-up:
     * <ul>
     *     <li>Increase level by 1.</li>
     *     <li>Increase baseHP and baseMP by 10%.</li>
     *     <li>Restore HP and MP to full.</li>
     *     <li>Delegate stat growth (strength/dex/agi) to subclass.</li>
     * </ul>
     */
    protected void levelUp() {
        level++;

        // HP / MP growth common to all heroes
        baseHP = (int) Math.round(baseHP * 1.1);  // +10%
        baseMP = (int) Math.round(baseMP * 1.1);  // +10%
        this.HP = baseHP;
        this.MP = baseMP;

        // Let subclasses decide how strength/dex/agi grow
        applyLevelUpStatGrowth();

        System.out.printf("%s leveled up to level %d!%n", name, level);
    }

    /**
     * Subclasses (Warrior, Paladin, Sorcerer) must implement
     * how their main stats grow on level up.
     */
    protected abstract void applyLevelUpStatGrowth();

    /**
     * Compute the experience required for the next level.
     * <p>
     * This can be tuned to match the assignment specification.
     * Currently: required exp = level * 10.
     */
    private int requiredExpForNextLevel() {
        return level * 10;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + getName() + '\'' +          // from Entity
                ", level=" + getLevel() +              // from Entity
                ", HP=" + HP + "/" + baseHP +
                ", MP=" + MP + "/" + baseMP +
                ", str=" + strength +
                ", dex=" + dexterity +
                ", agi=" + agility +
                ", gold=" + gold +
                ", exp=" + exp +
                '}';
    }
}
