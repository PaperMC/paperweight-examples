package de.verdox.mccreativelab.util.nbt;

import de.verdox.mccreativelab.MCCreativeLabExtension;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class PersistentData<T extends PersistentDataHolder> implements NBTSerializable {
    private static final Map<Class<? extends PersistentData<?>>, Function<PersistentDataHolder, PersistentData<?>>> registered = new ConcurrentHashMap<>();
    static final Set<Class<? extends ChunkPersistentData>> chunkPersistentDataClasses = new HashSet<>();
    static final Set<Class<? extends EntityPersistentData>> entityPersistentDataClasses = new HashSet<>();
    static final Set<Class<? extends PlayerPersistentData>> playerPersistentDataClasses = new HashSet<>();
    static final Set<Class<? extends WorldPersistentData>> worldPersistentDataClasses = new HashSet<>();
    /**
     *
     * @param type - The class type
     * @param creator - A creator function
     * @param <B> - The persistent data type
     * @param <T> - The Persistent Data type
     */
    public static <B extends PersistentDataHolder, T extends PersistentData<B>> void registerPersistentData(Class<? extends T> type, Function<? extends B, ? extends T> creator){
        if(ChunkPersistentData.class.isAssignableFrom(type))
            chunkPersistentDataClasses.add((Class<? extends ChunkPersistentData>) type);
        else if(EntityPersistentData.class.isAssignableFrom(type))
            entityPersistentDataClasses.add((Class<? extends EntityPersistentData>) type);
        else if(PlayerPersistentData.class.isAssignableFrom(type))
            playerPersistentDataClasses.add((Class<? extends PlayerPersistentData>) type);
        else if(WorldPersistentData.class.isAssignableFrom(type))
            worldPersistentDataClasses.add((Class<? extends WorldPersistentData>) type);
        else
            throw new IllegalArgumentException("Type "+type+" does not extend allowed persistent data types");

        registered.put(type, (Function<PersistentDataHolder, PersistentData<?>>) creator);
    }
    /**
     * Edits the persistent data and returns any value. After editing, you should always trigger a save manually.
     * @param type - The class type
     * @param persistentDataHolder - The persistent data holder
     * @return Any value returned
     * @param <X> - The return value type
     * @param <B> - The persistent data type
     * @param <T> - The Persistent Data type
     */
    public static <B extends PersistentDataHolder, T extends PersistentData<B>, X> X load(Class<? extends T> type, B persistentDataHolder, Function<T, X> editor) {
        T persistentData = get(type, persistentDataHolder);
        X result = editor.apply(persistentData);
        persistentData.save();
        return result;
    }
    /**
     * Edits the persistent data. After editing, you should always trigger a save manually.
     * @param type - The class type
     * @param persistentDataHolder - The persistent data holder
     * @param <B> - The persistent data type
     * @param <T> - The Persistent Data type
     */
    public static <B extends PersistentDataHolder, T extends PersistentData<B>> void load(Class<? extends T> type, B persistentDataHolder, Consumer<T> editor) {
        load(type, persistentDataHolder, t -> {
            editor.accept(t);
            return null;
        });
    }

    /**
     * Gets the persistent data. After editing, you should always trigger a save manually.
     * @param type - The class type
     * @param persistentDataHolder - The persistent data holder
     * @return - The persistent data
     * @param <B> - The persistent data type
     * @param <T> - The Persistent Data type
     */
    public static <B extends PersistentDataHolder, T extends PersistentData<B>> T get(Class<? extends T> type, B persistentDataHolder){
        if(persistentDataHolder instanceof ItemMeta)
            throw new IllegalArgumentException("ItemStacks or ItemMeta is not supported by this because of their immutability.");
        final String metadataKey = "persistent_data_"+type.getName();
        T persistentData;

        if(persistentDataHolder instanceof Metadatable metadatable && metadatable.hasMetadata(metadataKey))
            persistentData = type.cast(metadatable.getMetadata(metadataKey).get(0).value());
        else {
            if(!registered.containsKey(type))
                throw new IllegalArgumentException(type+" not registered");

            persistentData = type.cast(registered.get(type).apply(persistentDataHolder));
            if(persistentDataHolder instanceof Metadatable metadatable)
                metadatable.setMetadata(metadataKey, new FixedMetadataValue(MCCreativeLabExtension.getInstance(), persistentData));
        }
        return persistentData;
    }

    private final T persistentDataHolder;

    protected PersistentData(T persistentDataHolder) {
        this.persistentDataHolder = persistentDataHolder;
        this.loadNBTData(NBTContainer.of("fixedminecraft", persistentDataHolder.getPersistentDataContainer()).getOrCreateNBTContainer(nbtKey().toLowerCase(Locale.ROOT)));
    }

    public T getPersistentDataHolder() {
        return persistentDataHolder;
    }

    public void save() {
        this.saveNBTData(NBTContainer.of("fixedminecraft", persistentDataHolder.getPersistentDataContainer()).getOrCreateNBTContainer(nbtKey().toLowerCase(Locale.ROOT)));
    }

    protected abstract String nbtKey();
}
