package zooller.regnum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthManager {
    public static class Account {
        public String salt;
        public String hash;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<UUID> AUTHENTICATED = ConcurrentHashMap.newKeySet();
    private static final Map<String, Account> ACCOUNTS = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static Path file;

    private AuthManager() {
    }

    public static void load() {
        file = FabricLoader.getInstance().getConfigDir().resolve("regnum-auth").resolve("accounts.json");
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            Type type = new TypeToken<Map<String, Account>>() {
            }.getType();
            Map<String, Account> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                ACCOUNTS.putAll(loaded);
            }
        } catch (IOException e) {
            RegnumAuth.LOGGER.error("Failed to load accounts", e);
        }
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(ACCOUNTS, writer);
            }
        } catch (IOException e) {
            RegnumAuth.LOGGER.error("Failed to save accounts", e);
        }
    }

    private static String hash(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 20000, 256);
            byte[] result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isRegistered(ServerPlayer player) {
        return ACCOUNTS.containsKey(player.getUUID().toString());
    }

    public static boolean isAuthenticated(ServerPlayer player) {
        return AUTHENTICATED.contains(player.getUUID());
    }

    public static boolean isBlocked(Player player) {
        return player instanceof ServerPlayer serverPlayer && !isAuthenticated(serverPlayer);
    }

    public static void logout(ServerPlayer player) {
        AUTHENTICATED.remove(player.getUUID());
    }

    public static void register(ServerPlayer player, String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        Account account = new Account();
        account.salt = Base64.getEncoder().encodeToString(salt);
        account.hash = hash(password, salt);
        ACCOUNTS.put(player.getUUID().toString(), account);
        AUTHENTICATED.add(player.getUUID());
        save();
    }

    public static boolean login(ServerPlayer player, String password) {
        Account account = ACCOUNTS.get(player.getUUID().toString());
        if (account == null) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(account.salt);
        byte[] expected = account.hash.getBytes();
        byte[] actual = hash(password, salt).getBytes();
        if (MessageDigest.isEqual(expected, actual)) {
            AUTHENTICATED.add(player.getUUID());
            return true;
        }
        return false;
    }
}