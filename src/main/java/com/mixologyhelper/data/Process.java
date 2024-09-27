package com.mixologyhelper.data;

import lombok.Getter;

@Getter
public enum Process {
    // By spam clicking, the process takes 10 ticks and rewards an additional 20 exp
    CONCENTRATE(2, "Concentrate", "Retort", " (Spam-Click)", 20, 10),
    // Not sure how the mechanic works on this one, but without clicking it takes 21 ticks and gives 0 extra exp
    HOMOGENISE(1, "Homogenise", "Agitator", "", 0, 21),
    // Clicking after 5 pumps grants 14 exp and takes 13 ticks in total
    CRYSTALISE(3, "Crystalise", "Alembic", " (Click after 5 pumps)", 14, 13);

    private final int id;
    private final String name;
    private final String machine;
    private final String extraInstructions;
    private final int potentialExp;
    private final int tickDuration; // How many ticks it takes for the machine to finish the process

    Process(int id, String name, String machine, String extraInstructions, int potentialExp, int tickDuration) {
        this.id = id;
        this.name = name;
        this.machine = machine;
        this.extraInstructions = extraInstructions;
        this.potentialExp = potentialExp;
        this.tickDuration = tickDuration;
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
