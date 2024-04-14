package org.mick.build_and_guess;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
//        Connection connection = null;
//        Class.forName("org.sqlite.JDBC");
//        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
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
        this.plugin.commandExecutor("msg @p[tag=building] " + "Guess word: " + guess_word);
    }

    @Override
    public void run() {
        if(plugin.inGame) {
            if(havePrint) havePrint = false;
            int timeLeft = this.plugin.chatHandler.getTimeLeft();
            if(timeLeft == 2399) {
                plugin.chatHandler.guessCounter = 0;
                setGuessWord();
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
