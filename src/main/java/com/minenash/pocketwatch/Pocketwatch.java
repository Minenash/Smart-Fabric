package com.minenash.pocketwatch;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

import com.minenash.pocketwatch.PocketwatchConfig;

import java.util.ArrayList;
import java.util.List;

public class Pocketwatch extends DrawableHelper implements ClientModInitializer {

	public static final PocketwatchConfig CONFIG = PocketwatchConfig.createAndLoad();

	private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {

		CONFIG.whitelist().replaceAll(id -> new Identifier(id).toString());
		CONFIG.subscribeToWhitelist( whitelist -> whitelist.replaceAll(id -> new Identifier(id).toString()));

		HudRenderCallback.EVENT.register(new Identifier("smart-fabric:render"), (matrices, tickDelta) -> {
			List<ItemStack> stacks = new ArrayList<>();

			hotbar_loop:
			for (int i = 9; i < 40 && stacks.size() < CONFIG.slotLimit(); i++) {
				ItemStack stack = client.player.getInventory().getStack(i);
				if (CONFIG.whitelist().contains(Registries.ITEM.getId(stack.getItem()).toString())) {
					for (ItemStack item : stacks)
						if (ItemStack.canCombine(stack, item))
							continue hotbar_loop;
					stacks.add(stack);
				}
			}

			if (stacks.isEmpty())
				return;

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
			RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);

			int slots = stacks.size();
			int baseX = client.getWindow().getScaledWidth() / 2 + (client.player.getMainArm() == Arm.RIGHT ? 99 : -119 - 18*(slots-1));
			int y = client.getWindow().getScaledHeight() - 22;

			if (FabricLoader.getInstance().getObjectShare().get("raised:distance") instanceof Integer distance)
				y -= distance;
			else if (FabricLoader.getInstance().getObjectShare().get("raised:hud") instanceof Integer distance)
				y -= distance;

			if (slots == 1)
				drawTexture(matrices, baseX-1, y, 24, 23, 22, 22);
			else {
				drawTexture(matrices, baseX-1, y, 24, 23, 21, 22);
				for (int i = 1; i < slots; i++)
					drawTexture(matrices, baseX + i*18, y, 21, 0, 18, 22);
				drawTexture(matrices, baseX + (slots)*18, y, 43, 23, 3, 22);
			}

			for (int i = 0; i < slots; i++)
				drawItem(stacks.get(i), baseX + i*18 + 2, y + 3);

		});
	}

	public void drawItem(ItemStack stack, int x, int y) {
		MatrixStack matrices = RenderSystem.getModelViewStack();
		float f = (float)stack.getBobbingAnimationTime();
		if (f > 0.0F) {
			float g = 1.0F + f / 5.0F;
			matrices.push();
			matrices.translate(x + 8, y + 12, 0.0);
			matrices.scale(1.0F / g, (g + 1.0F) / 2.0F, 1.0F);
			matrices.translate(-(x + 8), -(y + 12), 0.0);
			RenderSystem.applyModelViewMatrix();
		}

		client.getItemRenderer().renderInGuiWithOverrides(client.player, stack, x, y, 1);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		if (f > 0.0F) {
			matrices.pop();
			RenderSystem.applyModelViewMatrix();
		}

		if (CONFIG.showDetails())
			client.getItemRenderer().renderGuiItemOverlay(client.textRenderer, stack, x, y);
	}

}
