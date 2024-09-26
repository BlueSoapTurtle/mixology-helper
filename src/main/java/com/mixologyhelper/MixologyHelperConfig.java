package com.mixologyhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(MixologyHelperConfig.GROUP)
public interface MixologyHelperConfig extends Config {
    String GROUP = "mixologyhelper";

    @ConfigSection(
            name = "Highlight Options",
            description = "Settings to control highlighting",
            position = 0
    )
    String highlightSection = "highlightSection";

    @ConfigItem(
            keyName = "showLevers",
            name = "Highlight Levers",
            description = "Toggle highlighting of levers",
            position = 1,
            section = highlightSection
    )
    default boolean showLevers() {
        return true;
    }

    @ConfigItem(
            keyName = "showMixer",
            name = "Highlight Mixer",
            description = "Toggle highlighting of the mixer",
            position = 2,
            section = highlightSection
    )
    default boolean showMixer() {
        return true;
    }

    @ConfigItem(
            keyName = "showMachines",
            name = "Highlight Machines",
            description = "Toggle highlighting of processing machines",
            position = 3,
            section = highlightSection
    )
    default boolean showMachines() {
        return true;
    }

    @ConfigItem(
            keyName = "showConveyorBelt",
            name = "Highlight Conveyor Belt",
            description = "Toggle highlighting of the conveyor belt",
            position = 4,
            section = highlightSection
    )
    default boolean showConveyorBelt() {
        return true;
    }

    @ConfigItem(
            keyName = "showArrows",
            name = "Show Arrows",
            description = "Toggle showing arrows on the next object",
            position = 5,
            section = highlightSection
    )
    default boolean showArrows() {
        return true;
    }

    @ConfigSection(
            name = "Priority Options",
            description = "Settings to prioritize certain potions",
            position = 6
    )
    String prioritySection = "prioritySection";

    @ConfigItem(
            keyName = "priorityType",
            name = "Priority Type",
            description = "Select priority between Experience or Ingredient",
            position = 7,
            section = prioritySection
    )
    default PriorityType priorityType() {
        return PriorityType.EXPERIENCE;
    }

    @ConfigItem(
            keyName = "priorityIngredient",
            name = "Priority Ingredient",
            description = "Select ingredient to prioritize",
            position = 8,
            section = prioritySection
    )
    default Ingredient priorityIngredient() {
        return Ingredient.LYE;
    }

    @ConfigSection(
            name = "Display Options",
            description = "Settings for panel display",
            position = 9
    )
    String displaySection = "displaySection";

    @ConfigItem(
            keyName = "showPanel",
            name = "Show panel",
            description = "Toggle displaying the panel",
            position = 10,
            section = displaySection
    )
    default boolean showPanel() {
        return true;
    }

    @ConfigItem(
            keyName = "showPotionExp",
            name = "Show Potion Experience",
            description = "Toggle displaying how much experience a potion will give",
            position = 11,
            section = displaySection
    )
    default boolean showPotionExp() {
        return true;
    }

    @ConfigItem(
            keyName = "selectedReward",
            name = "Selected Reward",
            description = "Select a reward to track resin for",
            position = 12,
            section = displaySection
    )
    default Reward selectedReward() {
        return Reward.NONE;
    }

    @ConfigItem(
            keyName = "goalDisplayFormat",
            name = "Goal Display Format",
            description = "Choose how to display the goal progress",
            position = 13,
            section = displaySection
    )
    default GoalDisplayFormat goalDisplayFormat() {
        return GoalDisplayFormat.CURRENT_TOTAL;
    }

    @ConfigItem(
            keyName = "showOrdersFulfilled",
            name = "Show Orders Fulfilled",
            description = "Toggle showing the orders fulfilled in the panel",
            position = 14,
            section = displaySection
    )
    default boolean showOrdersFulfilled() {
        return true;
    }

    @ConfigItem(
            keyName = "showOrdersFulfilledInfobox",
            name = "Show Orders Fulfilled Infobox",
            description = "Toggle showing an infobox with the orders fulfilled",
            position = 15,
            section = displaySection
    )
    default boolean showOrdersFulfilledInfobox() {
        return true;
    }

    @ConfigItem(
            keyName = "showBestOrderInfobox",
            name = "Show Best Order Infobox",
            description = "Toggle showing an infobox with the best order and its details",
            position = 16,
            section = displaySection
    )
    default boolean showBestOrderInfobox() {
        return true;
    }

    @ConfigItem(
            keyName = "showDebugPanel",
            name = "Show debug panel",
            description = "Toggle displaying the debug panel",
            position = 17,
            section = displaySection
    )
    default boolean showDebugPanel() {
        return false;
    }
}