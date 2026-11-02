package dev.emilian.elytraswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class ElytraSwapMod implements ClientModInitializer {

    private static KeyBinding swapToggleKey;
    private static KeyBinding autopilotToggleKey;

    public static boolean swapActive = false;
    public static boolean midSwap = false;
    private int tickCounter = 0;
    private int swapPhase = 0;
    private boolean wasGliding = false;

    private static final int SWAP_INTERVAL = 14;
    private static final int CHESTPLATE_SLOT = 7;
    private static final int CHEST_ARMOR_SLOT_INDEX = 6;

    private static Class<?> autopilotClass = null;
    private static Field autoFlightField = null;
    private static Field isDescendingField = null;
    private static Field pitchModField = null;
    private static Field calculateHudField = null;
    private static boolean reflectionFailed = false;

    @Override
    public void onInitializeClient() {
        swapToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elytraswap.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.Category.MISC
        ));

        autopilotToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.elytraswap.autopilot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        handleKeybinds(client);

        if (swapActive) {
            tickSwap(client);
        }
    }

    private void handleKeybinds(MinecraftClient client) {
        if (swapToggleKey.wasPressed()) {
            swapActive = !swapActive;
            tickCounter = 0;
            swapPhase = 0;
            midSwap = false;
            wasGliding = false;
            client.player.sendMessage(Text.literal(
                    swapActive ? "§aDurability Swap: ON" : "§cDurability Swap: OFF"), true);
        }

        if (autopilotToggleKey.wasPressed()) {
            toggleAutopilotDirect(client);
        }
    }

    private void tickSwap(MinecraftClient client) {
        switch (swapPhase) {
            case 1:
                // Tick 2: swap elytra back on + press space
                doSwap(client);
                client.options.jumpKey.setPressed(true);
                swapPhase = 2;
                break;
            case 2:
                // Tick 3: release space
                client.options.jumpKey.setPressed(false);
                swapPhase = 0;
                break;
            case 0:
                if (client.player.isGliding()) {
                    wasGliding = true;
                }
                if (!wasGliding) return;

                tickCounter++;
                if (tickCounter >= SWAP_INTERVAL) {
                    tickCounter = 0;
                    // Tick 1: swap elytra out
                    doSwap(client);
                    swapPhase = 1;
                }
                break;
        }
    }

    private void doSwap(MinecraftClient client) {
        int syncId = client.player.currentScreenHandler.syncId;
        client.interactionManager.clickSlot(syncId, CHEST_ARMOR_SLOT_INDEX, CHESTPLATE_SLOT, SlotActionType.SWAP, client.player);
    }

    private void toggleAutopilotDirect(MinecraftClient client) {
        if (!initReflection()) {
            client.player.sendMessage(Text.literal("§cAutopilot mod not found"), true);
            return;
        }

        try {
            boolean current = autoFlightField.getBoolean(null);
            if (current) {
                autoFlightField.setBoolean(null, false);
                client.player.sendMessage(Text.literal("§cAutopilot: OFF"), true);
            } else {
                autoFlightField.setBoolean(null, true);
                isDescendingField.setBoolean(null, true);
                pitchModField.setDouble(null, 3.0);
                calculateHudField.setBoolean(null, true);
                client.player.sendMessage(Text.literal("§aAutopilot: ON"), true);
            }
        } catch (Exception e) {
            client.player.sendMessage(Text.literal("§cAutopilot toggle failed"), true);
        }
    }

    private boolean initReflection() {
        if (reflectionFailed) return false;
        if (autopilotClass != null) return true;

        try {
            autopilotClass = Class.forName("net.elytraautopilot.ElytraAutoPilot");
            autoFlightField = autopilotClass.getDeclaredField("autoFlight");
            autoFlightField.setAccessible(true);
            isDescendingField = autopilotClass.getDeclaredField("isDescending");
            isDescendingField.setAccessible(true);
            pitchModField = autopilotClass.getDeclaredField("pitchMod");
            pitchModField.setAccessible(true);
            calculateHudField = autopilotClass.getDeclaredField("calculateHud");
            calculateHudField.setAccessible(true);
            return true;
        } catch (Exception e) {
            reflectionFailed = true;
            return false;
        }
    }
}
