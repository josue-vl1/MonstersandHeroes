package Utility;

import Items.Weapon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading {@link Weapon} objects from a text file.
 *
 * Expected file format (Weaponry.txt):
 *   - first line: header (ignored)
 *   - subsequent lines: one weapon per line, e.g.
 *
 *     Name   Price   Level   Damage   HandsRequired
 *
 * Example:
 *   Sword         500   1   100   1
 *   Greatsword    900   3   200   2
 *
 * The {@code handsRequired} field is used by {@link Items.Weapon} and
 * the weapon-equipping rules in {@code Hero.equipWeapon}.
 */
public final class WeaponLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static-only utility class.
     */
    private WeaponLoader() { }

    /**
     * Load all weapons from the given file.
     *
     * @param file path to the weapon data file (e.g. "src/resources/Weaponry.txt")
     * @return list of {@link Weapon} parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Weapon> loadWeapon(Path file) throws IOException {
        List<Weapon> weapon = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name         = parts[0];
                int price           = Integer.parseInt(parts[1]);
                int level           = Integer.parseInt(parts[2]);
                int damageValue     = Integer.parseInt(parts[3]);
                int handsRequired   = Integer.parseInt(parts[4]);

                Weapon w = new Weapon(name, price, level, damageValue, handsRequired);
                weapon.add(w);
            }
        }

        return weapon;
    }
}
