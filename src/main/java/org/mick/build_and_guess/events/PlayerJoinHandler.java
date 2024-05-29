package org.mick.build_and_guess.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mick.build_and_guess.Build_and_guess;

import java.util.Objects;
import java.util.Set;


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
        } else {
            Set<String> tags = player.getScoreboardTags();
            for(String tag: tags) {
                this.plugin.logger.info("tag: " + tag);
                this.plugin.chatHandler.commandExecutor("tag " + player.getName() + " remove " + tag);
            }

            player.teleport(Objects.requireNonNull(player.getRespawnLocation()));

            this.plugin.chatHandler.commandExecutor("clear " + player.getName());
            this.plugin.chatHandler.commandExecutor("effect clear " + player.getName());
            this.plugin.chatHandler.commandExecutor("effect give " + player.getName() + " minecraft:saturation infinite 3 true");
        }
    }
}
