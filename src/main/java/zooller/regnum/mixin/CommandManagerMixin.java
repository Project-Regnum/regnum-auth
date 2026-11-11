package zooller.regnum.mixin;

import com.mojang.brigadier.ParseResults;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zooller.regnum.AuthManager;
import zooller.regnum.Lang;

import java.util.Locale;

@Mixin(Commands.class)
public class CommandManagerMixin {
    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void regnum$performCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        ServerPlayer player = parseResults.getContext().getSource().getPlayer();
        if (player == null || AuthManager.isAuthenticated(player)) {
            return;
        }
        String name = command.startsWith("/") ? command.substring(1) : command;
        int space = name.indexOf(' ');
        String root = (space < 0 ? name : name.substring(0, space)).toLowerCase(Locale.ROOT);
        if (root.equals("login") || root.equals("register")) {
            return;
        }
        Lang.send(player, "commandBlocked", ChatFormatting.RED);
        ci.cancel();
    }
}