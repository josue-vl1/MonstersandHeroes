# CS611-Assignment < 4 >
## <HEROES & MONSTERS>
---------------------------------------------------------------------------

- Name: Josue Vega
- Email: josuevl@bu.edu
- Student ID: U43284900

## Files
---------------------------------------------------------------------------

### Top-level

- **Main.java**  
  Program entry point. Loads all game data via `AllObjectsLoader.loadAllObjects()`, creates a shared `Scanner`, constructs a `Game` instance, and calls `game.start()` to run the main loop.

---

### Game package

- **Game/Game.java**  
  High-level game controller. Orchestrates:
    - title screen & instructions
    - hero party selection
    - board creation/preview/confirmation
    - main map loop (movement, inventory, markets, random battles).  
      Coordinates domain objects (`Board`, `Party`, `Market`, `Battle`, `GameData`) but does not contain low-level logic like battle mechanics or file parsing.

- **Game/Board.java**  
  Represents the world map as a grid of `Tile`s.  
  Responsibilities:
    - random generation of tiles (`CommonTile`, `MarketTile`, `InaccessibleTile`) using fixed ratios
    - tracking which tiles are markets
    - computing a starting accessible position for the party
    - ASCII rendering of the map where:
        - `H` = hero party (green text)
        - `M` = market tile (yellow text)
        - `X` = inaccessible tile (red text)
        - blank = common tile.

- **Game/Tile.java**  
  Abstract base class for all board tiles.  
  Responsibilities:
    - report accessibility (`isAccessible()`)
    - mark whether it is a market (`isMarket()`, default false)
    - render a 5-character representation used by `Board.print`
    - provide a short description string (e.g., `"market"`, `"common"`, `"inaccessible"`).  
      Also defines shared ANSI color codes for foreground text.

- **Game/CommonTile.java**  
  Concrete `Tile` that is always accessible and not a market. Renders as a blank 5-character cell.

- **Game/MarketTile.java**  
  Concrete `Tile` that is accessible and marked as a market. Renders as `"  M  "` with a yellow `M`.

- **Game/InaccessibleTile.java**  
  Concrete `Tile` that is not accessible. Renders as `"  X  "` with a red `X`.

- **Game/Party.java**  
  Represents the group of heroes traveling together.  
  Responsibilities:
    - holds up to `maxMembers` (3) `Hero` instances
    - tracks party position (`row`, `col`) on the `Board`
    - applies movement commands (`W/A/S/D`), delegating bounds and accessibility checks to `Board`/`Tile`
    - prints messages when attempting to move off-map or into inaccessible tiles.

- **Game/Battle.java**  
  Handles a single battle between the party and a group of monsters.  
  Responsibilities:
    - battle loop: alternating heroes’ turn → monsters’ turn → end-of-round regeneration
    - hero actions:
        - basic attack
        - cast a spell (Fire/Ice/Lightning) with MP cost, dodge chance, and debuffs
        - use potions that buff HP/MP/Strength/Dexterity/Agility (or “All”)
        - change equipment (weapons/armor)
        - inspect detailed hero/monster stats or skip turn
    - monster actions:
        - choose random living hero
        - apply dodge chance based on hero agility
        - compute damage based on monster stats vs. hero armor
    - compute rewards on victory (gold + exp based on monster levels)
    - revive fainted heroes after battle with partial HP/MP
    - handle defeat (game over).

- **Game/Market.java**  
  Represents a single market on the map.  
  Responsibilities:
    - maintains this market’s **stock** of items and per-item quantities
    - is opened when the party steps on a `MarketTile`
    - lets the player choose a hero, then:
        - **Buy** (Weapons, Armor, Potions, Spells) with:
            - level requirements
            - gold checks
            - per-market quantity tracking (stock decreases when bought)
        - **Sell** items from hero inventory:
            - hero gains ½ price in gold
            - market stock for that item increases
    - prints tabular item lists using each item’s `TableDisplayable` metadata (headers/values).

---

### Entities package (Heroes & Monsters)

- **Entities/Entity.java**  
  Abstract base class for all “things with a name and level” in the game.  
  Contains:
    - `name`
    - `level`
    - getters.

- **Entities/Hero.java**  
  Abstract base class for all hero types.  
  Attributes:
    - name, level (from `Entity`)
    - base/current HP & MP
    - Strength, Dexterity, Agility
    - Gold and Experience
    - Equipped `Weapon` (main hand) and optional off-hand `Weapon`
    - Equipped `Armor`
    - `Inventory` of items.  
      Responsibilities:
    - manage HP/MP (damage, regeneration, revive, full restore)
    - manage stats via potions (addHP, addMP, addStrength, addDexterity, addAgility)
    - manage gold (earn/spend)
    - manage experience and level-ups (HP/MP growth + subclass-specific stat growth)
    - compute total weapon damage from main + off-hand
    - compute armor damage reduction
    - **weapon equip logic** enforcing:
        - a 2-handed weapon occupies both hands (off-hand cleared)
        - if already using a 2H weapon and equipping a 1H, the 2H is replaced with the 1H in main hand
        - up to two 1H weapons total (main + off-hand), replacing main-hand if both are already filled.

- **Entities/Paladin.java, Entities/Warrior.java, Entities/Sorcerer.java**  
  Concrete hero classes extending `Hero`.  
  Each uses an inner `Builder` to construct instances with:
    - base HP, base MP
    - Strength, Dexterity, Agility
    - starting Gold, Exp.  
      Each overrides `applyLevelUpStatGrowth()` with different favored stats:
    - **Paladin**: favors Strength & Dexterity (10%), normal Agility (5%)
    - **Warrior**: favors Strength & Agility (10%), normal Dexterity (5%)
    - **Sorcerer**: favors Dexterity & Agility (10%), normal Strength (5%).

- **Entities/Monster.java**  
  Abstract base class for all monsters.  
  Attributes:
    - name, level (from `Entity`)
    - base/current HP
    - base damage
    - defense
    - dodge chance
    - “original” copies of damage/defense/dodge so debuffs can be reset.  
      Responsibilities:
    - track whether the monster is dead
    - take damage with floor at 0
    - `resetForBattle()` to restore HP and reset stats
    - debuff methods that reduce defense, damage, or dodge by a percentage with clamping at 0.

- **Entities/Dragon.java, Entities/Exoskeleton.java, Entities/Spirit.java**  
  Concrete monster classes extending `Monster`.  
  Each has an inner `Builder` taking:
    - name, level
    - baseHP (computed as `level * 100` in loader)
    - baseDamage, defense, dodge.  
      Used by `MonsterLoader` and `Game.cloneMonster()` to create fresh instances for battles.

---

### Items package

- **Items/Item.java**  
  Abstract base class for all items, implements `TableDisplayable`.  
  Attributes:
    - `name`
    - `price`
    - `level` requirement.  
      Provides default table headers and values: `Name`, `Price`, `Level`.

- **Items/TableDisplayable.java**  
  Interface defining:
    - `getColumnHeaders()` – column names for tables
    - `getColumnValues()` – row values aligned with those headers.  
      Used by `Market` and inventory printing to render items in generic tables.

- **Items/Weapon.java**  
  Subclass of `Item`.  
  Attributes:
    - `damageValue`
    - `handsRequired` (1 or 2).  
      Table columns: `Name`, `Price`, `Level`, `Damage`, `Hands Req.`  
      Weapon hands are interpreted by `Hero.equipWeapon` and displayed by `describeEquippedWeapons`.

- **Items/Armor.java**  
  Subclass of `Item`.  
  Attribute:
    - `damageReduction`.  
      Table columns: `Name`, `Price`, `Level`, `Defense`.  
      Armor reduction is used when computing monster → hero damage in `Battle`.

- **Items/Spell.java** (abstract)  
  Subclass of `Item`.  
  Attributes:
    - `damage`
    - `manaCost`.  
      Provides:
    - `getDamage()`, `getManaCost()`
    - abstract `getElement()` implemented by subclasses.  
      Table columns: `Name`, `Price`, `Level`, `Damage`, `Mana`, `Element`.  
      Spell damage is combined with hero Dexterity in `Battle.computeSpellDamage`.

- **Items/FireSpell.java, Items/IceSpell.java, Items/LightningSpell.java**  
  Concrete spell types with hard-coded elements `"Fire"`, `"Ice"`, and `"Lightning"`.  
  In `Battle`, after dealing damage:
    - **FireSpell**: reduces target defense
    - **IceSpell**: reduces target damage
    - **LightningSpell**: reduces target dodge.

- **Items/Potion.java**  
  Subclass of `Item`.  
  Attributes:
    - `effectIncrease`
    - `attributeAffected` (e.g., `"Health"`, `"Mana"`, `"Strength"`, `"Dexterity"`, `"Agility"`, `"All"`).  
      Battle logic interprets this string (case-insensitively, with `"all"` as a special case to buff everything).

- **Items/Inventory.java**  
  Simple container that belongs to each `Hero`.  
  Responsibilities:
    - store arbitrary `Item`s
    - provide unmodifiable view of all items
    - convenience getters for subsets (weapons, armors, spells, potions).


---

### Utility package (loading data / aggregating game content)

- **Utility/GameData.java**  
  Simple container for all loaded game content:
    - `List<Hero> heroes`
    - `List<Monster> monsters`
    - `List<Armor> armors`
    - `List<Weapon> weapons`
    - `List<Potion> potions`
    - `List<Spell> spells`.  
      Passed into `Game` so it can populate hero selection, markets, and monster encounters.

- **Utility/AllObjectsLoader.java**  
  Single entry point for reading all resource files and constructing `GameData`.  
  Responsibilities:
    - calls:
        - `HeroLoader.loadPaladins/Warriors/Sorcerers`
        - `MonsterLoader.loadDragons/Exoskeletons/Spirits`
        - `ArmorLoader.loadArmor`
        - `WeaponLoader.loadWeapon`
        - `PotionLoader.loadPotion`
        - `SpellLoader.loadAllSpells`
    - handles `IOException` and returns a fully-populated `GameData` object.

- **Utility/HeroLoader.java**  
  Loads Paladins, Warriors, Sorcerers from text files (e.g., `Paladins.txt`, `Warriors.txt`, `Sorcerers.txt`).  
  Expected format:
    - header line, then:
      `Name  Mana  Strength  Agility  Dexterity  Money  Exp`  
      Uses the appropriate `Builder` to construct each hero with:
    - `level = 1`
    - `baseHP = level * 100`.

- **Utility/MonsterLoader.java**  
  Loads Dragons, Exoskeletons, Spirits from text files (e.g., `Dragons.txt`, `Exoskeletons.txt`, `Spirits.txt`).  
  Expected format:
    - header line, then:
      `Name  Level  Damage  Defense  Dodge`  
      Also sets `baseHP = level * 100` for each monster and uses the monster-specific `Builder`.

- **Utility/ArmorLoader.java**  
  Loads armor items from `Armory.txt`.  
  Expected format:
    - header, then:
      `Name  Price  Level  DamageReduction`.

- **Utility/WeaponLoader.java**  
  Loads weapons from `Weaponry.txt`.  
  Expected format:
    - header, then:
      `Name  Price  Level  Damage  HandsRequired`.

- **Utility/PotionLoader.java**  
  Loads potions from `Potions.txt`.  
  Expected format:
    - header, then:
      `Name  Price  Level  EffectAmount  AttributeAffected`.

- **Utility/SpellLoader.java**  
  Loads spells from `FireSpells.txt`, `IceSpells.txt`, `LightningSpells.txt`.  
  Each file format:
    - header, then:
      `Name  Price  Level  Damage  ManaCost`.  
      Provides per-element loaders and a `loadAllSpells` convenience method that returns a combined `List<Spell>`.

---

### Resource files (in src/resources)

- **Paladins.txt, Warriors.txt, Sorcerers.txt** – hero definitions.
- **Dragons.txt, Exoskeletons.txt, Spirits.txt** – monster definitions.
- **Armory.txt** – armor definitions.
- **Weaponry.txt** – weapon definitions.
- **Potions.txt** – potion definitions.
- **FireSpells.txt, IceSpells.txt, LightningSpells.txt** – spell definitions by element.



## Notes
---------------------------------------------------------------------------

1. **Layered design & separation of concerns**
    - `Game` coordinates high-level flow (title menu, map loop, encounters).
    - `Board` and `Tile` handle map structure and rendering.
    - `Party` handles movement and grouping of heroes.
    - `Battle` and `Market` encapsulate combat and trading logic.
    - `Utility` classes load data and provide a clean `GameData` object.

2. **Object-oriented modeling of heroes & monsters**
    - `Entity` → `Hero` / `Monster` forms a clear inheritance hierarchy.
    - Concrete hero types (`Paladin`, `Warrior`, `Sorcerer`) and monster types (`Dragon`, `Exoskeleton`, `Spirit`) capture distinct behaviors (especially level-up growth for heroes).

3. **Builder pattern for complex entities**
    - Heroes and monsters are constructed via inner `Builder` classes, which:
        - improve readability when setting multiple stats
        - align nicely with file-based loading where each line becomes a builder chain  
          Example: `new Paladin.Builder(name, level).baseHP(...).baseMP(...).strength(...).build();`

4. **Resource-driven design**
    - All heroes, monsters, and items are defined in text files under `src/resources/`.
    - Changing game content (adding heroes, monsters, or gear) only requires editing these files, not the code.

5. **Random map generation with player control**
    - `Board` randomly assigns tiles based on:
        - `inaccesibleRate` (e.g., 20% of tiles become `InaccessibleTile`)
        - a fixed number of `MarketTile`s (e.g., 5 markets).
    - The game shows a **preview** of the generated map, and the player can accept or reroll until satisfied.

6. **Party and hero selection flow**
    - The player chooses a party size (1–3 heroes).
    - Heroes are grouped by class (Paladins, Warriors, Sorcerers) and shown in a table.
    - The user can confirm or reselect the entire party, with detailed stat previews before finalizing.

7. **Inventory & equipment system**
    - Each hero has an `Inventory` that stores arbitrary `Item`s.
    - Items implement `TableDisplayable`, enabling generic table printing (headers + values) in markets and inventory views.
    - Equipment is shown via `describeEquippedWeapons` and armor name displays.

8. **Weapon-hands rule enforcement**
    - `Hero.equipWeapon` enforces:
        - 2-handed weapons occupy both hands and clear the off-hand slot.
        - If a hero has a 2H weapon and equips a 1H, the 2H is replaced by the 1H in main hand.
        - A hero can dual-wield two 1H weapons (main + off-hand), and equipping an additional 1H replaces the main-hand.

9. **Markets with per-item quantities and level requirements**
    - Each `Market` receives a subset of global items and per-item stock counts:
        - weapons/armor: ~1 per party member
        - potions/spells: ~10 per party member.
    - Buying:
        - checks hero level vs item level
        - checks available quantity and gold
        - decreases quantity and adds the item to the hero’s inventory.
    - Selling:
        - removes from inventory, gives half price in gold
        - increases market quantity (or adds the item to stock if new).

10. **Battle system with stats and debuffs**
    - Physical damage: uses hero Strength + weapon damage vs monster defense.
    - Spells: use spell damage scaled by hero Dexterity, plus:
        - Fire → reduces monster defense
        - Ice → reduces monster damage
        - Lightning → reduces monster dodge.
    - Monsters attack heroes with armor-based mitigation and hero Agility–based dodge chance.
    - End-of-round regeneration restores a portion of HP/MP for living heroes.

11. **Experience, leveling, and stat growth**
    - After victories, living heroes gain gold and experience based on monster levels.
    - Level-ups are triggered when accumulated exp crosses a formula (`level * 10`), at which point:
        - HP & MP increase (e.g., +10% each)
        - hero-level-specific stat growth is applied via `applyLevelUpStatGrowth`
        - current HP/MP are reset to full.

12. **Fainting and revival**
    - Heroes whose HP drop to 0 are marked as fainted and skip actions in battle.
    - If the party wins, fainted heroes are revived with partial HP/MP and do not receive rewards for that fight.
    - There is also a method to reset HP/MP to full between battles if desired.

13. **Random encounters scaled to party level**
    - `Game` computes a reference party level (average of hero levels).
    - Monsters for a new encounter are chosen with levels close to this reference, ensuring balanced fights.
    - For each hero in the party, a corresponding monster is spawned (with fresh stats via `cloneMonster`).

14. **Unified console input handling**
    - A single `Scanner` is created in `Main` and passed down into `Game`, `Battle`, and `Market`.
    - All menus use helper methods like `readIntInRange` to:
        - validate numeric input
        - handle out-of-range and non-numeric values gracefully.

15. **Extensibility**
    - New hero or monster types can be added by:
        - creating a new class extending `Hero` or `Monster`
        - adding a loader or adjusting existing loaders
        - updating text resources.
    - New item types can extend `Item` and implement `TableDisplayable` to automatically work with markets and inventory views.
    - The map, battle system, and markets are modular, so future features (e.g., new tiles, status effects, or equipment slots) can be added without changing the core game loop structure.

## How to compile and run
---------------------------------------------------------------------------

1. Navigate to the directory where my GameModule assignment has been uploaded
2. Run the following instructions:
   javac *.java
   java Main

    

## Input/Output Example
---------------------------------------------------------------------------
```
    __  _______  _   _______________________  _____    
   /  |/  / __ \/ | / / ___/_  __/ ____/ __ \/ ___/    
  / /|_/ / / / /  |/ /\__ \\ / / / __/ / /_/ /\__ \     
 / /  / / /_/ / /|  /___/ // / / /___/ _, _/___/ /     
/_/  /_/\____/_/ |_//____//_/ /_____/_/ |_|/____/      
                    ( _ )                              
                   / __ \/|                            
                  / /_/  <                             
       __  _______\\/\\___  ___________             
      / / / / ____/ __ \/ __ \/ ____/ ___/             
     / /_/ / __/ / /_/ / / / / __/  \\__ \              
    / __  / /___/ _, _/ /_/ / /___ ___/ /              
   /_/ /_/_____/_/ |_|\____/_____//____/              

1) Play
2) Instructions
3) Quit

Select an option (1-3): 2

=== Instructions ===
 - Use W/A/S/D to move around the map.
 - I: open party inventory.
 - Markets (M) let you buy/sell and equip items.
 - Battles may start randomly on common tiles.
 - In battle: attack, cast spells, use potions, change equipment.
Press Enter to return to the title screen...


    __  _______  _   _______________________  _____    
   /  |/  / __ \/ | / / ___/_  __/ ____/ __ \/ ___/    
  / /|_/ / / / /  |/ /\__ \\ / / / __/ / /_/ /\__ \     
 / /  / / /_/ / /|  /___/ // / / /___/ _, _/___/ /     
/_/  /_/\____/_/ |_//____//_/ /_____/_/ |_|/____/      
                    ( _ )                              
                   / __ \/|                            
                  / /_/  <                             
       __  _______\\/\\___  ___________             
      / / / / ____/ __ \/ __ \/ ____/ ___/             
     / /_/ / __/ / /_/ / / / / __/  \\__ \              
    / __  / /___/ _, _/ /_/ / /___ ___/ /              
   /_/ /_/_____/_/ |_|\____/_____//____/              

1) Play
2) Instructions
3) Quit

Select an option (1-3): 1

Preview of generated map:

+-----+-----+-----+-----+-----+-----+-----+-----+
|  H  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Do you want to use this map? (Y to accept, anything else to reroll): y

=== Hero Selection ===
You may choose between 1 and 3 heroes.
How many heroes do you want in your party? (1-3): 2

Available hero classes:
1) Paladins (6 available)
2) Warriors (6 available)
3) Sorcerers (6 available)
Select a hero class for hero #1: 1

=== Paladins ===
Choose hero #1 from the list below.

#   Name                 Lvl   HP          MP          Str    Dex    Agi    Gold    Exp    
1   Parzival             1     100/100     300/300     750    700    650    2500    7      
2   Sehanine_Moonbow     1     100/100     300/300     750    700    700    2500    7      
3   Skoraeus_Stonebones  1     100/100     250/250     650    350    600    2500    4      
4   Garl_Glittergold     1     100/100     100/100     600    400    500    2500    5      
5   Amaryllis_Astra      1     100/100     500/500     500    500    500    2500    5      
6   Caliber_Heist        1     100/100     400/400     400    400    400    2500    8      

Enter the number of the hero to select, or 0 to go back to hero classes.
Your choice for hero #1: 1

Available hero classes:
1) Paladins (5 available)
2) Warriors (6 available)
3) Sorcerers (6 available)
Select a hero class for hero #2: 2

=== Warriors ===
Choose hero #2 from the list below.

#   Name                 Lvl   HP          MP          Str    Dex    Agi    Gold    Exp    
1   Gaerdal_Ironhand     1     100/100     100/100     700    600    500    1354    7      
2   Sehanine_Monnbow     1     100/100     600/600     700    500    800    2500    8      
3   Muamman_Duathall     1     100/100     300/300     900    750    500    2546    6      
4   Flandal_Steelskin    1     100/100     200/200     750    700    650    2500    7      
5   Undefeated_Yoj       1     100/100     400/400     800    700    400    2500    7      
6   Eunoia_Cyn           1     100/100     400/400     700    600    800    2500    6      

Enter the number of the hero to select, or 0 to go back to hero classes.
Your choice for hero #2: 2

You selected:
#   Name                 Class      Lvl   HP          MP          Str    Dex    Agi    Gold    Exp    
1   Parzival             Paladin    1     100/100     300/300     750    700    650    2500    7      
2   Sehanine_Monnbow     Warrior    1     100/100     600/600     700    500    800    2500    8      
Confirm this party? (Y to confirm, anything else to reselect): y

Party created with 2 hero(es).
Starting game...
Use W/A/S/D to move, I for inventory, Q to quit.


+-----+-----+-----+-----+-----+-----+-----+-----+
|  H  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: d
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  H  |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: d
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  H  |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a market tile.
Do you want to enter the market? (Y/N): y

=== Market ===
Choose a hero to trade for, or 0 to leave the market.

1) Parzival (Lvl 1, Gold 2500)
2) Sehanine_Monnbow (Lvl 1, Gold 2500)
Your choice: 1

=== Market: Parzival ===
Level: 1 | Gold: 2500
1) Buy items
2) Sell items
0) Back to hero selection
Choose an option: 1

=== Buy Menu for Parzival ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 1

=== Weapons for sale ===
Gold: 2500
#   Name            Price           Level           Damage          Hands Req.      Qty  
1   Scythe          1000            6               1100            2               2    
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 2

=== Armor for sale ===
Gold: 2500
#   Name            Price           Level           Defense         Qty  
1   Breastplate     350             3               600             2    
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 3

=== Potions for sale ===
Gold: 2500
#   Name            Price           Level           Effect          Affects         Qty  
1   Ambrosia        1000            8               150             All             20   
2   Luck_Elixir     500             4               65              Agility         20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 4

=== Spells for sale ===
Gold: 2500
#   Name            Price           Level           Damage          Mana            Element         Qty  
1   Hell_Storm      600             3               950             600             Fire            20   
2   Electric_Arrows 550             5               650             200             Lightning       20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 0

=== Market: Parzival ===
Level: 1 | Gold: 2500
1) Buy items
2) Sell items
0) Back to hero selection
Choose an option: 0

=== Market ===
Choose a hero to trade for, or 0 to leave the market.

1) Parzival (Lvl 1, Gold 2500)
2) Sehanine_Monnbow (Lvl 1, Gold 2500)
Your choice: 0

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  H  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: d
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |  H  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: d
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |  H  |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: d
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |  H  |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |  H  |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a market tile.
Do you want to enter the market? (Y/N): y

=== Market ===
Choose a hero to trade for, or 0 to leave the market.

1) Parzival (Lvl 1, Gold 2500)
2) Sehanine_Monnbow (Lvl 1, Gold 2500)
Your choice: 1

=== Market: Parzival ===
Level: 1 | Gold: 2500
1) Buy items
2) Sell items
0) Back to hero selection
Choose an option: 1

=== Buy Menu for Parzival ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 1

=== Weapons for sale ===
Gold: 2500
#   Name            Price           Level           Damage          Hands Req.      Qty  
1   Sword           500             1               800             1               2    
Enter the number of the item to buy, or 0 to go back.
Your choice: 1
Parzival bought Sword for 500 gold.

=== Weapons for sale ===
Gold: 2000
#   Name            Price           Level           Damage          Hands Req.      Qty  
1   Sword           500             1               800             1               1    
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 2000
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 2

=== Armor for sale ===
Gold: 2000
#   Name            Price           Level           Defense         Qty  
1   Guardian_Angel  1000            10              1000            2    
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 2000
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 3

=== Potions for sale ===
Gold: 2000
#   Name            Price           Level           Effect          Affects         Qty  
1   Mermaid_Tears   850             5               100             Health/Mana/Strength/Agility 20   
2   Strength_Potion 200             1               75              Strength        20   
3   Magic_Potion    350             2               100             Mana            20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 2
Parzival bought Strength_Potion for 200 gold.

=== Potions for sale ===
Gold: 1800
#   Name            Price           Level           Effect          Affects         Qty  
1   Mermaid_Tears   850             5               100             Health/Mana/Strength/Agility 20   
2   Strength_Potion 200             1               75              Strength        19   
3   Magic_Potion    350             2               100             Mana            20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 2
Parzival bought Strength_Potion for 200 gold.

=== Potions for sale ===
Gold: 1600
#   Name            Price           Level           Effect          Affects         Qty  
1   Mermaid_Tears   850             5               100             Health/Mana/Strength/Agility 20   
2   Strength_Potion 200             1               75              Strength        18   
3   Magic_Potion    350             2               100             Mana            20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Parzival ===
Gold: 1600
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 0

=== Market: Parzival ===
Level: 1 | Gold: 1600
1) Buy items
2) Sell items
0) Back to hero selection
Choose an option: 0

=== Market ===
Choose a hero to trade for, or 0 to leave the market.

1) Parzival (Lvl 1, Gold 1600)
2) Sehanine_Monnbow (Lvl 1, Gold 2500)
Your choice: 2

=== Market: Sehanine_Monnbow ===
Level: 1 | Gold: 2500
1) Buy items
2) Sell items
0) Back to hero selection
Choose an option: 1

=== Buy Menu for Sehanine_Monnbow ===
Gold: 2500
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 1

=== Weapons for sale ===
Gold: 2500
#   Name            Price           Level           Damage          Hands Req.      Qty  
1   Sword           500             1               800             1               1    
Enter the number of the item to buy, or 0 to go back.
Your choice: 1
Sehanine_Monnbow bought Sword for 500 gold.
No more weapons in this market.

=== Buy Menu for Sehanine_Monnbow ===
Gold: 2000
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 3

=== Potions for sale ===
Gold: 2000
#   Name            Price           Level           Effect          Affects         Qty  
1   Mermaid_Tears   850             5               100             Health/Mana/Strength/Agility 20   
2   Strength_Potion 200             1               75              Strength        18   
3   Magic_Potion    350             2               100             Mana            20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 2
Sehanine_Monnbow bought Strength_Potion for 200 gold.

=== Potions for sale ===
Gold: 1800
#   Name            Price           Level           Effect          Affects         Qty  
1   Mermaid_Tears   850             5               100             Health/Mana/Strength/Agility 20   
2   Strength_Potion 200             1               75              Strength        17   
3   Magic_Potion    350             2               100             Mana            20   
Enter the number of the item to buy, or 0 to go back.
Your choice: 0

=== Buy Menu for Sehanine_Monnbow ===
Gold: 1800
1) Weapons
2) Armor
3) Potions
4) Spells
0) Back
Choose a category: 0

=== Market: Sehanine_Monnbow ===
Level: 1 | Gold: 1800
1) Buy items
2) Sell items
0) Back to hero selection
Choose an option: 0

=== Market ===
Choose a hero to trade for, or 0 to leave the market.

1) Parzival (Lvl 1, Gold 1600)
2) Sehanine_Monnbow (Lvl 1, Gold 1800)
Your choice: 0

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  H  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: i

=== Party Inventory ===
1) Parzival (Lvl 1, HP 100/100, MP 300/300)
2) Sehanine_Monnbow (Lvl 1, HP 100/100, MP 600/600)
0) Back to map
Whose inventory do you want to open? 1

=== Parzival Inventory ===
Equipped weapon(s): none
Equipped armor : none

1) Change equipment
2) Show all items
0) Back
Your choice: 1

=== Change equipment for Parzival ===
Current weapon(s): none
Current armor : none
1) Change weapon
2) Change armor
0) Back
Your choice: 1

Weapons in inventory:
#   Name                 Lvl   Damage    
1   Sword                1     800       
Enter the number of the weapon to equip, or 0 to cancel.
Your choice: 1
Parzival now wields: Sword (1H)

=== Change equipment for Parzival ===
Current weapon(s): Sword (1H)
Current armor : none
1) Change weapon
2) Change armor
0) Back
Your choice: 0

=== Parzival Inventory ===
Equipped weapon(s): Sword (1H)
Equipped armor : none

1) Change equipment
2) Show all items
0) Back
Your choice: 0

=== Party Inventory ===
1) Parzival (Lvl 1, HP 100/100, MP 300/300)
2) Sehanine_Monnbow (Lvl 1, HP 100/100, MP 600/600)
0) Back to map
Whose inventory do you want to open? 2

=== Sehanine_Monnbow Inventory ===
Equipped weapon(s): none
Equipped armor : none

1) Change equipment
2) Show all items
0) Back
Your choice: 1

=== Change equipment for Sehanine_Monnbow ===
Current weapon(s): none
Current armor : none
1) Change weapon
2) Change armor
0) Back
Your choice: 1

Weapons in inventory:
#   Name                 Lvl   Damage    
1   Sword                1     800       
Enter the number of the weapon to equip, or 0 to cancel.
Your choice: 1
Sehanine_Monnbow now wields: Sword (1H)

=== Change equipment for Sehanine_Monnbow ===
Current weapon(s): Sword (1H)
Current armor : none
1) Change weapon
2) Change armor
0) Back
Your choice: 0

=== Sehanine_Monnbow Inventory ===
Equipped weapon(s): Sword (1H)
Equipped armor : none

1) Change equipment
2) Show all items
0) Back
Your choice: 
Please enter a valid integer.
Your choice: 0

=== Party Inventory ===
1) Parzival (Lvl 1, HP 100/100, MP 300/300)
2) Sehanine_Monnbow (Lvl 1, HP 100/100, MP 600/600)
0) Back to map
Whose inventory do you want to open? 0

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  H  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |  H  |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: a
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  H  |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |  H  |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: d
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |  H  |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |  H  |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: a
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |  H  |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: w
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |  H  |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: w
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  H  |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: a
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  H  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: a
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  H  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: w
You moved onto a common tile.
You feel a dark presence...

=== A battle begins! ===

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       100/100   
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       200/200   
2   WickedWitch     2       200/200   

--- Heroes' turn ---

Parzival's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 200/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 1
Andrealphus dodged the attack!

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 200/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
Sehanine_Monnbow attacks WickedWitch for 35 damage.

--- Monsters' turn ---
Andrealphus attacks Parzival for 30 damage.
Parzival dodged the attack from WickedWitch!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       77/100    
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       200/200   
2   WickedWitch     2       165/200   

--- Heroes' turn ---

Parzival's turn (HP: 77/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 165/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 1
Andrealphus dodged the attack!

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 2
No spells in inventory.

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 165/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
WickedWitch dodged the attack!

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andrealphus!
Sehanine_Monnbow dodged the attack from WickedWitch!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       85/100    
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       200/200   
2   WickedWitch     2       165/200   

--- Heroes' turn ---

Parzival's turn (HP: 85/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 165/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
Parzival attacks WickedWitch for 36 damage.

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 129/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
Sehanine_Monnbow attacks WickedWitch for 35 damage.

--- Monsters' turn ---
Parzival dodged the attack from Andrealphus!
WickedWitch attacks Parzival for 13 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       79/100    
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       200/200   
2   WickedWitch     2       94/200    

--- Heroes' turn ---

Parzival's turn (HP: 79/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 94/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
Parzival attacks WickedWitch for 36 damage.

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 58/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
Sehanine_Monnbow attacks WickedWitch for 35 damage.

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andrealphus!
Sehanine_Monnbow dodged the attack from WickedWitch!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       87/100    
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       200/200   
2   WickedWitch     2       23/200    

--- Heroes' turn ---

Parzival's turn (HP: 87/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
2) WickedWitch (HP: 23/200, dmg: 250, def: 350, dodge: 25%)
Target (0 to cancel): 2
Parzival attacks WickedWitch for 36 damage.
WickedWitch has been defeated!

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 200/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 2
Please enter a number between 0 and 1.
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andrealphus for 28 damage.

--- Monsters' turn ---
Andrealphus attacks Sehanine_Monnbow for 30 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       96/100    
2   Sehanine_Monnbow 1       77/100    

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       172/200   

--- Heroes' turn ---

Parzival's turn (HP: 96/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 172/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Parzival attacks Andrealphus for 29 damage.

Sehanine_Monnbow's turn (HP: 77/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 143/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andrealphus for 28 damage.

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andrealphus!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       100/100   
2   Sehanine_Monnbow 1       85/100    

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       115/200   

--- Heroes' turn ---

Parzival's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 115/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Parzival attacks Andrealphus for 29 damage.

Sehanine_Monnbow's turn (HP: 85/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 86/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andrealphus for 28 damage.

--- Monsters' turn ---
Parzival dodged the attack from Andrealphus!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       100/100   
2   Sehanine_Monnbow 1       94/100    

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       58/200    

--- Heroes' turn ---

Parzival's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 58/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Parzival attacks Andrealphus for 29 damage.

Sehanine_Monnbow's turn (HP: 94/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 29/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Andrealphus dodged the attack!

--- Monsters' turn ---
Andrealphus attacks Parzival for 30 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       77/100    
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       29/200    

--- Heroes' turn ---

Parzival's turn (HP: 77/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 29/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Andrealphus dodged the attack!

Sehanine_Monnbow's turn (HP: 100/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 29/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andrealphus for 28 damage.

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andrealphus!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        1       85/100    
2   Sehanine_Monnbow 1       100/100   

Monsters:
#   Name            Level   HP        
1   Andrealphus     2       1/200     

--- Heroes' turn ---

Parzival's turn (HP: 85/100)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andrealphus (HP: 1/200, dmg: 600, def: 500, dodge: 40%)
Target (0 to cancel): 1
Parzival attacks Andrealphus for 29 damage.
Andrealphus has been defeated!

=== Heroes win the battle! ===
Parzival leveled up to level 2!
Parzival gains 200 gold and 4 exp.
Sehanine_Monnbow leveled up to level 2!
Sehanine_Monnbow gains 200 gold and 4 exp.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  H  |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a common tile.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  H  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: s
You moved onto a common tile.
You feel a dark presence...

=== A battle begins! ===

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       110/110   
2   Sehanine_Monnbow 2       110/110   

Monsters:
#   Name            Level   HP        
1   Andromalius     3       300/300   
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 110/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 300/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Andromalius dodged the attack!

Sehanine_Monnbow's turn (HP: 110/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 300/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Andromalius dodged the attack!

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andromalius!
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       101/110   
2   Sehanine_Monnbow 2       110/110   

Monsters:
#   Name            Level   HP        
1   Andromalius     3       300/300   
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 101/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 300/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Andromalius for 33 damage.

Sehanine_Monnbow's turn (HP: 110/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 11
Please enter a number between 0 and 6.
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 267/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andromalius for 31 damage.

--- Monsters' turn ---
Andromalius attacks Sehanine_Monnbow for 28 damage.
Brandobaris attacks Sehanine_Monnbow for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       110/110   
2   Sehanine_Monnbow 2       70/110    

Monsters:
#   Name            Level   HP        
1   Andromalius     3       236/300   
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 110/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 236/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Andromalius dodged the attack!

Sehanine_Monnbow's turn (HP: 70/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 236/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Andromalius dodged the attack!

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andromalius!
Sehanine_Monnbow dodged the attack from Brandobaris!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       110/110   
2   Sehanine_Monnbow 2       77/110    

Monsters:
#   Name            Level   HP        
1   Andromalius     3       236/300   
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 110/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 236/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Andromalius for 33 damage.

Sehanine_Monnbow's turn (HP: 77/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 203/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andromalius for 31 damage.

--- Monsters' turn ---
Andromalius attacks Parzival for 28 damage.
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       70/110    
2   Sehanine_Monnbow 2       85/110    

Monsters:
#   Name            Level   HP        
1   Andromalius     3       172/300   
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 70/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 172/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Andromalius dodged the attack!

Sehanine_Monnbow's turn (HP: 85/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 172/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andromalius for 31 damage.

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Andromalius!
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       57/110    
2   Sehanine_Monnbow 2       94/110    

Monsters:
#   Name            Level   HP        
1   Andromalius     3       141/300   
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 57/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 141/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Andromalius for 33 damage.

Sehanine_Monnbow's turn (HP: 94/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 108/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andromalius for 31 damage.

--- Monsters' turn ---
Parzival dodged the attack from Andromalius!
Brandobaris attacks Sehanine_Monnbow for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       63/110    
2   Sehanine_Monnbow 2       84/110    

Monsters:
#   Name            Level   HP        
1   Andromalius     3       77/300    
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 63/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 77/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Andromalius for 33 damage.

Sehanine_Monnbow's turn (HP: 84/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 44/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andromalius for 31 damage.

--- Monsters' turn ---
Andromalius attacks Sehanine_Monnbow for 28 damage.
Brandobaris attacks Sehanine_Monnbow for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       69/110    
2   Sehanine_Monnbow 2       42/110    

Monsters:
#   Name            Level   HP        
1   Andromalius     3       13/300    
2   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 69/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 13/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Andromalius dodged the attack!

Sehanine_Monnbow's turn (HP: 42/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Andromalius (HP: 13/300, dmg: 550, def: 450, dodge: 25%)
2) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Andromalius for 31 damage.
Andromalius has been defeated!

--- Monsters' turn ---
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       56/110    
2   Sehanine_Monnbow 2       46/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       300/300   

--- Heroes' turn ---

Parzival's turn (HP: 56/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 300/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Brandobaris for 33 damage.

Sehanine_Monnbow's turn (HP: 46/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 267/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

--- Monsters' turn ---
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       42/110    
2   Sehanine_Monnbow 2       51/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       267/300   

--- Heroes' turn ---

Parzival's turn (HP: 42/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 267/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Brandobaris for 33 damage.

Sehanine_Monnbow's turn (HP: 51/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 234/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Brandobaris!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       46/110    
2   Sehanine_Monnbow 2       56/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       234/300   

--- Heroes' turn ---

Parzival's turn (HP: 46/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 234/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

Sehanine_Monnbow's turn (HP: 56/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 234/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Brandobaris for 31 damage.

--- Monsters' turn ---
Parzival dodged the attack from Brandobaris!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       51/110    
2   Sehanine_Monnbow 2       62/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       203/300   

--- Heroes' turn ---

Parzival's turn (HP: 51/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 203/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Brandobaris for 33 damage.

Sehanine_Monnbow's turn (HP: 62/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 170/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Brandobaris!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       56/110    
2   Sehanine_Monnbow 2       68/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       170/300   

--- Heroes' turn ---

Parzival's turn (HP: 56/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 170/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Brandobaris for 33 damage.

Sehanine_Monnbow's turn (HP: 68/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 137/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

--- Monsters' turn ---
Brandobaris attacks Sehanine_Monnbow for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       62/110    
2   Sehanine_Monnbow 2       55/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       137/300   

--- Heroes' turn ---

Parzival's turn (HP: 62/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 
Please enter a valid integer.
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 137/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

Sehanine_Monnbow's turn (HP: 55/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 137/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Brandobaris for 31 damage.

--- Monsters' turn ---
Brandobaris attacks Sehanine_Monnbow for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       68/110    
2   Sehanine_Monnbow 2       41/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       106/300   

--- Heroes' turn ---

Parzival's turn (HP: 68/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 106/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Brandobaris for 33 damage.

Sehanine_Monnbow's turn (HP: 41/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 73/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Brandobaris for 31 damage.

--- Monsters' turn ---
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       55/110    
2   Sehanine_Monnbow 2       45/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       42/300    

--- Heroes' turn ---

Parzival's turn (HP: 55/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 3

=== Potions in inventory ===
#   Name                 Lvl   Amount   Affects     
1   Strength_Potion      1     75       Strength    
2   Strength_Potion      1     75       Strength    
Enter the number of the potion to use, or 0 to cancel.
Your choice: 1
Parzival's Strength increased by 75 (now 900).

Sehanine_Monnbow's turn (HP: 45/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 3

=== Potions in inventory ===
#   Name                 Lvl   Amount   Affects     
1   Strength_Potion      1     75       Strength    
Enter the number of the potion to use, or 0 to cancel.
Your choice: 1
Sehanine_Monnbow's Strength increased by 75 (now 845).

--- Monsters' turn ---
Sehanine_Monnbow dodged the attack from Brandobaris!

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       61/110    
2   Sehanine_Monnbow 2       50/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       42/300    

--- Heroes' turn ---

Parzival's turn (HP: 61/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 42/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

Sehanine_Monnbow's turn (HP: 50/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 42/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Brandobaris dodged the attack!

--- Monsters' turn ---
Brandobaris attacks Parzival for 18 damage.

End of round: heroes regain some HP/MP.

--- Battle status ---
Heroes:
#   Name            Level   HP        
1   Parzival        2       47/110    
2   Sehanine_Monnbow 2       55/110    

Monsters:
#   Name            Level   HP        
1   Brandobaris     3       42/300    

--- Heroes' turn ---

Parzival's turn (HP: 47/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 42/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Parzival attacks Brandobaris for 34 damage.

Sehanine_Monnbow's turn (HP: 55/110)
Choose action:
1) Attack
2) Cast spell
3) Use potion
4) Change equipment
5) Show heroes' stats
6) Show monsters' stats
0) Skip action
Your choice: 1

Choose a monster to attack:
1) Brandobaris (HP: 8/300, dmg: 350, def: 450, dodge: 30%)
Target (0 to cancel): 1
Sehanine_Monnbow attacks Brandobaris for 33 damage.
Brandobaris has been defeated!

=== Heroes win the battle! ===
Parzival gains 300 gold and 6 exp.
Sehanine_Monnbow gains 300 gold and 6 exp.

+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |  M  |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |  X  |  M  |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |     |     |     |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  X  |  H  |  X  |     |     |  X  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|     |  M  |     |     |     |     |  M  |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
|  M  |     |     |     |     |     |     |     |
+-----+-----+-----+-----+-----+-----+-----+-----+
Legend:
  H  : Hero party
  M  : Market
  X  : Inaccessible
       (blank) : Common

Move (W/A/S/D), I = Inventory, or Q to quit: q
Goodbye!
```