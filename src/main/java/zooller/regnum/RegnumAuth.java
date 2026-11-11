package zooller.regnum;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegnumAuth implements ModInitializer {
	public static final String MOD_ID = "regnum-auth";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AuthManager.load();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> AuthCommands.register(dispatcher));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.player;
			AuthManager.logout(player);
			if (AuthManager.isRegistered(player)) {
				Lang.send(player, "needLogin", ChatFormatting.YELLOW);
			} else {
				Lang.send(player, "needRegister", ChatFormatting.YELLOW);
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> AuthManager.logout(handler.player));

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
				AuthManager.isBlocked(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
				AuthManager.isBlocked(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
				AuthManager.isBlocked(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
				AuthManager.isBlocked(player) ? InteractionResult.FAIL : InteractionResult.PASS);

		UseItemCallback.EVENT.register((player, world, hand) ->
				AuthManager.isBlocked(player)
						? InteractionResultHolder.fail(player.getItemInHand(hand))
						: InteractionResultHolder.pass(ItemStack.EMPTY));

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> !AuthManager.isBlocked(player));

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
				!(entity instanceof ServerPlayer player && !AuthManager.isAuthenticated(player)));

		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			if (AuthManager.isAuthenticated(sender)) {
				return true;
			}
			Lang.send(sender, "chatBlocked", ChatFormatting.RED);
			return false;
		});
	}
}