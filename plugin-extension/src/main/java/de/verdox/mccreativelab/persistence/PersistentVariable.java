package de.verdox.mccreativelab.persistence;

import de.verdox.mccreativelab.util.nbt.NBTContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

public abstract class PersistentVariable<B> {
    private final NamespacedKey persistenceKey;

    PersistentVariable(String namespace, String key) {
        this.persistenceKey = new NamespacedKey(namespace, key);
    }

    public boolean has(PersistentDataHolder persistentDataHolder) {
        return persistentDataHolder.getPersistentDataContainer().has(persistenceKey);
    }

    @Nullable
    public abstract B read(PersistentDataContainer persistentDataContainer);

    @Nullable
    public final B read(PersistentDataHolder persistentDataHolder) {
        return read(persistentDataHolder.getPersistentDataContainer());
    }

    public final void edit(PersistentDataContainer persistentDataContainer, Function<B, B> editFunction) {
        B value = read(persistentDataContainer);
        B newValue = editFunction.apply(value);
        set(persistentDataContainer, newValue);
    }

    public abstract void set(PersistentDataContainer persistentDataContainer, @Nullable B data);

    public final void set(PersistentDataHolder persistentDataHolder, @Nullable B data) {
        set(persistentDataHolder.getPersistentDataContainer(), data);
    }

    public NamespacedKey getPersistenceKey() {
        return persistenceKey;
    }

    public static class NativePersistentVariable<Z, B> extends PersistentVariable<B> {
        private final PersistentDataType<Z, B> type;
        private final B defaultValue;

        NativePersistentVariable(String namespace, String key, PersistentDataType<Z, B> type, B defaultValue) {
            super(namespace, key);
            this.type = type;
            this.defaultValue = defaultValue;
        }

        @Override
        @Nullable
        public B read(PersistentDataContainer persistentDataContainer) {
            if (!persistentDataContainer.has(getPersistenceKey()))
                return defaultValue;
            return persistentDataContainer.get(getPersistenceKey(), type);
        }

        @Override
        public void set(PersistentDataContainer persistentDataContainer, @Nullable B data) {
            if (data == null)
                persistentDataContainer.remove(getPersistenceKey());
            else
                persistentDataContainer.set(getPersistenceKey(), type, data);
        }
    }

    public static <Z, B> NativePersistentVariable<Z, B> create(String namespace, String key, PersistentDataType<Z, B> dataType, B defaultValue) {
        return new NativePersistentVariable<>(namespace, key, dataType, defaultValue);
    }

    public static PersistentVariable<Location> location(String namespace, String key) {
        return create(namespace, key, LocationTagType.INSTANCE, null);
    }

    public static PersistentVariable<UUID> uuid(String namespace, String key) {
        return create(namespace, key, UUIDTagType.INSTANCE, null);
    }

    public static PersistentVariable<String[]> stringArray(String namespace, String key) {
        return create(namespace, key, StringArrayTagType.INSTANCE, new String[0]);
    }

    public static PersistentVariable<boolean[]> booleanArray(String namespace, String key) {
        return create(namespace, key, BooleanArrayTagType.INSTANCE, new boolean[0]);
    }

    public static class LocationTagType implements PersistentDataType<PersistentDataContainer, Location> {
        public static final LocationTagType INSTANCE = new LocationTagType();

        private LocationTagType() {
        }

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Location> getComplexType() {
            return Location.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer persistentDataContainer = context.newPersistentDataContainer();
            NBTContainer nbtContainer = NBTContainer.of("minecraft", persistentDataContainer);
            nbtContainer.set("level", complex.getWorld().getName());
            nbtContainer.set("x", complex.x());
            nbtContainer.set("y", complex.y());
            nbtContainer.set("z", complex.z());
            nbtContainer.set("yaw", complex.getYaw());
            nbtContainer.set("pitch", complex.getPitch());
            return persistentDataContainer;
        }

        @Override
        public @NotNull Location fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            NBTContainer nbtContainer = NBTContainer.of("minecraft", primitive);
            String worldName = nbtContainer.getString("level");
            World world = Bukkit.getWorld(worldName);
            if (world == null)
                return null;
            double x = nbtContainer.getDouble("x");
            double y = nbtContainer.getDouble("y");
            double z = nbtContainer.getDouble("z");
            float yaw = nbtContainer.getFloat("yaw");
            float pitch = nbtContainer.getFloat("pitch");
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        }
    }

    public static class UUIDTagType implements PersistentDataType<byte[], UUID> {
        public static final UUIDTagType INSTANCE = new UUIDTagType();

        private UUIDTagType() {
        }

        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(complex.getMostSignificantBits());
            bb.putLong(complex.getLeastSignificantBits());
            return bb.array();
        }

        @Override
        public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(primitive);
            long firstLong = bb.getLong();
            long secondLong = bb.getLong();
            return new UUID(firstLong, secondLong);
        }
    }

    public static class StringArrayTagType implements PersistentDataType<PersistentDataContainer[], String[]> {
        public static final StringArrayTagType INSTANCE = new StringArrayTagType();

        private StringArrayTagType() {
        }

        private static final PersistentVariable<String> stringVariable = create("minecraft", "string_array_value", PersistentDataType.STRING, null);

        @Override
        public @NotNull Class<PersistentDataContainer[]> getPrimitiveType() {
            return PersistentDataContainer[].class;
        }

        @Override
        public @NotNull Class<String[]> getComplexType() {
            return String[].class;
        }

        @Override
        public PersistentDataContainer @NotNull [] toPrimitive(String @NotNull [] complex, @NotNull PersistentDataAdapterContext context) {
            return Arrays.stream(complex).map(s -> {
                PersistentDataContainer persistentDataContainer = context.newPersistentDataContainer();
                stringVariable.set(persistentDataContainer, s);
                return persistentDataContainer;
            }).toArray(PersistentDataContainer[]::new);
        }

        @Override
        public String @NotNull [] fromPrimitive(PersistentDataContainer @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            return Arrays.stream(primitive).map(stringVariable::read).toArray(String[]::new);
        }
    }

    public static class BooleanArrayTagType implements PersistentDataType<byte[], boolean[]> {
        public static final BooleanArrayTagType INSTANCE = new BooleanArrayTagType();

        private BooleanArrayTagType() {
        }

        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<boolean[]> getComplexType() {
            return boolean[].class;
        }

        @Override
        public byte @NotNull [] toPrimitive(boolean @NotNull [] complex, @NotNull PersistentDataAdapterContext context) {
            byte[] bArray = new byte[complex.length];
            for (int i = 0; i < complex.length; i++) {
                if (complex[i])
                    bArray[i] = 1;
                else
                    bArray[i] = 0;
            }
            return bArray;
        }

        @Override
        public boolean @NotNull [] fromPrimitive(byte @NotNull [] bArray, @NotNull PersistentDataAdapterContext context) {
            var array = new boolean[bArray.length];
            for (int i = 0; i < bArray.length; i++)
                array[i] = bArray[i] == 1;
            return array;
        }
    }
}
