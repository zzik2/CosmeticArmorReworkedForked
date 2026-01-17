package lain.mods.cos.impl.platform.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Path;

public class PlatformHelperImpl {

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static Path getPlayerDataDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sendToServer(T payload) {
        PacketDistributor.sendToServer((CustomPacketPayload) payload);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sendToPlayer(ServerPlayer player, T payload) {
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) payload);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sendToAllPlayers(T payload) {
        PacketDistributor.sendToAllPlayers((CustomPacketPayload) payload);
    }

}
