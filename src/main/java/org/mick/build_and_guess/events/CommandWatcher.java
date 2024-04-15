package org.mick.build_and_guess.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.mick.build_and_guess.Build_and_guess;

public class CommandWatcher implements Listener {

    private final Build_and_guess plugin;

    public CommandWatcher(Build_and_guess plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerCommandEvent(PlayerCommandPreprocessEvent event) {
        String commandLine = event.getMessage();
        Player player = event.getPlayer();
        Component component = Component.text("NO way to tell other player")
                .color(TextColor.color(0xE21611));
        if(commandLine.startsWith("/msg") && this.plugin.inGame) {
            event.setCancelled(true);
            player.sendMessage(component);
        }
    }
}
