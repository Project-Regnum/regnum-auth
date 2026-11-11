package zooller.regnum.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zooller.regnum.AuthManager;

@Mixin(AbstractContainerMenu.class)
public abstract class ScreenHandlerMixin {
    @Shadow
    public abstract void sendAllDataToRemote();

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void regnum$clicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer && !AuthManager.isAuthenticated(serverPlayer)) {
            sendAllDataToRemote();
            ci.cancel();
        }
    }
}