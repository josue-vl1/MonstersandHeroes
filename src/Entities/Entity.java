package Entities;

/**
 * Base abstract class for all entities in the game.
 * An Entity is anything that has a name and a level, such as
 * a Hero or a Monster. Common fields and behavior that apply
 * to both are defined here.
 */
public abstract class Entity {

    /**
     * The display name of this entity (e.g., hero or monster name).
     */
    protected String name;

    /**
     * The current level of this entity.
     */
    protected int level;

    /**
     * Construct a new Entity with a given name and level.
     *
     * @param name  the name of the entity
     * @param level the starting level of the entity
     */
    public Entity(String name, int level) {
        this.name = name;
        this.level = level;
    }

    /**
     * @return the name of this entity
     */
    public String getName() {
        return name;
    }

    /**
     * @return the current level of this entity
     */
    public int getLevel() {
        return level;
    }
}
