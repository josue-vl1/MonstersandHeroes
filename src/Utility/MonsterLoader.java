package Utility;

import Entities.Dragon;
import Entities.Exoskeleton;
import Entities.Spirit;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading monster data (Dragons, Exoskeletons, Spirits)
 * from text files.
 *
 * Expected file format for each monster file:
 *   - first line: header (ignored)
 *   - subsequent lines: one monster per line, e.g.
 *
 *     Name   Level   Damage   Defense   DodgeChance
 *
 * HP is not stored directly in the file; instead we follow the convention:
 *   baseHP = level * 100
 *
 * This class uses each monster type's Builder to construct
 * the corresponding {@link Entities.Monster} objects.
 */
public final class MonsterLoader {

    /**
     * Private constructor to prevent instantiation.
     * This is a static-only utility class.
     */
    private MonsterLoader() { }

    /**
     * Load all Dragons from the given file.
     *
     * @param file path to Dragons.txt
     * @return list of Dragon instances parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Dragon> loadDragons(Path file) throws IOException {
        List<Dragon> dragons = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int level     = Integer.parseInt(parts[1]);
                int damage    = Integer.parseInt(parts[2]);
                int defense   = Integer.parseInt(parts[3]);
                int dodge     = Integer.parseInt(parts[4]);
                int baseHP = level * 100;

                Dragon d = new Dragon.Builder(name, level)
                        .baseHP(baseHP)
                        .baseDamage(damage)
                        .defense(defense)
                        .dodge(dodge)
                        .build();

                dragons.add(d);
            }
        }

        return dragons;
    }

    /**
     * Load all Exoskeletons from the given file.
     *
     * @param file path to Exoskeletons.txt
     * @return list of Exoskeleton instances parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Exoskeleton> loadExoskeletons(Path file) throws IOException {
        List<Exoskeleton> exos = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int level     = Integer.parseInt(parts[1]);
                int damage    = Integer.parseInt(parts[2]);
                int defense   = Integer.parseInt(parts[3]);
                int dodge     = Integer.parseInt(parts[4]);
                int baseHP    = level * 100;

                Exoskeleton e = new Exoskeleton.Builder(name, level)
                        .baseHP(baseHP)
                        .baseDamage(damage)
                        .defense(defense)
                        .dodge(dodge)
                        .build();

                exos.add(e);
            }
        }

        return exos;
    }

    /**
     * Load all Spirits from the given file.
     *
     * @param file path to Spirits.txt
     * @return list of Spirit instances parsed from the file
     * @throws IOException if reading the file fails
     */
    public static List<Spirit> loadSpirits(Path file) throws IOException {
        List<Spirit> spirits = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(file)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                String name   = parts[0];
                int level     = Integer.parseInt(parts[1]);
                int damage    = Integer.parseInt(parts[2]);
                int defense   = Integer.parseInt(parts[3]);
                int dodge     = Integer.parseInt(parts[4]);
                int baseHP    = level * 100;
                Spirit s = new Spirit.Builder(name, level)
                        .baseHP(baseHP)
                        .baseDamage(damage)
                        .defense(defense)
                        .dodge(dodge)
                        .build();

                spirits.add(s);
            }
        }

        return spirits;
    }
}
