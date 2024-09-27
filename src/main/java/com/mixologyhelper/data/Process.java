package com.mixologyhelper.data;

import lombok.Getter;

@Getter
public enum Process {
    CONCENTRATE(2, "Concentrate", "Retort", " (Spam-Click)"),
    HOMOGENISE(1, "Homogenise", "Agitator", ""),
    CRYSTALISE(3, "Crystalise", "Alembic", " (Click after 5 pumps)");

    private final int id;
    private final String name;
    private final String machine;
    private final String extraInstructions;

    Process(int id, String name, String machine, String extraInstructions) {
        this.id = id;
        this.name = name;
        this.machine = machine;
        this.extraInstructions = extraInstructions;
    }

    public static Process getProcessFromId(int id) {
        for (Process process : Process.values()) {
            if (process.getId() == id) {
                return process;
            }
        }
        return null;
    }
}
