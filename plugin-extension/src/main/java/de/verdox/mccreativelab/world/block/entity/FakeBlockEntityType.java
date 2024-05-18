package de.verdox.mccreativelab.world.block.entity;

import de.verdox.mccreativelab.world.block.FakeBlock;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class FakeBlockEntityType<T extends FakeBlockEntity> implements Keyed {
    private final NamespacedKey namespacedKey;
    private final FakeBlock fakeBlock;
    private final Supplier<T> constructor;

    FakeBlockEntityType(NamespacedKey namespacedKey, FakeBlock fakeBlock, Supplier<T> constructor) {
        this.namespacedKey = namespacedKey;
        this.fakeBlock = fakeBlock;
        this.constructor = constructor;
    }

    public FakeBlock getFakeBlock() {
        return fakeBlock;
    }

    public T create(){
        return constructor.get();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return namespacedKey;
    }
}
