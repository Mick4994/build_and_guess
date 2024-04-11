package org.mick.build_and_guess.events;

import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.mick.build_and_guess.Build_and_guess;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class ChatHandler implements Listener {

    private volatile boolean chatEnabled = true;
    private HttpURLConnection request;
    private final Logger logger;
    private final Server server;
    private final Build_and_guess plugin;

    public ChatHandler(Logger logger, Server server, Build_and_guess plugin) {
        this.logger = logger;
        this.server = server;
        this.plugin = plugin;
    }

    public void commandExecutor(String command) throws CommandException {
        server.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
            server.dispatchCommand(server.getConsoleSender(), command);
        });
    }

    private void sendGuessWord(String word, String playerName) throws IOException {
        URL url = new URL("http://localhost:5994/guess?word=" + word + "&player=" + playerName);
        request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        request.connect();
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) throws IOException {
        if (!chatEnabled) {
            String message = event.getMessage();
            if(message.startsWith("@")) {
                commandExecutor(message.substring(1));
            }
            logger.info(message);
            sendGuessWord(message, event.getPlayer().getName());
            event.setCancelled(true);
        }
    }
}
