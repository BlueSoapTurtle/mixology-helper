package com.mixologyhelper.data;

import lombok.Getter;

@Getter
public enum Process {
    CONCENTRATE(2, "Concentrate", "Retort", " (Spam-Click)", 20),
    HOMOGENISE(1, "Homogenise", "Agitator", "", 0),
    CRYSTALISE(3, "Crystalise", "Alembic", " (Click after 5 pumps)", 14);

    private final int id;
    private final String name;
    private final String machine;
    private final String extraInstructions;
    private final int potentialExp;

    Process(int id, String name, String machine, String extraInstructions, int potentialExp) {
        this.id = id;
        this.name = name;
        this.machine = machine;
        this.extraInstructions = extraInstructions;
        this.potentialExp = potentialExp;
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
