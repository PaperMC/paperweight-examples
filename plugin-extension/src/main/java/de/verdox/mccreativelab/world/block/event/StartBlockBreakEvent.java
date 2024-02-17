package de.verdox.mccreativelab.world.block.event;

import de.verdox.mccreativelab.world.block.FakeBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called when a user starts damaging a block and thus starts a block breaking action.
 * Can be used to change a block hardness.
 * <p>
 * <p>
 * <p>
 * Use this with caution! The block hardness change is not synced with the player client.
 * The effect is achieved by giving the player mining fatigue when breaking a block and completely running the logic on the server.
 * However, the client may predict a block damage that is not in sync with the server.
 * <p>
 * <p>
 * <p>
 * For example a player is breaking a stone block with an efficiency 5 diamond pickaxe.
 * The client now predicts a block damage, and maybe even breaks it client side while the server knows a different block hardness.
 */
public class StartBlockBreakEvent extends BlockEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final FakeBlock.FakeBlockState fakeBlockState;
    private float hardness;

    public StartBlockBreakEvent(@NotNull Player player, @Nullable FakeBlock.FakeBlockState fakeBlockState, @NotNull Block theBlock, float hardness) {
        super(theBlock);
        this.player = player;
        this.fakeBlockState = fakeBlockState;
        this.hardness = hardness;
    }

    /**
     * The hardness of the block that is broken.
     * If you want to use vanilla hardness set this value to -1;
     * @return The block hardness.
     */
    public float getHardness() {
        return hardness;
    }

    /**
     * The player breaking the block
     * @return - The block breaking player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * A FakeBlockState if available. If no FakeBlockState is available the player is breaking a normal minecraft block.
     * @return - The FakeBlockState if available
     */
    public @Nullable FakeBlock.FakeBlockState getFakeBlockState() {
        return fakeBlockState;
    }

    /**
     * Sets the hardness of the block that is being broken
     * If you want to use vanilla hardness set this value to -1;
     * @param hardness - The Block hardness
     */
    public void setHardness(float hardness) {
        this.hardness = hardness;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
