package com.example.game;

import java.util.HashMap;
import java.util.Map;

public class ModuleRegistry {
    private static final Map<String, Class<? extends Module>> moduleClasses = new HashMap<>();

    static {
        registerModule("NONE", Module.class);
        registerModule("Timer", ModuleTimer.class);
        registerModule("WordDisplay", ModuleWords.class);
    }

    public static void registerModule(String type, Class<? extends Module> moduleClass) {
        moduleClasses.put(type, moduleClass);
    }

    public static Class<? extends Module> getModuleClass(String type) {
        return moduleClasses.get(type);
    }

    public static Module createModule(String type, int row, int col, Object... params) {
        try {
            Class<? extends Module> moduleClass = getModuleClass(type);
            if (moduleClass == null) {
                return new Module(row, col);
            }
            if (type.equals("Timer")) {
                return new ModuleTimer(row, col, (Long) params[0]);
            }
            if (type.equals("WordDisplay")) {
                return new ModuleWords(row, col, (int) params[0]);
            }
            return moduleClass.getConstructor(int.class, int.class)
                    .newInstance(row, col);
        } catch (Exception e) {
            e.printStackTrace();
            return new Module(row, col);
        }
    }
}
