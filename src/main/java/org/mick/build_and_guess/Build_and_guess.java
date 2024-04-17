package org.mick.build_and_guess;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.mick.build_and_guess.events.ChatHandler;
import org.bukkit.command.CommandException;
import org.mick.build_and_guess.events.CommandWatcher;
import org.mick.build_and_guess.events.PlayerJoinHandler;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

public final class Build_and_guess extends JavaPlugin {

    public ChatHandler chatHandler;
    public PlayerJoinHandler playerJoinHandler;
    public CommandWatcher commandWatcher;
    public Server server = this.getServer();
    public Logger logger = this.getLogger();
    public boolean inGame = false;
    public FileConfiguration config = this.getConfig();

    /**
     * 指令执行器: 对终端指令执行入口进行的封装
     * @param command  - 命令 + 参数. 例如: test abc 123
     * @return found 若命令未找到返回false
     * @throws CommandException - 当执行命令期间出现未捕获的异常时抛出
     */
    public boolean commandExecutor(String command) throws CommandException{
        return server.dispatchCommand(server.getConsoleSender(), command);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        config.addDefault("game_time", 3600);
        config.options().copyDefaults(true);
        saveConfig();

        // 初始化参数
        chatHandler = new ChatHandler(this);
        playerJoinHandler = new PlayerJoinHandler(this);
        commandWatcher = new CommandWatcher(this);

        // 注册指令事件
        Bukkit.getPluginManager().registerEvents(chatHandler, this);
        Bukkit.getPluginManager().registerEvents(playerJoinHandler, this);
        Bukkit.getPluginManager().registerEvents(commandWatcher, this);

        Objects.requireNonNull(this.getCommand("chat_manger")).setExecutor(this);

        new GameRound(this).runTaskTimer(this, 0, 0);
    }

    /**
     * chat_manger: 聊天控制器
     * @param sender    指令发送者，可以是玩家、远程控制台、本地控制台、命令方块、其他实现了Permissible接口的实现类
     * @param command   指令:就是this.getCommand("chat_manger")的返回值，存储着在plugin.yml中声明的各种指令信息
     * @param label     指令的名称:如/chat_manger的指令label就是"chat_manger"
     * @param args      指令的参数:加入输入的完整指令是/chat_manger 1 2 3，那么该args数组就是[1，2，3]
     * @return bool
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(label.equalsIgnoreCase("chat_manger")) {

            // 仅控制台指令
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage("Only use in console command");
                return false;
            }

            if (args.length != 1) {
                sender.sendMessage("error args num");
                return false;
            }

            // 参数决定聊天是否开启
            switch (args[0]) {
                case "on":
                    this.chatHandler.setChatEnabled(true);
                    sender.sendMessage("set chat on now");
                    return true;
                case "off":
                    this.chatHandler.setChatEnabled(false);
                    sender.sendMessage("set chat off now");
                    return true;
                default:
                    sender.sendMessage("error arg");
                    return false;
            }
        }

        if(label.equalsIgnoreCase("start")){
            if(inGame) {
                sender.sendMessage("The game is in progress");
                return true;
            }

            if (args.length > 0) {
                sender.sendMessage("error args num");
                return false;
            }

            boolean success = commandExecutor("function building_and_guessing:start {game_time:" + config.getInt("game_time") + "}");
            if(success) {
                inGame = true;
                logger.info("game start!");
                return true;
            } else {
                sender.sendMessage("start failed, please check start function");
            }
            return false;
        }

        if(label.equalsIgnoreCase("left") && sender.isOp()){
            if (args.length != 1) {
                sender.sendMessage("error args num");
                return false;
            }

            if(!inGame) {
                sender.sendMessage("must in game!");
                return false;
            }

            boolean success = commandExecutor("scoreboard players set time_left building_and_guessing_time_left " + args[0]);
            if(success) {
                String msg = "adjust time left to " + args[0];
                logger.info(msg);
                sender.sendMessage(msg);
                return true;
            }
        }

        if(label.equalsIgnoreCase("game_time") && sender.isOp()) {
            if (args.length != 1) {
                sender.sendMessage("error args num");
                return false;
            }

            config.set("game_time", Integer.valueOf(args[0]));
            config.options().copyDefaults(true);
            saveConfig();

            String msg = "set game_time to " + args[0];
            logger.info(msg);
            sender.sendMessage(msg);
            return true;
        }

        logger.info("Unknown command" + command.getName());
        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
