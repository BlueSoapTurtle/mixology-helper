package com.mixologyhelper;

public enum GoalDisplayFormat {
    LEFT("Remaining"),
    CURRENT_TOTAL("Current/Total");

    private final String name;

    GoalDisplayFormat(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}