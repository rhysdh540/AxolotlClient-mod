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

package io.github.axolotlclient.mixin;

import io.github.axolotlclient.modules.blur.MenuBlur;
import io.github.axolotlclient.modules.screenshotUtils.ScreenshotUtils;
import io.github.axolotlclient.modules.scrollableTooltips.ScrollableTooltips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Screen.class)
public abstract class ScreenMixin {

	@ModifyArgs(method = "renderWithTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Lnet/minecraft/client/gui/tooltip/TooltipPositioner;II)V"))
	public void axolotlclient$modifyTooltipPosition(Args args) {
		if (ScrollableTooltips.getInstance().enabled.get()) {
			if ((MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen)
				&& ((CreativeInventoryScreen) MinecraftClient.getInstance().currentScreen).isInventoryOpen()) {
				return;
			}

			args.set(3, (int) args.get(3) + ScrollableTooltips.getInstance().tooltipOffsetX);
			args.set(4, (int) args.get(4) + ScrollableTooltips.getInstance().tooltipOffsetY);
		}
	}

	@Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/ClickEvent;getAction()Lnet/minecraft/text/ClickEvent$Action;", ordinal = 0), cancellable = true)
	public void axolotlclient$customClickEvents(Style style, CallbackInfoReturnable<Boolean> cir) {
		ClickEvent event = style.getClickEvent();
		if (event instanceof ScreenshotUtils.CustomClickEvent) {
			((ScreenshotUtils.CustomClickEvent) event).doAction();
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V"), cancellable = true)
	private void axolotlclient$menuBlur(GuiGraphics graphics, CallbackInfo ci) {
		if (MenuBlur.getInstance().renderScreen(graphics)) {
			ci.cancel();
		}
	}
}
