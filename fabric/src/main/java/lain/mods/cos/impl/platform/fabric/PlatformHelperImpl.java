package lain.mods.cos.impl.platform.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class PlatformHelperImpl {

    private static MinecraftServer currentServer;

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static MinecraftServer getServer() {
        return currentServer;
    }

    public static void setServer(MinecraftServer server) {
        currentServer = server;
    }

    public static Path getPlayerDataDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sendToServer(T payload) {
        ClientPlayNetworking.send((CustomPacketPayload) payload);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sendToPlayer(ServerPlayer player, T payload) {
        ServerPlayNetworking.send(player, (CustomPacketPayload) payload);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sendToAllPlayers(T payload) {
        if (currentServer != null) {
            for (ServerPlayer player : currentServer.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, (CustomPacketPayload) payload);
            }
        }
    }

}
