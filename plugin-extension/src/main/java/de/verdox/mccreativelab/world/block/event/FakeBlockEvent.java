package de.verdox.mccreativelab.world.block.event;

import de.verdox.mccreativelab.world.block.FakeBlock;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public abstract class FakeBlockEvent extends BlockEvent {
    private final FakeBlock.FakeBlockState fakeBlockState;

    public FakeBlockEvent(@NotNull Block theBlock, @NotNull FakeBlock.FakeBlockState fakeBlockState) {
        super(theBlock);
        this.fakeBlockState = fakeBlockState;
    }
    public FakeBlock.FakeBlockState getFakeBlockState() {
        return fakeBlockState;
    }
}
