package com.mixologyhelper.ui;

import com.mixologyhelper.MixologyHelperConfig;
import com.mixologyhelper.MixologyHelperPlugin;
import com.mixologyhelper.data.MixologyStep;
import com.mixologyhelper.data.Order;
import com.mixologyhelper.data.Recipe;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class MixologyHelperDebugPanel extends OverlayPanel {
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

        Order bestOrder = plugin.getbestOrder();
        String order = bestOrder == null ? "None" : (bestOrder.getRecipe().getName() + " " + bestOrder.getRecipe().getShortName());
        panelComponent.getChildren().add(LineComponent.builder().left("Best Order:").leftColor(color).right(order).build());


        panelComponent.setPreferredSize(new Dimension(300, 200));
        return super.render(graphics);
    }
}
