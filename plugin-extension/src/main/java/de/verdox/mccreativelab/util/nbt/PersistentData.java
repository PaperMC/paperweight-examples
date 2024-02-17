package de.verdox.mccreativelab.util.nbt;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class PersistentData<T extends PersistentDataHolder> implements NBTSerializable {
    /**
     * Gets the persistent data. After editing, you should always trigger a save manually.
     *
     * @param type                 - The class type
     * @param persistentDataHolder - The persistent data holder
     * @param <B>                  - The persistent data type
     * @param <T>                  - The Persistent Data type
     * @return - The persistent data
     */
    @NotNull
    public static <B extends PersistentDataHolder, T extends PersistentData<B>> T get(Class<? extends T> type, B persistentDataHolder) {
        if (persistentDataHolder instanceof ItemMeta)
            throw new IllegalArgumentException("ItemStacks or ItemMeta is not supported by this because of their immutability.");
        final String metadataKey = "persistent_data_" + type.getName();
        T persistentData;

        if (persistentDataHolder instanceof Metadatable metadatable && metadatable.hasMetadata(metadataKey)) {
            persistentData = type.cast(metadatable.getMetadata(metadataKey).get(0).value());
            Objects.requireNonNull(persistentData);
        }
        else {
            try {
                persistentData = createPersistentDataObject(type);
                persistentData.loadFromStorage(persistentDataHolder);
                persistentData.setup(persistentDataHolder);
                if (persistentDataHolder instanceof Metadatable metadatable) {
                    metadatable.setMetadata(metadataKey, new FixedMetadataValue(MCCreativeLabExtension.getInstance(), persistentData));

                    Set<T> cached = getAllCachedData(persistentDataHolder);
                    cached.add(persistentData);
                    if (!metadatable.hasMetadata("cached_persistent_data"))
                        metadatable.setMetadata("cached_persistent_data", new FixedMetadataValue(MCCreativeLabExtension.getInstance(), cached));
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
        return persistentData;
    }

    @NotNull private static <B extends PersistentDataHolder, T extends PersistentData<B>> T createPersistentDataObject(Class<? extends T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            Bukkit.getLogger().warning("Standard constructor not found in PersistentData class " + type.getName());
            throw new RuntimeException(e);
        }
    }

    public static <B extends PersistentDataHolder, T extends PersistentData<B>> Set<T> getAllCachedData(B persistentDataHolder) {
        if (!(persistentDataHolder instanceof Metadatable metadatable))
            return new HashSet<>();

        if (!metadatable.hasMetadata("cached_persistent_data"))
            return new HashSet<>();
        return (Set<T>) metadatable.getMetadata("cached_persistent_data").get(0).value();
    }

    static <B extends PersistentDataHolder> void clearCache(B persistentDataHolder) {
        if (!(persistentDataHolder instanceof Metadatable metadatable))
            return;

        metadatable.removeMetadata("cached_persistent_data", MCCreativeLabExtension.getInstance());
    }

    public final void loadFromStorage(T dataHolder) {
        NBTContainer nbtContainer = NBTContainer.of("fixedminecraft", dataHolder.getPersistentDataContainer());
        if(!nbtContainer.has(nbtKey().toLowerCase(Locale.ROOT)))
            return;
        NBTContainer load = nbtContainer.getNBTContainer(nbtKey().toLowerCase(Locale.ROOT));
        this.loadNBTData(load);
    }

    public final void save(T dataHolder) {
        NBTContainer nbtContainer = NBTContainer.of("fixedminecraft", dataHolder.getPersistentDataContainer());
        NBTContainer save = nbtContainer.createNBTContainer();
        this.saveNBTData(save);
        nbtContainer.set(nbtKey().toLowerCase(Locale.ROOT), save);
    }

    void setup(T persistentDataHolder) {
    }

    protected abstract String nbtKey();
}
