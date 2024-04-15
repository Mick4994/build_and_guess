package org.mick.build_and_guess;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameRound extends BukkitRunnable {

    private Build_and_guess plugin;
    private boolean havePrint = false;
    private Set<String> guessed_word = new HashSet<String>();
    private ArrayList<String> all_word = new ArrayList<String>();;

    public GameRound(Build_and_guess plugin) throws SQLException, ClassNotFoundException {
        this.plugin = plugin;
        readFile();
    }

    public void readFile() {
        String pathname = this.plugin.getDataFolder().getAbsolutePath() + "/guess_words.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
        //防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                all_word.add(line);
            }
            this.plugin.logger.info(all_word.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean haveGameOver() {
        for(Player player : this.plugin.server.getOnlinePlayers()) {
            Set<String> playerTag = player.getScoreboardTags();
            if(playerTag.contains("winner")) return true;
        }
        return false;
    }

    private void setGuessWord() {
        String guess_word;
        do {
            guess_word = all_word.get((int) (Math.random() * all_word.size()));
        } while (guessed_word.contains(guess_word));
        guessed_word.add(guess_word);
        this.plugin.chatHandler.setGuessWord(guess_word);
        this.plugin.commandExecutor("msg @a[tag=building] " + "Guess word: " + guess_word);
    }

    private void setActionBar() {
        for(Player player : this.plugin.server.getOnlinePlayers()) {
            String actionBarMsg = "";
            Set<String> tags =  player.getScoreboardTags();
            if(tags.contains("building") || tags.contains("correct_guessed")) {
                actionBarMsg = this.plugin.chatHandler.guessWord;
            }
            if (!tags.contains("building")) {
                actionBarMsg = this.plugin.chatHandler.unguess_word;
            }
            TextComponent textComponent = new TextComponent(actionBarMsg);
            textComponent.setColor(ChatColor.AQUA);
            player.sendMessage(ChatMessageType.ACTION_BAR, textComponent);
        }
    }

    @Override
    public void run() {
        if(plugin.inGame) {
            if(havePrint) havePrint = false;
            int timeLeft = this.plugin.chatHandler.getTimeLeft();
            if(timeLeft == 2399) {
                plugin.chatHandler.guess_stop = false;
                plugin.chatHandler.guessCounter = 0;
                setGuessWord();
            } else if (timeLeft > 0) {
                setActionBar();
                if (timeLeft == 290) {
                    this.plugin.chatHandler.showRandomWord();
                }
                if (timeLeft < 2000 && this.plugin.chatHandler.haveGuessCount == 0) {
                    this.plugin.chatHandler.showRandomWord();
                }
                if (timeLeft == 60 || timeLeft == 40 || timeLeft == 20) {
                    if(!plugin.chatHandler.guess_stop) {
                        this.plugin.chatHandler.commandExecutor("execute run playsound minecraft:block.stone_button.click_on voice @a ~ ~ ~");
                        for(Player player : this.plugin.server.getOnlinePlayers()) {
                            player.sendMessage(Component.text("剩余" + (timeLeft / 20) + "秒").color(TextColor.color(0xE2740E)));
                        }
                    }
                }
                if (timeLeft == 1 && !plugin.chatHandler.guess_stop) {
                    plugin.chatHandler.guess_stop = true;
                    this.plugin.commandExecutor("scoreboard players set time_left building_and_guessing_time_left 60");
                    this.plugin.commandExecutor("title @a title [{\"text\":\"" + this.plugin.chatHandler.guessWord + "\",\"color\":\"white\",\"bold\":false}]");
                }
            }
        }

        // 当游戏正常结束，或者出现所有人同时掉线，还是因其他原因退到没人时执行
        if(this.plugin.server.getOnlinePlayers().isEmpty() || haveGameOver()) {
            if(!havePrint) {
                havePrint = true;
                this.plugin.logger.info("game over!");
            }
            guessed_word.clear();
            plugin.inGame = false;
        }
    }
}
