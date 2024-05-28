package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

public class PlayerUtil {
    public static void startAsyncRaytracer(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(MCCreativeLabExtension.getInstance(), () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                RayTraceResult rayTraceResult = onlinePlayer.rayTraceBlocks(7, FluidCollisionMode.NEVER);
                Entity entity = onlinePlayer.getTargetEntity(3, false);

                if(rayTraceResult != null)
                    onlinePlayer.setMetadata("blockRayTrace", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), rayTraceResult));
                else
                    onlinePlayer.removeMetadata("blockRayTrace", MCCreativeLabExtension.getInstance());

                if(entity != null)
                    onlinePlayer.setMetadata("targetEntity", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), entity));
                else
                    onlinePlayer.removeMetadata("targetEntity", MCCreativeLabExtension.getInstance());
            }
        }, 0L, 1L);
    }

    public static boolean hasMenuOpen(Player player){
        return player.hasMetadata("hasMenuOpen");
    }

    @Nullable
    public static Block getTargetBlock(Player player){
        if(!player.hasMetadata("blockRayTrace"))
            return null;
        RayTraceResult rayTraceResult = (RayTraceResult) player.getMetadata("blockRayTrace").get(0).value();
        if(rayTraceResult != null && rayTraceResult.getHitBlock() != null)
            return rayTraceResult.getHitBlock();
        return null;
    }

    @Nullable
    public static Entity getTargetEntity(Player player){
        if(!player.hasMetadata("targetEntity"))
            return null;
        return (Entity) player.getMetadata("targetEntity").get(0).value();
    }
}
