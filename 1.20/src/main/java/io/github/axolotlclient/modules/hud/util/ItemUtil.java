/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.util;

import com.mojang.blaze3d.lighting.DiffuseLighting;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

@UtilityClass
public class ItemUtil {

	public static ArrayList<ItemUtil.TimedItemStorage> removeOld(List<ItemUtil.TimedItemStorage> list, int time) {
		ArrayList<ItemUtil.TimedItemStorage> stored = new ArrayList<>();
		for (ItemUtil.TimedItemStorage storage : list) {
			if (storage.getPassedTime() <= time) {
				stored.add(storage);
			}
		}
		return stored;
	}

	public static List<ItemUtil.TimedItemStorage> untimedToTimed(List<ItemStorage> list) {
		ArrayList<TimedItemStorage> timed = new ArrayList<>();
		for (ItemStorage stack : list) {
			timed.add(stack.timed());
		}
		return timed;
	}

	public static Optional<ItemUtil.TimedItemStorage> getTimedItemFromItem(ItemStack item,
																		   List<ItemUtil.TimedItemStorage> list) {
		ItemStack compare = item.copy();
		compare.setCount(1);
		for (ItemUtil.TimedItemStorage storage : list) {
			if (storage.stack.isOf(compare.getItem())) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	public static int getTotal(MinecraftClient client, ItemStack stack) {
		List<ItemStack> item = ItemUtil.getItems(client);
		if (item == null || item.isEmpty()) {
			return 0;
		}
		List<ItemUtil.ItemStorage> items = ItemUtil.storageFromItem(item);
		Optional<ItemUtil.ItemStorage> stor = ItemUtil.getItemFromItem(stack, items);
		return stor.map(itemStorage -> itemStorage.times).orElse(0);
	}

	public static List<ItemStack> getItems(MinecraftClient client) {
		ArrayList<ItemStack> items = new ArrayList<>();
		if (client.player == null) {
			return null;
		}
		items.addAll(client.player.getInventory().armor);
		items.addAll(client.player.getInventory().offHand);
		items.addAll(client.player.getInventory().main);
		return items;
	}

	public static List<ItemStorage> storageFromItem(List<ItemStack> items) {
		ArrayList<ItemStorage> storage = new ArrayList<>();
		for (ItemStack item : items) {
			if (item.isEmpty()) {
				continue;
			}
			Optional<ItemStorage> s = getItemFromItem(item, storage);
			if (s.isPresent()) {
				ItemUtil.ItemStorage store = s.get();
				store.incrementTimes(item.getCount());
			} else {
				storage.add(new ItemUtil.ItemStorage(item, item.getCount()));
			}
		}
		return storage;
	}

	public static Optional<ItemUtil.ItemStorage> getItemFromItem(ItemStack item, List<ItemUtil.ItemStorage> list) {
		ItemStack compare = item.copy();
		compare.setCount(1);
		for (ItemUtil.ItemStorage storage : list) {
			if (storage.stack.isOf(compare.getItem())) {
				return Optional.of(storage);
			}
		}
		return Optional.empty();
	}

	/**
	 * Compares two ItemStorage Lists.
	 * If list1.get(1) is 10, and list2 is 5, it will return 5.
	 * Will return nothing if negative...
	 *
	 * @param list1 one to be based off of
	 * @param list2 one to compare to
	 * @return the compared list
	 */
	public static List<ItemStorage> compare(List<ItemStorage> list1, List<ItemStorage> list2) {
		ArrayList<ItemStorage> list = new ArrayList<>();
		for (ItemStorage current : list1) {
			Optional<ItemStorage> optional = getItemFromItem(current.stack, list2);
			if (optional.isPresent()) {
				ItemStorage other = optional.get();
				if (current.times - other.times <= 0) {
					continue;
				}
				list.add(new ItemStorage(other.stack.copy(), current.times - other.times));
			} else {
				list.add(current.copy());
			}
		}
		return list;
	}

	public void renderGuiItemModel(float scale, ItemStack stack, float x, float y) {
		MinecraftClient client = MinecraftClient.getInstance();
		BakedModel model = client.getItemRenderer().getHeldItemModel(stack, null, null, (int) (x * y));
		client.getTextureManager().getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
		RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		MatrixStack modelStack = RenderSystem.getModelViewStack();
		modelStack.push();
		modelStack.scale(scale, scale, 0);
		modelStack.translate(x, y, (100.0F + ItemRenderer.ITEM_COUNT_BLIT_OFFSET));
		modelStack.translate(8.0D, 8.0D, 0.0D);
		modelStack.scale(1.0F, -1.0F, 1.0F);
		modelStack.scale(16.0F, 16.0F, 16.0F);

		RenderSystem.applyModelViewMatrix();
		MatrixStack nextStack = new MatrixStack();

		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
			.getEntityVertexConsumers();
		boolean bl = !model.isSideLit();
		if (bl) {
			DiffuseLighting.setupFlatGuiLighting();
		}

		client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, nextStack, immediate, 15728880,
			OverlayTexture.DEFAULT_UV, model);
		immediate.draw();
		RenderSystem.enableDepthTest();
		if (bl) {
			DiffuseLighting.setup3DGuiLighting();
		}

		modelStack.pop();
		RenderSystem.applyModelViewMatrix();
	}

	public static class ItemStorage {

		public final ItemStack stack;
		public int times;

		public ItemStorage(ItemStack stack, int times) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			this.stack = copy;
			this.times = times;
		}

		public void incrementTimes(int num) {
			times = times + num;
		}

		public ItemStorage copy() {
			return new ItemStorage(stack.copy(), times);
		}

		public TimedItemStorage timed() {
			return new TimedItemStorage(stack, times);
		}
	}

	public static class TimedItemStorage extends ItemStorage {

		public float start;

		public TimedItemStorage(ItemStack stack, int times) {
			super(stack, times);
			this.start = Util.getMeasuringTimeMs();
		}

		public float getPassedTime() {
			return Util.getMeasuringTimeMs() - start;
		}

		@Override
		public void incrementTimes(int num) {
			super.incrementTimes(num);
			refresh();
		}

		public void refresh() {
			start = Util.getMeasuringTimeMs();
		}
	}
}
