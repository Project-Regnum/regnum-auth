package zooller.regnum;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Map;

public final class Lang {
    private static final Map<String, String> EN = Map.ofEntries(
            Map.entry("needRegister", "Register with /register <password> <password>"),
            Map.entry("needLogin", "Log in with /login <password>"),
            Map.entry("registered", "You have registered and logged in."),
            Map.entry("passwordShort", "Password must be at least 4 characters."),
            Map.entry("passwordMismatch", "Passwords do not match."),
            Map.entry("alreadyRegistered", "You are already registered. Use /login <password>"),
            Map.entry("notRegistered", "You are not registered. Use /register <password> <password>"),
            Map.entry("loggedIn", "You have logged in."),
            Map.entry("wrongPassword", "Wrong password."),
            Map.entry("alreadyLoggedIn", "You are already logged in."),
            Map.entry("commandBlocked", "Commands are unavailable until you log in."),
            Map.entry("chatBlocked", "Chat is unavailable until you log in.")
    );

    private static final Map<String, String> RU = Map.ofEntries(
            Map.entry("needRegister", "Зарегистрируйтесь: /register <пароль> <пароль>"),
            Map.entry("needLogin", "Войдите: /login <пароль>"),
            Map.entry("registered", "Вы зарегистрировались и вошли."),
            Map.entry("passwordShort", "Пароль должен быть не короче 4 символов."),
            Map.entry("passwordMismatch", "Пароли не совпадают."),
            Map.entry("alreadyRegistered", "Вы уже зарегистрированы. Используйте /login <пароль>"),
            Map.entry("notRegistered", "Вы не зарегистрированы. Используйте /register <пароль> <пароль>"),
            Map.entry("loggedIn", "Вы вошли."),
            Map.entry("wrongPassword", "Неверный пароль."),
            Map.entry("alreadyLoggedIn", "Вы уже вошли."),
            Map.entry("commandBlocked", "Команды недоступны, пока вы не войдёте."),
            Map.entry("chatBlocked", "Чат недоступен, пока вы не войдёте.")
    );

    private static final Map<String, String> UK = Map.ofEntries(
            Map.entry("needRegister", "Зареєструйтесь: /register <пароль> <пароль>"),
            Map.entry("needLogin", "Увійдіть: /login <пароль>"),
            Map.entry("registered", "Ви зареєструвалися та увійшли."),
            Map.entry("passwordShort", "Пароль має бути не коротшим за 4 символи."),
            Map.entry("passwordMismatch", "Паролі не збігаються."),
            Map.entry("alreadyRegistered", "Ви вже зареєстровані. Використовуйте /login <пароль>"),
            Map.entry("notRegistered", "Ви не зареєстровані. Використовуйте /register <пароль> <пароль>"),
            Map.entry("loggedIn", "Ви увійшли."),
            Map.entry("wrongPassword", "Невірний пароль."),
            Map.entry("alreadyLoggedIn", "Ви вже увійшли."),
            Map.entry("commandBlocked", "Команди недоступні, доки ви не увійдете."),
            Map.entry("chatBlocked", "Чат недоступний, доки ви не увійдете.")
    );

    private Lang() {
    }

    public static String get(ServerPlayer player, String key) {
        String code = player.clientInformation().language().toLowerCase(Locale.ROOT);
        Map<String, String> map = code.startsWith("ru") ? RU : code.startsWith("uk") ? UK : EN;
        return map.getOrDefault(key, EN.getOrDefault(key, key));
    }

    public static void send(ServerPlayer player, String key, ChatFormatting color) {
        player.sendSystemMessage(Component.literal(get(player, key)).withStyle(color));
    }
}