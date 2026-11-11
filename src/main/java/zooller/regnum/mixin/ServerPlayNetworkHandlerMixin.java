package zooller.regnum.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zooller.regnum.AuthManager;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void teleport(double x, double y, double z, float yRot, float xRot);

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void regnum$handleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (AuthManager.isAuthenticated(player)) {
            return;
        }
        if (packet.hasPosition()) {
            teleport(player.getX(), player.getY(), player.getZ(), packet.getYRot(player.getYRot()), packet.getXRot(player.getXRot()));
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void regnum$handlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (AuthManager.isAuthenticated(player)) {
            return;
        }
        ServerboundPlayerActionPacket.Action action = packet.getAction();
        if (action == ServerboundPlayerActionPacket.Action.DROP_ITEM
                || action == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                || action == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void regnum$handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (!AuthManager.isAuthenticated(player)) {
            ci.cancel();
        }
    }
}