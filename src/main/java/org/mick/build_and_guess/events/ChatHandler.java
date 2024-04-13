package org.mick.build_and_guess.events;

import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Score;
import org.mick.build_and_guess.Build_and_guess;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

public class ChatHandler implements Listener {

    private volatile boolean chatEnabled = true;
    private final Logger logger;
    private final Server server;
    private final Build_and_guess plugin;
    private final int[] timeLeftArray = {1200, 600, 300, 200, 1};
    public int guessCounter = 0;
    private String guessWord;

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

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public int getTimeLeft() {
        int timeLeft = 99_999;

        for(Player player : server.getOnlinePlayers()) {
            if(player.getScoreboardTags().contains("building")) {
                for(Score score : player.getScoreboard().getScores("time_left")) {
                    if(score.getObjective().getName().equals("building_and_guessing_time_left")) {
                        return score.getScore();
                    }
                }
            }
        }
        return timeLeft;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) throws IOException {
        String message = event.getMessage();
        Player chatPlayer = event.getPlayer();
        String name = chatPlayer.getName();
        Set<String> chatPlayerTag = chatPlayer.getScoreboardTags();
        if(chatPlayerTag.contains("building") || chatPlayerTag.contains("correct_guess") || chatPlayerTag.contains("correct_guessed") ) {
            event.setCancelled(true);
        } else {
            if(this.plugin.inGame) {
                if(message.equals(guessWord)) {
                    commandExecutor("tag " + name + " add correct_guess");

                    if(guessCounter < 4) {
                        int timeLeft = Math.min(timeLeftArray[guessCounter], getTimeLeft());
                        commandExecutor("scoreboard players set time_left building_and_guessing_time_left " + timeLeft);
                        guessCounter += 1;
                    }
                    if(guessCounter >= server.getOnlinePlayers().size() - 1) {
                        commandExecutor("scoreboard players set time_left building_and_guessing_time_left 60");
                    }
                    event.setMessage("猜中了！");
                }
            }
        }
    }

    public void setGuessWord(String guessWord) {
        this.guessWord = guessWord;
    }
}
