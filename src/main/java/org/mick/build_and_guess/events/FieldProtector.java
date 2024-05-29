package org.mick.build_and_guess.events;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.mick.build_and_guess.Build_and_guess;

public class FieldProtector implements Listener {

    private final int[] grep = {56, 34, 26, 4, -4, -26, -34, -56};

    private Build_and_guess plugin;

    public FieldProtector(Build_and_guess plugin) {
        this.plugin = plugin;
    }

    private boolean needCancelled(Block cancelledBlock) {
        Location blockLocation = cancelledBlock.getLocation();
        int x = blockLocation.getBlockX();
        int y = blockLocation.getBlockY();
        int z = blockLocation.getBlockZ();
        for (int i = 0; i < grep.length; i+=2) {
            if(x <= grep[i] && x >= grep[i+1]){
                for (int j = 0; j < grep.length; j+=2) {
                    if(z <= grep[j] && x >= grep[j+1]) {
                        if(y >= 0 && y <= 31) {
                            return false;
                        }
                    }
                }
            }
        }
        return plugin.inGame;
    }

    // 方块破坏
    @EventHandler
    public void onFieldBlockBreakEvent(BlockBreakEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 方块放置
    @EventHandler
    public void onFieldBlockPlaceEvent(BlockPlaceEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 防放置床
    @EventHandler
    public void onBlockMultiPlaceEvent(BlockMultiPlaceEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 液体流入
    @EventHandler
    public void onBlockFromToEvent(BlockFromToEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 混凝土固化
    @EventHandler
    public void onEntityBlockFormEvent(EntityBlockFormEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 防活塞
    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 防TNT
    @EventHandler
    public void onTNTPrimeEvent(TNTPrimeEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // 防蘑菇蔓延
    @EventHandler
    public void onBlockSpreadEvent(BlockSpreadEvent event) {
        if(needCancelled(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
