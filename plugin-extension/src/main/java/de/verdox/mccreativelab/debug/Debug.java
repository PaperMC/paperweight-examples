package de.verdox.mccreativelab.debug;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import de.verdox.mccreativelab.MCCreativeLabExtension;
import de.verdox.mccreativelab.world.block.FakeBlock;
import de.verdox.mccreativelab.world.block.display.ReusedBlockStateDisplay;
import de.verdox.mccreativelab.world.block.display.TransparentFullBlockEntityDisplay;
import de.verdox.mccreativelab.debug.block.CustomCrop;
import de.verdox.mccreativelab.debug.block.CustomOreBlock;
import de.verdox.mccreativelab.entity.UnleashableSugarCow;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.renderer.element.single.SingleHudText;
import de.verdox.mccreativelab.world.item.FakeItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Cow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Supplier;

public class Debug implements Listener {

    public static final FakeBlock DEBUG_BLOCK = MCCreativeLabExtension
        .getFakeBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "test_fake_block"), CustomOreBlock.class)
            .withBlockState(builder ->
                builder
                    .withBlockProperties(fakeBlockProperties ->
                        fakeBlockProperties.isRandomlyTicking(true)
                                           .withBlockHardness(4)
                                           .withExplosionResistance(6)
                                           .requiresCorrectToolForDrops(true)
                    )
                    .withBlockDisplay(
                        new ReusedBlockStateDisplay.Builder()
                            .asFullBlockDisplay(new Asset<>("/debug/textures/block/tin_ore.png"))
                    )
            )
        );

    public static final FakeBlock DEBUG_CROP = MCCreativeLabExtension
        .getFakeBlockRegistry()
        .register(new FakeBlock.Builder<>(new NamespacedKey("mccreativelab", "debug_crop"), CustomCrop.class)
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage0"), FakeBlock.FakeBlockHitbox.CROP_AGE_0, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(0))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage1"), FakeBlock.FakeBlockHitbox.CROP_AGE_1, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(1))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage2"), FakeBlock.FakeBlockHitbox.CROP_AGE_2, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(1))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage3"), FakeBlock.FakeBlockHitbox.CROP_AGE_3, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(2))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage4"), FakeBlock.FakeBlockHitbox.CROP_AGE_4, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(2))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage5"), FakeBlock.FakeBlockHitbox.CROP_AGE_5, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(3))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage6"), FakeBlock.FakeBlockHitbox.CROP_AGE_6, () -> Bukkit.createBlockData(Material.CARROTS, blockData -> ((Ageable) blockData).setAge(3))))
            .withBlockState(builder -> createFakeCropBlockState(builder, new NamespacedKey("minecraft","block/wheat_stage7"), FakeBlock.FakeBlockHitbox.CROP_AGE_7, () -> Bukkit.createBlockData(Material.BIRCH_FENCE))
            ));

    public static void createFakeCropBlockState(FakeBlock.FakeBlockState.Builder builder, NamespacedKey namespacedKey, FakeBlock.FakeBlockHitbox fakeBlockHitbox, Supplier<BlockData> blockDataFunction) {
        builder
            .withBlockProperties(fakeBlockProperties -> fakeBlockProperties.fromVanillaBlockData(fakeBlockHitbox.getBlockData()))
            .withBlockDisplay(new TransparentFullBlockEntityDisplay.Builder()
                .withModel(CustomCrop.createFakeCropModel(namespacedKey))
                .withDestroyParticleData(blockDataFunction.get())
                .withHitbox(fakeBlockHitbox)
            );
    }

    public static final FakeItem DEBUG_ITEM = MCCreativeLabExtension
        .getFakeItemRegistry()
        .register(
            new FakeItem.Builder<>(new NamespacedKey("mccreativelab", "debug_item"), Material.STICK, DebugItem.class)
                .withProperties(new FakeItem.FakeItemProperties().stacksTo(4).fireResistant())
                .withItemMeta(meta -> meta.displayName(Component.text("Debug Item")))
        );
    public static final DebugHud DEBUG_HUD = new DebugHud(new NamespacedKey("mccreativelab", "debug_hud"));
    public static final DebugMenu DEBUG_MENU = new DebugMenu(new NamespacedKey("mccreativelab","debug_menu"));

    public static void init() {
        MCCreativeLabExtension.getCustomResourcePack().register(DEBUG_HUD);
        MCCreativeLabExtension.getCustomResourcePack().register(DEBUG_MENU);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        MCCreativeLabExtension.getHudRenderer().getOrStartActiveHud(e.getPlayer(), DEBUG_HUD)
                              .executeOnElement("debugText", SingleHudText.RenderedSingleHudText.class, renderedSingleHudText -> {
                                  renderedSingleHudText.setRenderedText(Bukkit.getServer().getName() + " " + Bukkit
                                      .getServer().getMinecraftVersion());
                              });
    }

    @EventHandler
    public void onCreatureAddToWorld(EntityAddToWorldEvent e) {
        if (e.getEntity() instanceof Cow cow)
            cow.setCustomEntityBehaviour(Cow.class, new UnleashableSugarCow());
    }
}
