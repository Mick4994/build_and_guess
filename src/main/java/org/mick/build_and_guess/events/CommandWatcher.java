package org.mick.build_and_guess.events;

import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.logging.Logger;

public class CommandWatcher implements Listener {

    private final Logger logger;
    private final Server server;

    public boolean commandExecutor(String command) throws CommandException {
        return server.dispatchCommand(server.getConsoleSender(), command);
    }

    public CommandWatcher(Logger logger, Server server) {
        this.logger = logger;
        this.server = server;
    }

    @EventHandler
    public void onServerCommandEvent(ServerCommandEvent event) {
        String commandLine = event.getCommand();
        logger.info("onCommand: " + commandLine);
    }
}
