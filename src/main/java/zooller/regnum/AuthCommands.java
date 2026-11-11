package zooller.regnum;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class AuthCommands {
    private AuthCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("register")
                .then(Commands.argument("password", StringArgumentType.word())
                        .then(Commands.argument("confirm", StringArgumentType.word())
                                .executes(AuthCommands::executeRegister))));

        dispatcher.register(Commands.literal("login")
                .then(Commands.argument("password", StringArgumentType.word())
                        .executes(AuthCommands::executeLogin)));
    }

    private static int executeRegister(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String password = StringArgumentType.getString(context, "password");
        String confirm = StringArgumentType.getString(context, "confirm");

        if (AuthManager.isRegistered(player)) {
            Lang.send(player, "alreadyRegistered", ChatFormatting.RED);
            return 0;
        }
        if (password.length() < 4) {
            Lang.send(player, "passwordShort", ChatFormatting.RED);
            return 0;
        }
        if (!password.equals(confirm)) {
            Lang.send(player, "passwordMismatch", ChatFormatting.RED);
            return 0;
        }
        AuthManager.register(player, password);
        Lang.send(player, "registered", ChatFormatting.GREEN);
        return 1;
    }

    private static int executeLogin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String password = StringArgumentType.getString(context, "password");

        if (AuthManager.isAuthenticated(player)) {
            Lang.send(player, "alreadyLoggedIn", ChatFormatting.RED);
            return 0;
        }
        if (!AuthManager.isRegistered(player)) {
            Lang.send(player, "notRegistered", ChatFormatting.RED);
            return 0;
        }
        if (AuthManager.login(player, password)) {
            Lang.send(player, "loggedIn", ChatFormatting.GREEN);
            return 1;
        }
        Lang.send(player, "wrongPassword", ChatFormatting.RED);
        return 0;
    }
}