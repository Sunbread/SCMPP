package top.sunbread.scmpp;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class LocationInfo {
    private World world;
    private BlockPos pos;

    public static boolean checkAndUpdate(ServerPlayerEntity player, LocationInfo loc) {
        World lastWorld = loc.world;
        BlockPos lastPos = loc.pos;
        loc.world = player.getWorld();
        loc.pos = player.getBlockPos();
        if (lastWorld == null || lastPos == null) return true;
        if (!Objects.equals(player.getWorld(), lastWorld)) return true;
        return !Objects.equals(player.getBlockPos(), lastPos);
    }

}
