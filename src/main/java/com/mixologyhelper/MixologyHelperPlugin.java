package com.mixologyhelper;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.*;

@PluginDescriptor(
        name = "Mixology Helper",
        description = "Helper plugin for the Mastering Mixology minigame"
)
public class MixologyHelperPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientThread clientThread;
    @Inject
    private MixologyHelperOverlay overlay;
    @Inject
    private BankTooltips bankTooltips;
    @Inject
    private MixologyHelperPanel panel;
    @Inject
    private MixologyHelperDebugPanel debugPanel;
    @Inject
    private MixologyHelperConfig config;
    @Inject
    private ConfigManager configManager;
    @Inject
    private InfoBoxManager infoBoxManager;
    private OrderFulfilledInfoBox orderFulfilledInfoBox;
    private BestOrderInfoBox bestOrderInfoBox;


    // All the varbits!
    private static final int MOX_PASTE_VARBIT = 11321;
    private static final int AGA_PASTE_VARBIT = 11322;
    private static final int LYE_PASTE_VARBIT = 11323;
    private static final int MOX_RESIN_VARBIT = 4416;
    private static final int AGA_RESIN_VARBIT = 4415;
    private static final int LYE_RESIN_VARBIT = 4414;
    private static final int POTIONS_MADE_VARBIT = 4480;
    private static final int INGREDIENT_1_CHANGED = 11324;
    private static final int INGREDIENT_2_CHANGED = 11325;
    private static final int INGREDIENT_3_CHANGED = 11326;
    private static final int RECIPE_VARBIT = 11339;
    private static final int ORDER_WIDGET_ID = 57802754;
    private static final int RECIPE_1_VARBIT = 11315;
    private static final int PROCESS_1_VARBIT = 11316;
    private static final int RECIPE_2_VARBIT = 11317;
    private static final int PROCESS_2_VARBIT = 11318;
    private static final int RECIPE_3_VARBIT = 11319;
    private static final int PROCESS_3_VARBIT = 11320;


    // Player data
    @Getter
    private int moxPaste;
    @Getter
    private int agaPaste;
    @Getter
    private int lyePaste;
    @Getter
    private int moxResin;
    @Getter
    private int agaResin;
    @Getter
    private int lyeResin;
    @Getter
    private int potionsMade;

    // Info about current potion in the mixer
    @Getter
    private int ingredient1;
    @Getter
    private int ingredient2;
    @Getter
    private int ingredient3;
    @Getter
    private Recipe currentRecipe = null;


    // The objects
    @Getter
    private final List<TileObject> conveyorBelts = new ArrayList<>();
    @Getter
    private final Map<Ingredient, TileObject> levers = new HashMap<>();
    @Getter
    private final Map<Process, TileObject> machines = new HashMap<>();
    @Getter
    private TileObject mixer;

    @Getter
    List<Order> orders = Arrays.asList(new Order(), new Order(), new Order());
    @Getter
    int bestOrderIndex = -1;
    @Getter
    private MixologyStep currentStep = MixologyStep.ADD_INGREDIENT_1;


    @Override
    protected void startUp() {
        overlayManager.add(overlay);
        overlayManager.add(panel);
        overlayManager.add(debugPanel);
        overlayManager.add(bankTooltips);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlayManager.remove(panel);
        overlayManager.remove(debugPanel);
        overlayManager.remove(bankTooltips);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState().equals(GameState.LOGGED_IN)) {
            resetPotionState();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        // Update order list when herblore level changes
        // This also fixes the best order not updating on login
        if (statChanged.getSkill() == Skill.HERBLORE) {
            updateBestOrder();
        }
    }

    @Subscribe
    protected void onConfigChanged(ConfigChanged configChanged) {
        if (!MixologyHelperConfig.GROUP.equals(configChanged.getGroup())) {
            return;
        }

        // Remove the arrow if the user disables it
        if (!config.showArrows()) {
            client.clearHintArrow();
        }

        // Update the order list since they might have changed the priority
        clientThread.invokeLater(this::updateBestOrder);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        String message = chatMessage.getMessage();

        if (message.startsWith("You collect some") && message.endsWith("from the mixing vessel.")) {
            // Player picked up the potion from the mixer
            currentStep = MixologyStep.PROCESS_POTION;
        } else if (message.startsWith("You start") && (message.contains("concentrating") || message.contains("homogenising") || message.contains("crystallising"))) {
            // Player started processing the potion
            currentStep = MixologyStep.PROCESSING_POTION;
        } else if (message.startsWith("You finish") && (message.contains("concentrating") || message.contains("homogenising") || message.contains("crystallising"))) {
            // Player finished processing the potion
            currentStep = MixologyStep.DELIVER_POTION;
        } else if (message.startsWith("You deposit some") && message.contains("and fulfil an order")) {
            // Player delivered the potion successfully
            currentStep = MixologyStep.COMPLETED;
            // Reset for next potion
            resetPotionState();
        } else if (message.startsWith("You deposit some") && message.contains("but fail to fulfil an order")) {
            // Player delivered the wrong potion
            currentStep = MixologyStep.COMPLETED;
            // Reset for next potion
            resetPotionState();
        }
    }

    private void resetPotionState() {
        ingredient1 = 0;
        ingredient2 = 0;
        ingredient3 = 0;
        currentRecipe = null;
        currentStep = MixologyStep.ADD_INGREDIENT_1;
        client.clearHintArrow();
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        int varbitId = varbitChanged.getVarbitId();
        int value = varbitChanged.getValue();

        if (varbitId == MOX_PASTE_VARBIT) {
            moxPaste = value;
        } else if (varbitId == AGA_PASTE_VARBIT) {
            agaPaste = value;
        } else if (varbitId == LYE_PASTE_VARBIT) {
            lyePaste = value;
        } else if (varbitChanged.getVarpId() == MOX_RESIN_VARBIT) {
            moxResin = value;
        } else if (varbitChanged.getVarpId() == AGA_RESIN_VARBIT) {
            agaResin = value;
        } else if (varbitChanged.getVarpId() == LYE_RESIN_VARBIT) {
            lyeResin = value;
        } else if (varbitChanged.getVarpId() == POTIONS_MADE_VARBIT) {
            potionsMade = value;
            updateInfoboxes();
        } else if (varbitId == INGREDIENT_1_CHANGED) {
            ingredient1 = value;
            updateCurrentStep();
        } else if (varbitId == INGREDIENT_2_CHANGED) {
            ingredient2 = value;
            updateCurrentStep();
        } else if (varbitId == INGREDIENT_3_CHANGED) {
            ingredient3 = value;
            updateCurrentStep();
        } else if (varbitId == RECIPE_VARBIT) {
            currentRecipe = Recipe.getRecipeFromId(value);
            updateCurrentStep();
        } else if (varbitId == RECIPE_1_VARBIT && value != 0) {
            updateRecipeFromVarbit(value, 0);
        } else if (varbitId == PROCESS_1_VARBIT && value != 0) {
            updateProcessFromVarbit(value, 0);
        } else if (varbitId == RECIPE_2_VARBIT && value != 0) {
            updateRecipeFromVarbit(value, 1);
        } else if (varbitId == PROCESS_2_VARBIT && value != 0) {
            updateProcessFromVarbit(value, 1);
        } else if (varbitId == RECIPE_3_VARBIT && value != 0) {
            updateRecipeFromVarbit(value, 2);
        } else if (varbitId == PROCESS_3_VARBIT && value != 0) {
            updateProcessFromVarbit(value, 2);
        }
    }

    private void updateRecipeFromVarbit(int value, int i) {
        Recipe recipe = Recipe.getRecipeFromId(value);
        if (recipe != null) {
            orders.get(i).setRecipe(recipe);
        }
        updateBestOrder();
    }

    private void updateProcessFromVarbit(int value, int i) {
        Process process = Process.getProcessFromId(value);
        if (process != null) {
            orders.get(i).setProcess(process);
        }
        updateBestOrder();
    }

    private void updateCurrentStep() {
        // Only update currentStep if it's before PROCESS_POTION
        if (currentStep.ordinal() >= MixologyStep.PROCESS_POTION.ordinal()) {
            return;
        }

        if (ingredient1 == 0) {
            currentStep = MixologyStep.ADD_INGREDIENT_1;
        } else if (ingredient2 == 0) {
            currentStep = MixologyStep.ADD_INGREDIENT_2;
        } else if (ingredient3 == 0) {
            currentStep = MixologyStep.ADD_INGREDIENT_3;
        } else if (currentRecipe != null && correctIngredientsAdded()) {
            currentStep = MixologyStep.PICKUP_POTION;
        } else {
            currentStep = MixologyStep.ADD_INGREDIENT_1; // Reset to first step on incorrect ingredients
        }
    }

    private boolean correctIngredientsAdded() {
        // Check if the correct ingredients are added to avoid progressing on incorrect ones
        return ingredient1 == currentRecipe.getIngredients().get(0).getId() &&
                ingredient2 == currentRecipe.getIngredients().get(1).getId() &&
                ingredient3 == currentRecipe.getIngredients().get(2).getId();
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectCreated) {
        GraphicsObject graphicsObject = graphicsObjectCreated.getGraphicsObject();
        if (graphicsObject.getId() == ObjectID.MATURE_DIGWEED) {
            // TODO Play a sound or something
            // 3283 	anma_puzzle_complete
        }
    }

    public void updateBestOrder() {
        // Find the best order based on priority
        bestOrderIndex = -1;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (client.getRealSkillLevel(Skill.HERBLORE) < order.getRecipe().getLevel()) {
                continue;
            }

            if (bestOrderIndex == -1) {
                bestOrderIndex = i;
                continue;
            }

            if (config.priorityType() == PriorityType.EXPERIENCE) {
                if (order.getRecipe().getExp() > orders.get(bestOrderIndex).getRecipe().getExp()) {
                    bestOrderIndex = i;
                }
            } else if (config.priorityType() == PriorityType.INGREDIENT) {
                Ingredient priorityIngredient = config.priorityIngredient();
                int currentOrderIngredientCount = order.getRecipe().getResin(priorityIngredient);
                int bestOrderIngredientCount = orders.get(bestOrderIndex).getRecipe().getResin(priorityIngredient);

                if (currentOrderIngredientCount > bestOrderIngredientCount) {
                    bestOrderIndex = i;
                } else if (currentOrderIngredientCount == bestOrderIngredientCount) {
                    // Tie-breaker: higher experience
                    if (order.getRecipe().getExp() > orders.get(bestOrderIndex).getRecipe().getExp()) {
                        bestOrderIndex = i;
                    }
                }
            }
        }

        updateInfoboxes();
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event) {
        TileObject tileObject = event.getDecorativeObject();

        if (tileObject.getId() == ObjectID.AGA_LEVER) {
            levers.put(Ingredient.AGA, tileObject);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        final TileObject tileObject = event.getGameObject();
        if (tileObject.getId() == ObjectID.MATURE_DIGWEED) {
            WorldPoint worldLocation = tileObject.getWorldLocation();
            client.setHintArrow(worldLocation);
        }

        switch (tileObject.getId()) {
            case ObjectID.CONVEYOR_BELT_54917:
                // Only add 2 conveyor belts since there's 2 with the same id
                if (conveyorBelts.size() < 2)
                    conveyorBelts.add(tileObject);
                break;
            case ObjectID.MOX_LEVER:
                levers.put(Ingredient.MOX, tileObject);
                break;
            case 55393:
                levers.put(Ingredient.AGA, tileObject);
                break;
            case ObjectID.LYE_LEVER:
                levers.put(Ingredient.LYE, tileObject);
                break;
            case 55389:
                machines.put(Process.CONCENTRATE, tileObject);
                break;
            case 55390:
                machines.put(Process.HOMOGENISE, tileObject);
                break;
            case 55391:
                machines.put(Process.CRYSTALISE, tileObject);
                break;
            case 55395:
                mixer = tileObject;
                break;
        }
    }

    public Order getbestOrder() {
        if (bestOrderIndex == -1) {
            return null;
        }

        return orders.get(bestOrderIndex);
    }

    public TileObject getLever(Ingredient ingredient) {
        return levers.get(ingredient);
    }

    public TileObject getMachine(Process process) {
        return machines.get(process);
    }

    private void updateInfoboxes() {
        if (config.showOrdersFulfilledInfobox()) {
            if (orderFulfilledInfoBox == null) {
                BufferedImage fulfilledImage = ImageUtil.loadImageResource(getClass(), "orders_completed.png");
                orderFulfilledInfoBox = new OrderFulfilledInfoBox(this, fulfilledImage, "Orders Fulfilled", potionsMade);
                infoBoxManager.addInfoBox(orderFulfilledInfoBox);
            }
            orderFulfilledInfoBox.setCount(potionsMade);
        } else if (orderFulfilledInfoBox != null) {
            infoBoxManager.removeInfoBox(orderFulfilledInfoBox);
            orderFulfilledInfoBox = null;
        }

        // Need to implement this
        if (config.showBestOrderInfobox()) {
            Order bestOrder = getbestOrder();
            if (bestOrder != null && bestOrderInfoBox == null) {
                BufferedImage bestOrderImage = ImageUtil.loadImageResource(getClass(), bestOrder.getRecipe().getIcon());
                if (bestOrderImage == null) {
                    bestOrderImage = ImageUtil.loadImageResource(getClass(), "orders_completed.png");
                }
                bestOrderInfoBox = new BestOrderInfoBox(this, bestOrderImage, bestOrder.getRecipe());
                infoBoxManager.addInfoBox(bestOrderInfoBox);
            } else if (bestOrderInfoBox != null) {
                infoBoxManager.removeInfoBox(bestOrderInfoBox);
                bestOrderInfoBox = null;
            }
        }
    }

    @Provides
    MixologyHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MixologyHelperConfig.class);
    }
}
