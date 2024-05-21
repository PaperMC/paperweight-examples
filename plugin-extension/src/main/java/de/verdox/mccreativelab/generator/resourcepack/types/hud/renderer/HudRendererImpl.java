package de.verdox.mccreativelab.generator.resourcepack.types.hud.renderer;

import de.verdox.mccreativelab.generator.resourcepack.types.hud.ActiveHud;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class HudRendererImpl extends Thread implements HudRenderer {
    private final Map<Player, PlayerHudRendererData> renderingDataCache = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<TickData> ticksToProcess = new LinkedBlockingQueue<>();
    private boolean isRunning = true;

    public HudRendererImpl() {
        super(null, null, "HudRendererThread");
    }

    public void printLastRender(Player player){
        if(renderingDataCache.containsKey(player))
            Bukkit.getLogger().info(renderingDataCache.get(player).getLastRendered().compact().toString());
    }

    @Override
    public ActiveHud getActiveHud(Player player, CustomHud customHud) {
        PlayerHudRendererData playerHudRendererData = getRendererData(player);
        return playerHudRendererData.getActiveHud(customHud);
    }

    @Override
    public ActiveHud getOrStartActiveHud(Player player, CustomHud customHud) {
        PlayerHudRendererData playerHudRendererData = getRendererData(player);
        return playerHudRendererData.getOrStartActiveHud(customHud);
    }

    public boolean stopActiveHud(Player player, CustomHud customHud) {
        return getRendererData(player).removeFromRendering(customHud);
    }

    @Override
    public void forceUpdate(Player player) {
        getRendererData(player).forceUpdate();
    }

    public void addTickToRenderQueue(Collection<? extends Player> serverPlayers) {
        ticksToProcess.offer(new TickData(Set.copyOf(serverPlayers)));
    }

    private PlayerHudRendererData getRendererData(Player player) {
        return renderingDataCache.computeIfAbsent(player, player1 -> new PlayerHudRendererData(player));
    }

    @Override
    public void interrupt() {
        isRunning = false;
        super.interrupt();
    }

    @Override
    public void run() {
        while(isRunning){

            try {
                var list = ticksToProcess.take().serverPlayers;
                for (Player player : list) {
                    getRendererData(player).sendUpdate();
                }
                for (Player player : renderingDataCache.keySet()) {
                    if(!list.contains(player))
                        renderingDataCache.remove(player);
                }
            } catch (Throwable e) {
                System.out.println("An error occured in the HudRenderer");
                e.printStackTrace();
            }
        }
    }

    public record TickData(Set<Player> serverPlayers) {

    }
}
