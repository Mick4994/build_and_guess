package org.mick.build_and_guess.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mick.build_and_guess.Build_and_guess;


public class PlayerJoinHandler implements Listener {

    private final Build_and_guess plugin;

    public PlayerJoinHandler(Build_and_guess plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(this.plugin.inGame) {
            this.plugin.commandExecutor("tag " + player.getName() + " add builded");
        }
    }
}
