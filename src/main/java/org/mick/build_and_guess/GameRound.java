package org.mick.build_and_guess;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameRound extends BukkitRunnable {

    private Build_and_guess plugin;
    private boolean havePrintGameOver = false;
    private Set<String> guessed_word = new HashSet<String>();
    private ArrayList<Integer> countDownArray = new ArrayList<Integer>();
    private ArrayList<String> all_word = new ArrayList<String>();

    public GameRound(Build_and_guess plugin) {
        for (int i = 0; i < 5 * 20; i += 20) {
            countDownArray.add(i + 20);
        }
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

        // 进入游戏状态后
        if(plugin.inGame) {

            // 恢复上一局结束日志锁
            if(havePrintGameOver) havePrintGameOver = false;

            // 加载实时剩余时间
            int timeLeft = this.plugin.chatHandler.getTimeLeft();
            int game_time = this.plugin.config.getInt("game_time");

            // 游戏开始时初始化状态和发送猜词
            if(timeLeft == game_time) {
                plugin.chatHandler.guess_stop = false;
                plugin.chatHandler.guessCounter = 0;
                setGuessWord();

            // 进入本轮猜猜乐游戏
            } else if (timeLeft > 0) {
                setActionBar();

                // 在不暴露原词情况下最后再给一个字
                if (timeLeft == 290) {
                    this.plugin.chatHandler.showRandomWord();
                }

                // 开始给提示字的时刻
                int first_word_time;
                if (this.plugin.chatHandler.guessCounter > 2) {
                    first_word_time = game_time * 2 / 3;
                } else {
                    first_word_time = game_time / 2;
                }
                if (timeLeft < first_word_time && this.plugin.chatHandler.haveGuessCount == 0) {
                    this.plugin.chatHandler.showRandomWord();
                }

                // 倒计时时间
                if (countDownArray.contains(timeLeft) && !plugin.chatHandler.guess_stop) {
                    for(Player player : this.plugin.server.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                        player.sendMessage(Component.text("剩余" + (timeLeft / 20) + "秒").color(TextColor.color(0xE2740E)));
                    }
                }

                // 结束展示本轮猜词
                if (timeLeft == 1 && !plugin.chatHandler.guess_stop) {
                    plugin.chatHandler.guess_stop = true;
                    this.plugin.commandExecutor("scoreboard players set time_left building_and_guessing_time_left 60");
                    this.plugin.commandExecutor("title @a title [{\"text\":\"" + this.plugin.chatHandler.guessWord + "\",\"color\":\"white\",\"bold\":false}]");
                    for(Player player : this.plugin.server.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1F, 1F);
                    }
                }
            }
        }

        // 当游戏正常结束，或者出现所有人同时掉线，还是因其他原因退到没人时执行
        if(this.plugin.server.getOnlinePlayers().isEmpty() || haveGameOver()) {

            // 日志锁，打印一次游戏结束后上锁，等待退出的时间将不再触发打印
            if(!havePrintGameOver) {
                havePrintGameOver = true;
                this.plugin.logger.info("game over!");
            }

            // 一整局游戏结束清理状态
            guessed_word.clear();
            plugin.inGame = false;

            // ....本插件该局游戏的程序代码结束，等待数据包加载退出游戏，并将所有人传送到大厅
        }
    }
}
