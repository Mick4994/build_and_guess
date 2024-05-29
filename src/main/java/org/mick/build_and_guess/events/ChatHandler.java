package org.mick.build_and_guess.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Color;
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
    public boolean guess_stop = false;
    public boolean enough_stop = false;
    private final Logger logger;
    private final Server server;
    private final Build_and_guess plugin;
    private final int[] timeLeftArray = {1800, 600, 300, 200, 1};
    public int guessCounter = 0;
    public String guessWord;
    public int haveGuessCount = 0;
    public String unguess_word = "";

    public ChatHandler(Build_and_guess plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.server = plugin.getServer();
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
            chatPlayer.sendMessage(Component.text("建造者和猜对者不许发言！").color(TextColor.color(0xE21E11)));
            event.setCancelled(true);
        } else {
            if(this.plugin.inGame) {
                if(message.equals(guessWord) && !guess_stop) {
                    commandExecutor("tag " + name + " add correct_guess");

                    // 剩余时间控制
                    if(guessCounter < 4) {
                        int timeLeft = Math.min(timeLeftArray[guessCounter], getTimeLeft());
                        commandExecutor("scoreboard players set time_left building_and_guessing_time_left " + timeLeft);
                        guessCounter += 1;
                    }

                    // 各个角色聊天栏发送内容设置
                    for(Player player : server.getOnlinePlayers()) {
                        int score = 3 - guessCounter + 1 > 0 ? 3 - guessCounter + 1 : 1;
                        TextColor textColor = TextColor.color(0xFFFFF7);
                        switch (score) {
                            case 3:
                                String buildName = "";
                                for(Player buildPlayer : server.getOnlinePlayers()) {
                                    if(buildPlayer.getScoreboardTags().contains("building")) {
                                        buildName = buildPlayer.getName();
                                        break;
                                    }
                                }
                                textColor = TextColor.color(0xABE222);
                                player.sendMessage(Component.text(buildName+" 的建筑被猜中了！获得3分！").color(textColor));
                                textColor = TextColor.color(0xE2DE18);
                                break;
                            case 2:
                                textColor = TextColor.color(0xD2E168);
                                break;
                            default:
                                textColor = TextColor.color(0xDEE297);
                                break;
                        }
                        player.sendMessage(Component.text(name+" 第"+guessCounter+"个猜中了！获得"+score+"分！").color(textColor));
                    }

                    // 所有人猜测完毕设置
                    if(guessCounter >= server.getOnlinePlayers().size() - 1) {
                        commandExecutor("scoreboard players set time_left building_and_guessing_time_left 60");
                        server.sendMessage(Component.text("哇！所有猜测者都猜中了！").color(TextColor.color(0x41E12C)));
                        enough_stop = true;
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    public void showRandomWord() {
        if(haveGuessCount < unguess_word.length() - 1) {
            int index = (int)(Math.random() * unguess_word.length());
            char[] unguess_word_array = unguess_word.toCharArray();
            unguess_word_array[index] = guessWord.charAt(index);
            unguess_word = new String(unguess_word_array);
            haveGuessCount += 1;
        }
    }

    public void setGuessWord(String guessWord) {
        this.guessWord = guessWord;
        this.unguess_word = "";
        this.haveGuessCount = 0;
        for(char word : this.guessWord.toCharArray()) {
            this.unguess_word += '_';
        }
    }
}
