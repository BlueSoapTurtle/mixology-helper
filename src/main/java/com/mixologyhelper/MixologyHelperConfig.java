package com.mixologyhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(MixologyHelperConfig.GROUP)
public interface MixologyHelperConfig extends Config {
    String GROUP = "mixologyhelper";

    @ConfigSection(
            name = "Tooltip Options",
            description = "Settings tooltips",
            position = 0
    )
    String tooltipSection = "tooltipSection";

    @ConfigItem(
            keyName = "showTooltip",
            name = "Show Paste Tooltips",
            description = "Herbs should show how much paste they give on tooltips",
            position = 1,
            section = tooltipSection
    )
    default boolean showTooltip()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showTooltipEach",
            name = "Show how much paste each herb gives",
            description = "The tooltip will also show how much paste each item gives, ex 50k (10 ea)",
            position = 2,
            section = tooltipSection
    )
    default boolean showTooltipEach()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showPricePerPaste",
            name = "Show GE price per paste",
            description = "Adds a line to the tooltip showing the GE price per paste",
            position = 3,
            section = tooltipSection
    )
    default boolean showPricePerPaste()
    {
        return true;
    }

    @ConfigSection(
            name = "Highlight Options",
            description = "Settings to control highlighting",
            position = 4
    )
    String highlightSection = "highlightSection";

    @ConfigItem(
            keyName = "showLevers",
            name = "Highlight Levers",
            description = "Toggle highlighting of levers",
            position = 5,
            section = highlightSection
    )
    default boolean showLevers() {
        return true;
    }

    @ConfigItem(
            keyName = "showMixer",
            name = "Highlight Mixer",
            description = "Toggle highlighting of the mixer",
            position = 6,
            section = highlightSection
    )
    default boolean showMixer() {
        return true;
    }

    @ConfigItem(
            keyName = "showMachines",
            name = "Highlight Machines",
            description = "Toggle highlighting of processing machines",
            position = 7,
            section = highlightSection
    )
    default boolean showMachines() {
        return true;
    }

    @ConfigItem(
            keyName = "showConveyorBelt",
            name = "Highlight Conveyor Belt",
            description = "Toggle highlighting of the conveyor belt",
            position = 8,
            section = highlightSection
    )
    default boolean showConveyorBelt() {
        return true;
    }

    @ConfigItem(
            keyName = "showArrows",
            name = "Show Arrows",
            description = "Toggle showing arrows on the next object",
            position = 9,
            section = highlightSection
    )
    default boolean showArrows() {
        return true;
    }

    @ConfigSection(
            name = "Priority Options",
            description = "Settings to prioritize certain potions",
            position = 10
    )
    String prioritySection = "prioritySection";

    @ConfigItem(
            keyName = "priorityType",
            name = "Priority Type",
            description = "Select priority between Experience or Ingredient",
            position = 11,
            section = prioritySection
    )
    default PriorityType priorityType() {
        return PriorityType.EXPERIENCE;
    }

    @ConfigItem(
            keyName = "priorityIngredient",
            name = "Priority Ingredient",
            description = "Select ingredient to prioritize",
            position = 12,
            section = prioritySection
    )
    default Ingredient priorityIngredient() {
        return Ingredient.LYE;
    }

    @ConfigSection(
            name = "Display Options",
            description = "Settings for panel display",
            position = 13
    )
    String displaySection = "displaySection";

    @ConfigItem(
            keyName = "showPanel",
            name = "Show panel",
            description = "Toggle displaying the panel",
            position = 14,
            section = displaySection
    )
    default boolean showPanel() {
        return true;
    }

    @ConfigItem(
            keyName = "showPotionExp",
            name = "Show Potion Experience",
            description = "Toggle displaying how much experience a potion will give",
            position = 15,
            section = displaySection
    )
    default boolean showPotionExp() {
        return true;
    }

    @ConfigItem(
            keyName = "selectedReward",
            name = "Selected Reward",
            description = "Select a reward to track resin for",
            position = 16,
            section = displaySection
    )
    default Reward selectedReward() {
        return Reward.NONE;
    }

    @ConfigItem(
            keyName = "goalDisplayFormat",
            name = "Goal Display Format",
            description = "Choose how to display the goal progress",
            position = 17,
            section = displaySection
    )
    default GoalDisplayFormat goalDisplayFormat() {
        return GoalDisplayFormat.CURRENT_TOTAL;
    }

    @ConfigItem(
            keyName = "showOrdersFulfilled",
            name = "Show Orders Fulfilled",
            description = "Toggle showing the orders fulfilled in the panel",
            position = 18,
            section = displaySection
    )
    default boolean showOrdersFulfilled() {
        return true;
    }

    @ConfigItem(
            keyName = "showOrdersFulfilledInfobox",
            name = "Show Orders Fulfilled Infobox",
            description = "Toggle showing an infobox with the orders fulfilled",
            position = 19,
            section = displaySection
    )
    default boolean showOrdersFulfilledInfobox() {
        return true;
    }

    @ConfigItem(
            keyName = "showBestOrderInfobox",
            name = "Show Best Order Infobox",
            description = "Toggle showing an infobox with the best order and its details",
            position = 20,
            section = displaySection
    )
    default boolean showBestOrderInfobox() {
        return true;
    }

    @ConfigItem(
            keyName = "showDebugPanel",
            name = "Show debug panel",
            description = "Toggle displaying the debug panel",
            position = 21,
            section = displaySection
    )
    default boolean showDebugPanel() {
        return false;
    }
}