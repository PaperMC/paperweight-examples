package de.verdox.mccreativelab.world.block.display;

import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.strategy.FakeBlockVisualStrategy;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackResource;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class FakeBlockDisplay extends ResourcePackResource {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(9999);
    private final FakeBlockVisualStrategy<?> fakeBlockVisualStrategy;
    protected final FakeBlock.FakeBlockHitbox hitBox;

    public FakeBlockDisplay(NamespacedKey namespacedKey, FakeBlockVisualStrategy<?> fakeBlockVisualStrategy, FakeBlock.FakeBlockHitbox hitBox){
        super(namespacedKey);
        this.fakeBlockVisualStrategy = fakeBlockVisualStrategy;
        this.hitBox = hitBox;
        this.hitBox.setUsed();
    }

    public FakeBlock.FakeBlockHitbox getHitBox() {
        return hitBox;
    }

    public abstract boolean simulateDiggingParticles();
    public abstract BlockData getDestroyParticleData();

    public FakeBlockVisualStrategy<?> getFakeBlockVisualStrategy() {
        return fakeBlockVisualStrategy;
    }

    protected int drawNewModelID(){
        return ID_COUNTER.getAndIncrement();
    }
    protected Material getModelMaterial(){
        return Material.BARRIER;
    }

    public interface Builder<T extends FakeBlockDisplay> {
        T build(NamespacedKey namespacedKey);
    }
}
