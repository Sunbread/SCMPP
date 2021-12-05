package top.sunbread.scmpp;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.MapColor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.random.ChunkRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class Scmpp implements ModInitializer {
    public static final int MAP_ID = -114514;
    public static final byte BACKGROUND_COLOR = (byte) (MapColor.GRAY.id * 4 + MapColor.Brightness.NORMAL.id);
    public static final byte PLAYER_CHUNK_COLOR = (byte) (MapColor.BRIGHT_RED.id * 4 + MapColor.Brightness.NORMAL.id);
    public static final byte SLIME_CHUNK_COLOR = (byte) (MapColor.LIME.id * 4 + MapColor.Brightness.NORMAL.id);
    public static final byte PLAYER_SLIME_CHUNK_COLOR = (byte) (MapColor.YELLOW.id * 4 + MapColor.Brightness.NORMAL.id);
    public static final byte NORMAL_CHUNK_COLOR_1 = (byte) (MapColor.LIGHT_GRAY.id * 4 + MapColor.Brightness.NORMAL.id);
    public static final byte NORMAL_CHUNK_COLOR_2 = (byte) (MapColor.WHITE.id * 4 + MapColor.Brightness.NORMAL.id);
    public static final Logger LOGGER = LogManager.getLogger("SlimeChunkMap++");

    @Override
    public void onInitialize() {
        LOGGER.info("Mod loaded successfully");
    }

    public static void noticePlayer(ServerPlayerEntity player, ServerWorld world) {
        sendMapPacket(player, generatePattern(player, world));
    }

    private static byte[] generatePattern(PlayerEntity player, ServerWorld world) {
        byte[] pattern = new byte[16384];
        Arrays.fill(pattern, BACKGROUND_COLOR);
        if (world.getBiome(player.getBlockPos()).getSpawnSettings().getSpawnEntries(SpawnGroup.MONSTER).getEntries().
                stream().noneMatch(it -> it.type == EntityType.SLIME)) return pattern;
        ChunkPos playerChunkPos = player.getChunkPos();
        final int xBias = 4;
        final int yBias = 4;
        final int width = 8;
        final int height = 8;
        for (int i = -7; i <= 7; ++i) {
            for (int j = -7; j <= 7; ++j) {
                int chunkX = playerChunkPos.x + i;
                int chunkZ = playerChunkPos.z + j;
                byte color;
                if (isSlimeChunk(world, chunkX, chunkZ)) color = SLIME_CHUNK_COLOR;
                else if ((chunkX + chunkZ) % 2 != 0) color = NORMAL_CHUNK_COLOR_1;
                else color = NORMAL_CHUNK_COLOR_2;
                if (i == 0 && j == 0) {
                    if (color == SLIME_CHUNK_COLOR) color = PLAYER_SLIME_CHUNK_COLOR;
                    else color = PLAYER_CHUNK_COLOR;
                }
                paintRectangle(pattern, color, xBias + (i + 7) * width, yBias + (j + 7) * height, width, height);
            }
        }
        return pattern;
    }

    private static void paintRectangle(byte[] pattern, byte color, int x, int y, int width, int height) {
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j)
                pattern[pos2index(x + i, y + j)] = color;
    }

    private static int pos2index(int x, int y) {
        return x + y * 128;
    }

    private static boolean isSlimeChunk(StructureWorldAccess world, int chunkX, int chunkZ) {
        return ChunkRandom.getSlimeRandom(chunkX, chunkZ, world.getSeed(), 987234911L).nextInt(10) == 0;
    }

    private static void sendMapPacket(ServerPlayerEntity player, byte[] colors) {
        if (colors.length != 16384) return;
        player.networkHandler.sendPacket(new MapUpdateS2CPacket(MAP_ID, (byte) 0, true, null,
                new MapState.UpdateData(0, 0, 128, 128, colors)));
    }

}
