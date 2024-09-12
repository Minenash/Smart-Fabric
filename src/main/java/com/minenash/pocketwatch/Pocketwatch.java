package com.minenash.pocketwatch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

import com.minenash.pocketwatch.PocketwatchConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Pocketwatch implements ClientModInitializer {

	public static final PocketwatchConfig CONFIG = PocketwatchConfig.createAndLoad();

	private static final Identifier HOTBAR_TEXTURE = Identifier.of("textures/gui/sprites/hud/hotbar.png");
	private static final Identifier OFFHAND_TEXTURE = Identifier.of("textures/gui/sprites/hud/hotbar_offhand_left.png");
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {

		CONFIG.whitelist().replaceAll(id -> Identifier.of(id).toString());
		CONFIG.subscribeToWhitelist( whitelist -> whitelist.replaceAll(id -> Identifier.of(id).toString()));

		HudRenderCallback.EVENT.register(Identifier.of("pocketwatch:render"), (context, tickDelta) -> {
			if (client.options.hudHidden)
				return;
			List<ItemStack> stacks = new ArrayList<>();

			for (int i = 0; i < 40 && stacks.size() < CONFIG.slotLimit(); i++) {
				ItemStack stack = client.player.getInventory().getStack(i);
				if (i >= 9)
					addToList(stacks, stack);

				var bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
				if (bundle != null)
                    for (Iterator<ItemStack> it = bundle.iterate().iterator(); it.hasNext() && stacks.size() < CONFIG.slotLimit(); )
                        addToList(stacks, it.next());

				var container = stack.get(DataComponentTypes.CONTAINER);
				if (container != null)
					for (Iterator<ItemStack> it = container.iterateNonEmpty().iterator(); it.hasNext() && stacks.size() < CONFIG.slotLimit(); )
						addToList(stacks, it.next());

			}

			if (stacks.isEmpty())
				return;

			int slots = stacks.size();
			int baseX = client.getWindow().getScaledWidth() / 2 + (client.player.getMainArm() == Arm.RIGHT ? 99 : -119 - 18*(slots-1)) + CONFIG.xOffset();
			int y = client.getWindow().getScaledHeight() - 22 + CONFIG.yOffset();

			if (FabricLoader.getInstance().getObjectShare().get("raised:distance") instanceof Integer distance) {
				y -= distance;
			}
			else if (FabricLoader.getInstance().getObjectShare().get("raised:hud") instanceof Integer distance) {
				y -= distance;
			}

			if (slots == 1)
				context.drawTexture(OFFHAND_TEXTURE, baseX-1, y, 0, 1, 22, 22, 29, 24);
			else {
				context.drawTexture(OFFHAND_TEXTURE, baseX-1, y, 0, 1, 21, 22, 29, 24);
				for (int i = 1; i < slots; i++)
					context.drawTexture(HOTBAR_TEXTURE, baseX + i*18, y, 21, 0, 18, 22, 182, 22);
				context.drawTexture(OFFHAND_TEXTURE, baseX + (slots)*18, y, 19, 1, 3, 22, 29, 24);
			}

			for (int i = 0; i < slots; i++)
				drawItem(context, stacks.get(i), baseX + i*18 + 2, y + 3);

		});
	}

	public void addToList(List<ItemStack> stacks, ItemStack stack) {
		if (CONFIG.whitelist().contains(Registries.ITEM.getId(stack.getItem()).toString())) {
			for (ItemStack item : stacks)
				if (ItemStack.areItemsAndComponentsEqual(stack, item))
					return;
			stacks.add(stack.copy());
		}
	}

	public void drawItem(DrawContext context, ItemStack stack, int x, int y) {
		float f = (float)stack.getBobbingAnimationTime();
		if (f > 0.0F) {
			float g = 1.0F + f / 5.0F;
			context.getMatrices().push();
			context.getMatrices().translate(x + 8, y + 12, 0.0);
			context.getMatrices().scale(1.0F / g, (g + 1.0F) / 2.0F, 1.0F);
			context.getMatrices().translate(-(x + 8), -(y + 12), 0.0);
		}
		context.drawItem(stack, x, y);
		if (f > 0.0F)
			context.getMatrices().pop();

		if (CONFIG.showDetails())
			context.drawItemInSlot(client.textRenderer, stack, x, y);
	}

}
