package com.thebubblenetwork.api.game.scoreboard;

import com.thebubblenetwork.api.framework.player.BukkitBubblePlayer;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.api.BoardPreset;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.api.BoardScore;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.api.BubbleBoardAPI;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.util.BoardModuleBuilder;
import com.thebubblenetwork.api.game.BubbleGameAPI;
import com.thebubblenetwork.api.global.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

public class LobbyPreset extends BoardPreset {
    public static final String PLAYINGTITLE = ChatColor.BLUE + "" + ChatColor.BOLD + "Playing", RANKTITLE = ChatColor.BLUE + "" + ChatColor.BOLD + "Rank", TOKENSTITLE = ChatColor.BLUE + "" + ChatColor.BOLD + "Tokens", SITE = "thebubblenetwork", PLAYERNEED = "Players needed", STARTING = "Starting in";

    public LobbyPreset() {
        super("Lobby",
                new BoardModuleBuilder("Playing", 12).withDisplay(PLAYINGTITLE).build(),
                new BoardModuleBuilder("PlayingValue", 11).withRandomDisplay().build(),
                new BoardModuleBuilder("Spacer1", 10).withRandomDisplay().build(),
                new BoardModuleBuilder("Rank", 9).withDisplay(RANKTITLE).build(),
                new BoardModuleBuilder("RankValue", 8).withRandomDisplay().
                build(), new BoardModuleBuilder("Spacer2", 7).withRandomDisplay().build(),
                new BoardModuleBuilder("Tokens", 6).withDisplay(TOKENSTITLE).build(),
                new BoardModuleBuilder("TokensValue", 5).withRandomDisplay().build(),
                new BoardModuleBuilder("Spacer3", 4).withRandomDisplay().build(),
                new BoardModuleBuilder("Status", 3).withDisplay(ChatColor.BLUE.toString() + ChatColor.BOLD.toString()).build(),
                new BoardModuleBuilder("StatusValue", 2).withRandomDisplay().build(),
                new BoardModuleBuilder("Spacer4", 1).withRandomDisplay().build(),
                new BoardModuleBuilder("address", 0).withRandomDisplay().build());
    }

    public void onEnable(BubbleBoardAPI board) {
        Team address = board.getScore(this, getModule("address")).getTeam();
        address.setPrefix(ChatColor.GRAY + "play.thebubble");
        address.setSuffix(ChatColor.GRAY + "network.com");
        BukkitBubblePlayer player = BukkitBubblePlayer.getObject(Bukkit.getPlayer(board.getName()).getUniqueId());
        BoardScore playingValue = board.getScore(this, getModule("PlayingValue"));
        playingValue.getTeam().setSuffix(BubbleGameAPI.getInstance().getName());
        BoardScore rankValue = board.getScore(this, getModule("RankValue"));
        Rank r = player.getRank();
        rankValue.getTeam().setSuffix(r.isDefault() ? "No rank" : String.valueOf(r.getName()));
        BoardScore tokenValue = board.getScore(this, getModule("TokensValue"));
        tokenValue.getTeam().setSuffix(String.valueOf(player.getTokens()));
        BoardScore status = board.getScore(this, getModule("Status"));
        BoardScore statusvalue = board.getScore(this, getModule("StatusValue"));
        if (Bukkit.getOnlinePlayers().size() < BubbleGameAPI.getInstance().getMinPlayers()) {
            status.getTeam().setSuffix(PLAYERNEED);
            statusvalue.getTeam().setSuffix(String.valueOf(BubbleGameAPI.getInstance().getMinPlayers() - Bukkit.getOnlinePlayers().size()));
        } else {
            status.getTeam().setSuffix(STARTING);
        }
    }

    public void setTokens(BubbleBoardAPI api, int tokens){
        api.getScore(this, getModule("TokensValue")).getTeam().setSuffix(String.valueOf(tokens));
    }

    public void setRank(BubbleBoardAPI api, String name){
        api.getScore(this, getModule("RankValue")).getTeam().setSuffix(name);
    }

    public void setStatus(BubbleBoardAPI api, String status){
        api.getScore(this, getModule("Status")).getTeam().setSuffix(status);
    }

    public void setStatusValue(BubbleBoardAPI api, String value){
        api.getScore(this, getModule("StatusValue")).getTeam().setSuffix(value);
    }
}
