package lain.mods.cos.impl.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class PlatformHelper {

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isClient() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static MinecraftServer getServer() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getPlayerDataDir(MinecraftServer server) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T> void sendToServer(T payload) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T> void sendToPlayer(ServerPlayer player, T payload) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T> void sendToAllPlayers(T payload) {
        throw new AssertionError();
    }

}
