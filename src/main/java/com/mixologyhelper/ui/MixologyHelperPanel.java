package com.mixologyhelper.ui;

import com.mixologyhelper.*;
import com.mixologyhelper.data.*;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.QuantityFormatter;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

public class MixologyHelperPanel extends OverlayPanel {
    private final Client client;
    private final MixologyHelperPlugin plugin;
    private final MixologyHelperConfig config;

    @Inject
    private MixologyHelperPanel(Client client,
                                MixologyHelperPlugin plugin,
                                MixologyHelperConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showPanel()) {
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder().text("Mixology Helper").build());

        int herbloreLevel = client.getRealSkillLevel(Skill.HERBLORE);
        List<Order> orders = plugin.getOrders();

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            Recipe recipe = order.getRecipe();
            String leftText = recipe.getName() + " (" + order.getProcess().getName() + ")";
            // Color the left side red if the player doesn't have the required level, green if it's the best order
            Color leftColor = plugin.getBestOrderIndex() == i ? Color.GREEN : Color.WHITE;
            if (herbloreLevel < recipe.getLevel()) {
                leftColor = Color.RED;
            }

            String rightText = recipe.getShortName();
            Color rightColor = Color.WHITE;

            if (config.showPotionExp()) {
                rightText += " (" + order.getExp() + " xp)";
            }

            panelComponent.getChildren().add(LineComponent.builder().left(leftText).leftColor(leftColor).right(rightText).rightColor(rightColor).build());
        }

        if (config.selectedReward() != Reward.NONE) {
            panelComponent.getChildren().add(LineComponent.builder().left("").right("").build());
            Reward selectedReward = config.selectedReward();

            int moxResinNeeded = Math.max(selectedReward.getMoxResinCost() - plugin.getMoxResin(), 0);
            int agaResinNeeded = Math.max(selectedReward.getAgaResinCost() - plugin.getAgaResin(), 0);
            int lyeResinNeeded = Math.max(selectedReward.getLyeResinCost() - plugin.getLyeResin(), 0);

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Goal:")
                    .right(selectedReward.getName())
                    .build());

            if (config.goalDisplayFormat() == GoalDisplayFormat.LEFT) {
                if (moxResinNeeded > 0)
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Mox:")
                            .right(QuantityFormatter.quantityToStackSize(moxResinNeeded) + " left")
                            .build());
                if (agaResinNeeded > 0)
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Aga:")
                            .right(QuantityFormatter.quantityToStackSize(agaResinNeeded) + " left")
                            .build());
                if (lyeResinNeeded > 0)
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Lye:")
                            .right(QuantityFormatter.quantityToStackSize(lyeResinNeeded) + " left")
                            .build());
            } else {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Mox:")
                        .right(plugin.getMoxResin() + "/" + QuantityFormatter.quantityToStackSize(selectedReward.getMoxResinCost()))
                        .build());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Aga:")
                        .right(QuantityFormatter.quantityToStackSize(plugin.getAgaResin()) + "/" + QuantityFormatter.quantityToStackSize(selectedReward.getAgaResinCost()))
                        .build());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Lye:")
                        .right(QuantityFormatter.quantityToStackSize(plugin.getLyeResin()) + "/" + QuantityFormatter.quantityToStackSize(selectedReward.getLyeResinCost()))
                        .build());
            }
        }

        if (config.showBankedPaste()) {
            panelComponent.getChildren().add(LineComponent.builder().left("").right("").build());
            String right = "Open bank";
            if (!plugin.getBankedPaste().isEmpty()) {
                String bankedMox = QuantityFormatter.quantityToStackSize(plugin.getBankedPaste().getOrDefault(Ingredient.MOX, 0));
                String bankedAga = QuantityFormatter.quantityToStackSize(plugin.getBankedPaste().getOrDefault(Ingredient.AGA, 0));
                String bankedLye = QuantityFormatter.quantityToStackSize(plugin.getBankedPaste().getOrDefault(Ingredient.LYE, 0));
                right = bankedMox + "/" + bankedAga + "/" + bankedLye;
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Banked Paste M/A/L:")
                    .right(right)
                    .build());
        }

        if (config.showOrdersFulfilled()) {
            panelComponent.getChildren().add(LineComponent.builder().left("").right("").build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Orders Fulfilled:")
                    .right(QuantityFormatter.quantityToStackSize(plugin.getPotionsMade()))
                    .build());
        }

        panelComponent.setPreferredSize(new Dimension(300, 200));
        return super.render(graphics);
    }
}