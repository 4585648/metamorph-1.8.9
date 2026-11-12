// src/main/java/com/example/metamorph/common/MorphConfig.java
package com.example.metamorph.common;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MorphConfig {

    public static boolean allowMenu = true;
    public static boolean commandOnly = false;
    public static boolean allowAllIfListEmpty = true;

    public static Set<Integer> allowedIds = new HashSet<Integer>();

    public static void load(File file) {
        Configuration config = new Configuration(file);

        try {
            config.load();

            allowMenu = config.getBoolean(
                    "allowMenu",
                    Configuration.CATEGORY_GENERAL,
                    true,
                    "Allow players to open the morph menu."
            );

            commandOnly = config.getBoolean(
                    "commandOnly",
                    Configuration.CATEGORY_GENERAL,
                    false,
                    "If true, only players with permission can morph from the menu."
            );

            allowAllIfListEmpty = config.getBoolean(
                    "allowAllIfListEmpty",
                    Configuration.CATEGORY_GENERAL,
                    true,
                    "If allowedIds is empty, allow all registered morphs."
            );

            String[] ids = config.getStringList(
                    "allowedIds",
                    Configuration.CATEGORY_GENERAL,
                    new String[0],
                    "Allowed numeric morph IDs. Empty means use allowAllIfListEmpty."
            );

            allowedIds.clear();

            for (String s : ids) {
                try {
                    allowedIds.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException ignored) {

                }
            }

        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static boolean isAllowed(int morphId) {
        if (morphId == 0) {
            return true;
        }

        if (allowedIds.isEmpty()) {
            return allowAllIfListEmpty;
        }

        return allowedIds.contains(morphId);
    }
}