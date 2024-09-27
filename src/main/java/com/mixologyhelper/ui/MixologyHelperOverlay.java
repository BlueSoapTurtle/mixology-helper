package com.mixologyhelper.ui;

import com.mixologyhelper.MixologyHelperConfig;
import com.mixologyhelper.MixologyHelperPlugin;
import com.mixologyhelper.data.Process;
import com.mixologyhelper.data.*;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mixologyhelper.Constants.ORDER_WIDGET_ID;

public class MixologyHelperOverlay extends Overlay {
    private final Client client;
    private final MixologyHelperPlugin plugin;
    private final MixologyHelperConfig config;

    @Inject
    private MixologyHelperOverlay(Client client, MixologyHelperPlugin plugin, MixologyHelperConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Exit if the potion order widget is not visible
        Widget orderWidget = client.getWidget(ORDER_WIDGET_ID);
        if (orderWidget == null || orderWidget.isHidden()) {
            return null;
        }

        // Exit if there is no best order
        Order bestOrder = plugin.getbestOrder();
        if (bestOrder == null) {
            return null;
        }

        if (config.showLevers()) {
            drawLevers(graphics, bestOrder);
        }

        if (config.showMixer()) {
            drawMixer(graphics, bestOrder);
        }

        if (config.showMachines()) {
            drawMachine(graphics, bestOrder);
        }

        if (config.showConveyorBelt()) {
            drawConveyorBelt(graphics);
        }

        return null;
    }

    private void drawLevers(Graphics2D graphics, Order order) {
        // Do not highlight levers after all ingredients have been added
        if (plugin.getCurrentStep().ordinal() >= MixologyStep.PICKUP_POTION.ordinal()) {
            return;
        }

        List<Ingredient> ingredients = order.getRecipe().getIngredients();

        // Map to collect steps per lever
        Map<Ingredient, List<Integer>> leverSteps = new HashMap<>();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            leverSteps.computeIfAbsent(ingredient, k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<Ingredient, List<Integer>> entry : leverSteps.entrySet()) {
            Ingredient ingredient = entry.getKey();
            List<Integer> steps = entry.getValue();
            TileObject lever = plugin.getLever(ingredient);
            if (lever == null) {
                continue;
            }

            // Create text by joining step numbers
            String text = steps.stream().map(s -> String.valueOf(s + 1)).collect(Collectors.joining(","));

            Color color;
            boolean drawArrow = false;

            // Determine if lever has current step
            boolean isCurrentLever = steps.contains(plugin.getCurrentStep().ordinal());

            if (isCurrentLever) {
                color = ColorScheme.PROGRESS_COMPLETE_COLOR; // Green
                drawArrow = true;
            } else if (steps.stream().allMatch(step -> step < plugin.getCurrentStep().ordinal())) {
                // All steps for this lever have been completed
                continue; // Do not highlight lever
            } else {
                color = ColorScheme.PROGRESS_INPROGRESS_COLOR; // Orange
            }

            highlightGameObject(graphics, lever, text, color, drawArrow);
        }
    }

    private void drawMixer(Graphics2D graphics, Order order) {
        // Do not highlight mixer after potion has been picked up
        if (plugin.getCurrentStep().ordinal() >= MixologyStep.PROCESS_POTION.ordinal()) {
            return;
        }

        Recipe currentRecipe = plugin.getCurrentRecipe();
        if (currentRecipe != null && currentRecipe.equals(order.getRecipe())) {
            // Correct recipe, draw green and with an arrow
            highlightGameObject(graphics, plugin.getMixer(), "Pick up Potion", ColorScheme.PROGRESS_COMPLETE_COLOR, true);
        } else if (plugin.getCurrentStep() == MixologyStep.PICKUP_POTION) { // Only show red if the current step is to pickup the potion
            // Wrong recipe, draw red and without an arrow
            highlightGameObject(graphics, plugin.getMixer(), "Wrong Potion", ColorScheme.PROGRESS_ERROR_COLOR, false);
        } else {
            // Default to orange, no arrow
            highlightGameObject(graphics, plugin.getMixer(), "Pick up Potion", ColorScheme.PROGRESS_INPROGRESS_COLOR, false);
        }
    }

    private void drawMachine(Graphics2D graphics, Order order) {
        // Do not highlight machine after potion has been processed
        if (plugin.getCurrentStep().ordinal() >= MixologyStep.DELIVER_POTION.ordinal()) {
            return;
        }

        // Highlight green if current, orange otherwise
        boolean currentStep = plugin.getCurrentStep() == MixologyStep.PROCESS_POTION || plugin.getCurrentStep() == MixologyStep.PROCESSING_POTION;
        Color color = currentStep ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.PROGRESS_INPROGRESS_COLOR;
        Process process = order.getProcess();
        TileObject machine = plugin.getMachine(process);
        highlightGameObject(graphics, machine, process.getMachine() + process.getExtraInstructions(), color, currentStep);
    }

    private void drawConveyorBelt(Graphics2D graphics) {
        // Highlight green if current, orange otherwise
        boolean currentStep = plugin.getCurrentStep() == MixologyStep.DELIVER_POTION;
        Color color = currentStep ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.PROGRESS_INPROGRESS_COLOR;

        boolean drawArrow = currentStep;
        for (TileObject conveyorBelt : plugin.getConveyorBelts()) {
            highlightGameObject(graphics, conveyorBelt, "Deliver Potion", color, drawArrow);
            drawArrow = false; // Only draw arrow on the first belt
        }
    }

    private void highlightGameObject(Graphics2D graphics, TileObject tileObject, String text, Color color, boolean drawArrow) {
        if (tileObject == null) {
            return;
        }

        drawTextOverlay(graphics, tileObject, text, color);
        drawObjectClickbox(graphics, tileObject, color);

        if (drawArrow && config.showArrows()) {
            client.setHintArrow(tileObject.getWorldLocation());
        } else if (plugin.getCurrentStep() == MixologyStep.COMPLETED || !config.showArrows()) {
            client.clearHintArrow();
        }
    }

    private void drawTextOverlay(Graphics2D graphics, TileObject tileObject, String text, Color color) {
        // Draw the text overlay
        LocalPoint textLocation = tileObject.getLocalLocation();
        textLocation = new LocalPoint(textLocation.getX(), textLocation.getY());
        Point canvasLocation = Perspective.getCanvasTextLocation(client, graphics, textLocation, text, 100);
        OverlayUtil.renderTextLocation(graphics, canvasLocation, text, color);
    }

    private void drawObjectClickbox(Graphics2D graphics, TileObject tileObject, Color color) {
        Shape objectClickbox = tileObject.getClickbox();
        if (objectClickbox != null) {
            Point mousePosition = client.getMouseCanvasPosition();
            if (objectClickbox.contains(mousePosition.getX(), mousePosition.getY())) {
                graphics.setColor(color.darker());
            } else {
                graphics.setColor(color);
            }
            graphics.draw(objectClickbox);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
            graphics.fill(objectClickbox);
        }
    }
}
