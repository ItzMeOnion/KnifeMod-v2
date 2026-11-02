package com.knifemod.client;

import com.knifemod.KnifeDef;
import com.knifemod.KnifeItems;
import com.knifemod.KnifeMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KnifeClient implements ClientModInitializer {
    private static final String CATEGORY = "category.knifemod";

    private static KeyBinding openMenu;
    private static KeyBinding spinKey;
    private static KeyBinding inspectKey;

    @Override
    public void onInitializeClient() {
        // All three are rebindable under Options -> Controls -> Knife Cases
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.knifemod.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
        spinKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.knifemod.spin", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));
        inspectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.knifemod.inspect", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                client.setScreen(new CaseScreen());
            }
            if (client.player == null) return;

            // Spin and inspect only work with an empty main hand
            boolean handEmpty = client.player.getMainHandStack().isEmpty();
            if (handEmpty && spinKey.wasPressed()) KnifeState.play(1);
            if (handEmpty && inspectKey.wasPressed()) KnifeState.play(2);
            if (!handEmpty) KnifeState.mode = 0;
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
                Identifier.of(KnifeMod.MOD_ID, "knife_hand"), KnifeClient::renderHand);
    }

    private static void renderHand(DrawContext ctx, RenderTickCounter tickCounter) {
        if (KnifeState.mode == 0 || KnifeState.equipped == null) return;

        long elapsed = System.currentTimeMillis() - KnifeState.start;
        boolean spin = KnifeState.mode == 1;
        if (elapsed > (spin ? 2500 : 6000)) {
            KnifeState.mode = 0;
            return;
        }
        float t = elapsed / 1000f;

        MinecraftClient client = MinecraftClient.getInstance();
        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();
        KnifeDef knife = KnifeState.equipped;
        ItemStack stack = KnifeItems.createStack(knife);
        int color = 0xFF000000 | knife.rarity().color();

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        if (spin) {
            // Fake 3D spin: squash the sprite horizontally with cos(angle), so it flips edge-on and back
            float cos = (float) Math.cos(t * 9f);
            matrices.translate(w - 50f, h - 44f);
            matrices.scale(3f * cos, 3f);
            ctx.drawItem(stack, -8, -8);
        } else {
            float cos = (float) Math.cos(t * 4f);
            matrices.translate(w / 2f, h / 2f);
            matrices.scale(6f * cos, 6f);
            ctx.drawItem(stack, -8, -8);
        }
        matrices.popMatrix();

        if (spin) {
            ctx.drawCenteredTextWithShadow(client.textRenderer, Text.literal(knife.name()), w - 50, h - 78, color);
        } else {
            ctx.drawCenteredTextWithShadow(client.textRenderer, Text.literal(knife.name()), w / 2, h / 2 + 60, color);
        }
    }
}
