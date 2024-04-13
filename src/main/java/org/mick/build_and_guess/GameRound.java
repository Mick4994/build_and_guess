package org.mick.build_and_guess;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

public class GameRound extends BukkitRunnable {

    private Build_and_guess plugin;
    private boolean havePrint = false;

    public GameRound(Build_and_guess plugin) throws SQLException, ClassNotFoundException {
        this.plugin = plugin;

        Connection connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    public boolean haveGameOver() {
        for(Player player : this.plugin.server.getOnlinePlayers()) {
            Set<String> playerTag = player.getScoreboardTags();
            if(playerTag.contains("winner")) return true;
        }
        return false;
    }

    @Override
    public void run() {
        if(plugin.inGame) {
            if(havePrint) havePrint = false;
            int timeLeft = this.plugin.chatHandler.getTimeLeft();
            if(timeLeft == 2399) {
                this.plugin.chatHandler.setGuessWord("你好");
                this.plugin.commandExecutor("say setGuessWord!");
            }
        }

        // 当游戏正常结束，或者出现所有人同时掉线，还是因其他原因退到没人时执行
        if(this.plugin.server.getOnlinePlayers().isEmpty() || haveGameOver()) {
            if(!havePrint) {
                havePrint = true;
                this.plugin.logger.info("game over!");
            }
            plugin.inGame = false;
        }
    }
}
