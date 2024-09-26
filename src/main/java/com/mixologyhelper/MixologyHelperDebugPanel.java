package com.mixologyhelper;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

class MixologyHelperDebugPanel extends OverlayPanel {
    private final MixologyHelperPlugin plugin;
    private final MixologyHelperConfig config;

    @Inject
    private MixologyHelperDebugPanel(MixologyHelperPlugin plugin, MixologyHelperConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Return null if debug is disabled
        if (!config.showDebugPanel()) {
            return null;
        }


        panelComponent.getChildren().add(TitleComponent.builder().text("Mixology Helper - DEBUG PANEL").build());
        Color color = new Color(150, 150, 255);

        int ingredient1 = plugin.getIngredient1();
        panelComponent.getChildren().add(LineComponent.builder().left("Ingredient 1:").leftColor(color).right(String.valueOf(ingredient1)).build());

        int ingredient2 = plugin.getIngredient2();
        panelComponent.getChildren().add(LineComponent.builder().left("Ingredient 2:").leftColor(color).right(String.valueOf(ingredient2)).build());

        int ingredient3 = plugin.getIngredient3();
        panelComponent.getChildren().add(LineComponent.builder().left("Ingredient 3:").leftColor(color).right(String.valueOf(ingredient3)).build());

        Recipe currentRecipe = plugin.getCurrentRecipe();
        String right = currentRecipe == null ? "None" : currentRecipe.getName() + " " + currentRecipe.getShortName();
        panelComponent.getChildren().add(LineComponent.builder().left("Current Recipe:").leftColor(color).right(right).build());

        MixologyStep currentStep = plugin.getCurrentStep(); // Not null
        panelComponent.getChildren().add(LineComponent.builder().left("Current Step:").leftColor(color).right(currentStep.toString()).build());

        int orderSize = plugin.getOrders().size();
        panelComponent.getChildren().add(LineComponent.builder().left("Orders:").leftColor(color).right(String.valueOf(orderSize)).build());

        int bestOrderIndex = plugin.getBestOrderIndex();
        panelComponent.getChildren().add(LineComponent.builder().left("Best Order Index:").leftColor(color).right(String.valueOf(bestOrderIndex)).build());


        panelComponent.setPreferredSize(new Dimension(300, 200));
        return super.render(graphics);
    }
}
