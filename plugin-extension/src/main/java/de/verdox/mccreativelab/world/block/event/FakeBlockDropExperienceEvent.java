package de.verdox.mccreativelab.world.block.event;

import de.verdox.mccreativelab.world.block.FakeBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FakeBlockDropExperienceEvent extends FakeBlockEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Entity causeOfBreak;
    private final ItemStack tool;
    private final boolean ignoreTool;
    private boolean cancelled;
    private int experienceToDrop;
    public FakeBlockDropExperienceEvent(@NotNull Block theBlock, FakeBlock.@NotNull FakeBlockState fakeBlockState, int experienceToDrop, @Nullable Entity causeOfBreak, @Nullable ItemStack tool, boolean ignoreTool) {
        super(theBlock, fakeBlockState);
        this.experienceToDrop = experienceToDrop;
        this.causeOfBreak = causeOfBreak;
        this.tool = tool;
        this.ignoreTool = ignoreTool;
    }

    public Entity getCauseOfBreak() {
        return causeOfBreak;
    }

    public int getExperienceToDrop() {
        return experienceToDrop;
    }

    public void setExperienceToDrop(int experienceToDrop) {
        this.experienceToDrop = experienceToDrop;
    }

    public ItemStack getTool() {
        return tool;
    }

    public boolean isIgnoreTool() {
        return ignoreTool;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
