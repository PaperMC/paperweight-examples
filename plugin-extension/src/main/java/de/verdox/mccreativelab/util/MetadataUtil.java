package de.verdox.mccreativelab.util;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.concurrent.TimeUnit;

public class MetadataUtil {
    public static void addTemporaryMetaData(Metadatable metadatable, String key, MetadataValue metadataValue, long time, TimeUnit timeUnit) {
        ScheduledTask scheduledTask = Bukkit.getAsyncScheduler().runDelayed(MCCreativeLabExtension.getInstance(), task -> {
            metadatable.removeMetadata(key, metadataValue.getOwningPlugin());
            removeRemovalTask(metadatable, key);
        }, time, timeUnit);
        metadatable.setMetadata(key, metadataValue);
        addRemovalTask(metadatable, key, scheduledTask);
    }

    private static void addRemovalTask(Metadatable metadatable, String key, ScheduledTask scheduledTask) {
        removeRemovalTask(metadatable, key);
        metadatable.setMetadata(key + "_temporaryDataTask", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), scheduledTask));
    }

    private static void removeRemovalTask(Metadatable metadatable, String key) {
        if (!metadatable.hasMetadata(key + "_temporaryDataTask"))
            return;
        ((ScheduledTask) metadatable.getMetadata(key + "_temporaryDataTask").get(0).value()).cancel();
        metadatable.removeMetadata(key + "_temporaryDataTask", MCCreativeLabExtension.getInstance());
    }
}
