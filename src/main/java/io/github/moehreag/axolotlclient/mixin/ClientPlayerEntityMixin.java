package io.github.moehreag.axolotlclient.mixin;

import io.github.moehreag.axolotlclient.modules.hud.HudManager;
import io.github.moehreag.axolotlclient.modules.hud.gui.hud.ToggleSprintHud;
import net.minecraft.client.network.ClientPlayerEntity;
//import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    /**
     * @author DragonEggBedrockBreaking
     * @param sprintKey the sprint key that the user has bound
     * @return whether or not the user should try to sprint
     */
    @Redirect(
            method = "tickMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/options/KeyBinding;isPressed()Z"
            )
    )
    private boolean alwaysPressed(KeyBinding sprintKey) {
        ToggleSprintHud hud = (ToggleSprintHud) HudManager.getINSTANCE().get(ToggleSprintHud.ID);
        return hud.sprintToggled || sprintKey.isPressed();
    }
}
