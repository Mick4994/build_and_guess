package org.mick.build_and_guess.events;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.mick.build_and_guess.Build_and_guess;

import java.util.ArrayList;

public class CommandWatcher implements Listener {

    private final Build_and_guess plugin;

    private final ArrayList<String> ban_command_array = Lists.newArrayList("/msg", "/tell", "/me", "/w");

    public CommandWatcher(Build_and_guess plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerCommandEvent(PlayerCommandPreprocessEvent event) {
        String commandLine = event.getMessage();
        Player player = event.getPlayer();
        Component component = Component.text("NO way to whisper answer to other player")
                .color(TextColor.color(0xE21611));
        for(String ban_command : ban_command_array) {
            if((commandLine.startsWith(ban_command))
                    && this.plugin.inGame) {
                event.setCancelled(true);
                player.sendMessage(component);
            }
        }
    }
}
