package com.thebubblenetwork.api.game;

import com.google.common.collect.ImmutableMap;
import com.thebubblenetwork.api.framework.BubbleNetwork;
import com.thebubblenetwork.api.framework.BukkitBubblePlayer;
import com.thebubblenetwork.api.framework.messages.Messages;
import com.thebubblenetwork.api.framework.plugin.BubbleAddon;
import com.thebubblenetwork.api.framework.plugin.BubbleRunnable;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.BoardModule;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.BoardPreset;
import com.thebubblenetwork.api.framework.util.mc.scoreboard.BoardScore;
import com.thebubblenetwork.api.framework.util.mc.world.VoidWorldGenerator;
import com.thebubblenetwork.api.game.kit.Kit;
import com.thebubblenetwork.api.game.kit.KitManager;
import com.thebubblenetwork.api.game.kit.KitSelection;
import com.thebubblenetwork.api.game.maps.GameMap;
import com.thebubblenetwork.api.game.maps.MapData;
import com.thebubblenetwork.api.game.maps.Vote;
import com.thebubblenetwork.api.game.maps.VoteInventory;
import com.thebubblenetwork.api.game.scoreboard.GameBoard;
import com.thebubblenetwork.api.game.scoreboard.LobbyPreset;
import com.thebubblenetwork.api.game.spectator.PlayersList;
import com.thebubblenetwork.api.global.file.DownloadUtil;
import com.thebubblenetwork.api.global.file.FileUTIL;
import com.thebubblenetwork.api.global.file.SSLUtil;
import com.thebubblenetwork.api.global.sql.SQLConnection;
import com.thebubblenetwork.api.global.sql.SQLUtil;
import com.thebubblenetwork.api.global.type.ServerType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Jacob on 12/12/2015.
 */
public abstract class BubbleGameAPI extends BubbleAddon {
    private static final String LOBBYMAP = "https://www.dropbox.com/s/0f6o78rpvd2oka3/world.zip?dl=1";

    public static Vector getLobbySpawn() {
        return new Vector(0D, 50D, 0D);
    }

    public static BubbleGameAPI getInstance() {
        return instance;
    }

    public static void setInstance(BubbleGameAPI instance) {
        BubbleGameAPI.instance = instance;
    }

    private static void stateChange(final BubbleGameAPI api, State oldstate, State newstate) {
        if (newstate.getPreset() != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                GameBoard.getBoard(p).enable(newstate.getPreset());
            }
        }
        if (newstate == State.PREGAME) {
            api.chosenmap = calculateMap(api);
            api.chosen = Bukkit.getWorld(api.chosenmap.getName());
            for (World w : Bukkit.getWorlds()) {
                if (!w.getName().equals("world") && w.getName().equals(lobbyworld) && !w.getName().equals(api.chosenmap.getName())) {
                    File file = w.getWorldFolder();
                    Bukkit.unloadWorld(w, false);
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
            api.teleportPlayers(api.chosenmap, api.chosen);
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerInventory inventory = p.getInventory();
                Kit k = KitSelection.getSelection(p).getKit();
                BukkitBubblePlayer player = BukkitBubblePlayer.getObject(p.getUniqueId());
                int level = k.getLevel(player);
                inventory.setContents(k.getInventorypreset(level));
                inventory.setArmorContents(k.getArmorpreset(level));
            }
            api.timer = new GameTimer(20, 5) {
                public void run(int seconds) {
                    Messages.broadcastMessageTitle(ChatColor.BLUE + String.valueOf(seconds), ChatColor.AQUA + "The game is starting", new Messages.TitleTiming(5, 10, 2));
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation().getBlock().getLocation(), Sound.NOTE_BASS, 1f, 1f);
                    }
                }

                public void end() {
                    api.setState(State.INGAME);
                }
            };
        }

        Change:
        if (newstate == State.HIDDEN && oldstate == null) {
            File worldfolder = new File(lobbyworld);
            FileUTIL.deleteDir(worldfolder);
            File tempzip = new File("temp.zip");
            try {
                SSLUtil.allowAnySSL();
            } catch (Exception e) {
                BubbleNetwork.getInstance().getLogger().log(Level.WARNING, "Could not allow all SSL", e);
                break Change;
            }
            try {
                DownloadUtil.download(tempzip, LOBBYMAP);
            } catch (Exception e) {
                BubbleNetwork.getInstance().getLogger().log(Level.WARNING, "Could not download lobby", e);
                break Change;
            }
            FileUTIL.setPermissions(tempzip, true, true, true);
            File temp = new File("temp");
            try {
                FileUTIL.unZip(tempzip.getPath(), temp.getPath());
            } catch (IOException e) {
                BubbleNetwork.getInstance().getLogger().log(Level.WARNING, "Could not unzip files", e);
                break Change;
            }
            if (!tempzip.delete()) {
                System.gc();
                if (!tempzip.delete()) {
                    tempzip.deleteOnExit();
                }
            }
            FileUTIL.setPermissions(temp, true, true, true);
            try {
                FileUTIL.copy(new File(temp + File.separator + temp.list()[0]), worldfolder);
            } catch (IOException e) {
                BubbleNetwork.getInstance().getLogger().log(Level.WARNING, "Could not copy files", e);
                break Change;
            }
            FileUTIL.deleteDir(temp);

        }
        if (newstate == State.LOADING) {
            new WorldCreator(BubbleGameAPI.lobbyworld).generateStructures(false).generator(VoidWorldGenerator.getGenerator()).createWorld();
            GameMap.doMaps();
            api.setState(State.LOBBY);
        }

        if (newstate == State.RESTARTING) {
            api.cleanup();
            if (api.getChosen() != null) {
                File file = api.getChosen().getWorldFolder();
                Bukkit.unloadWorld(api.getChosen(), false);
                FileUTIL.deleteDir(file);
                if (api.getChosen() != null) {
                    File worldfolder = api.getChosen().getWorldFolder();
                    Bukkit.unloadWorld(api.getChosen(), false);
                    FileUTIL.deleteDir(worldfolder);
                }
                if (api.getChosenGameMap() != null) {
                    File f = new File(api.getChosenGameMap().getName());
                    if (f.exists()) {
                        FileUTIL.deleteDir(f);
                    }
                }
            }
        }

        if (newstate == State.LOBBY) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.teleport(getLobbySpawn().toLocation(Bukkit.getWorld(lobbyworld)));
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().setContents(GameListener.generateSpawnInventory(4 * 9));
                p.getInventory().setArmorContents(new ItemStack[4]);
                p.setHealth(20.0D);
                p.setHealthScale(20.0D);
                p.setMaxHealth(20.0D);
                p.setFoodLevel(20);
                p.setLevel(0);
                p.setSaturation(600);
                Messages.sendMessageTitle(p, "", ChatColor.AQUA + "Welcome to " + ChatColor.BLUE + BubbleGameAPI.getInstance().getName(), new Messages.TitleTiming(10, 20, 30));
                p.teleport(BubbleGameAPI.getLobbySpawn().toLocation(Bukkit.getWorld(lobbyworld)));
                p.setGameMode(GameMode.SURVIVAL);
            }
            if (Bukkit.getOnlinePlayers().size() == api.getMinPlayers()) {
                api.setState(BubbleGameAPI.State.PREGAME);
            }
        }
    }

    private static GameMap calculateMap(BubbleGameAPI api) {
        final double chance = BubbleNetwork.getRandom().nextDouble();
        double current = 0;
        for (Map.Entry<GameMap, Double> entry : calculatePercentages(api).entrySet()) {
            current += entry.getValue();
            if (current <= chance) {
                return entry.getKey();
            }
        }
        //Hopefully shouldn't go past this point
        return GameMap.getMaps().get(0);
    }

    private static Map<GameMap, Double> calculatePercentages(BubbleGameAPI api) {
        Map<GameMap, Double> maps = new HashMap<>();
        final double votesize = api.getVotes().size();
        final double mapsize = GameMap.getMaps().size();
        for (Map.Entry<GameMap, Integer> entry : calculateScores(api).entrySet()) {
            maps.put(entry.getKey(), ((double) entry.getValue() + 1.0D) / (votesize + mapsize));
        }
        return maps;
    }

    private static Map<GameMap, Integer> calculateScores(BubbleGameAPI api) {
        Map<GameMap, Integer> maps = new HashMap<>();
        for (GameMap map : GameMap.getMaps()) {
            maps.put(map, 0);
        }
        GameMap temp;
        for (Vote v : api.getVotes().values()) {
            if ((temp = v.getMap()) != null && maps.containsKey(temp)) {
                maps.put(temp, maps.get(temp) + 1);
            }
        }
        return maps;
    }

    private static BubbleGameAPI instance;
    private static String lobbyworld = "Lobby";
    private LobbyPreset preset = new LobbyPreset();
    private World chosen = null;
    private GameMap chosenmap = null;
    private Map<UUID, Vote> votes = new HashMap<UUID, Vote>();
    private GameListener listener;
    private VoteInventory voteInventory;
    private GameTimer timer;
    private LobbyInventory hubInventory;
    private PlayersList list;
    private GameMode defaultgamemode;
    private String defaultkit;
    private String type;
    private int minplayers;

    public BubbleGameAPI(String type, GameMode defaultgamemode, String defaultkit, int minplayers) {
        super();
        this.minplayers = minplayers;
        this.type = type;
        this.defaultgamemode = defaultgamemode;
        this.defaultkit = defaultkit;
    }

    public int getMinPlayers() {
        return minplayers;
    }

    public GameListener getGame() {
        return listener;
    }

    public VoteInventory getVoteInventory() {
        return voteInventory;
    }

    public GameMap getChosenGameMap() {
        return chosenmap;
    }

    public World getChosen() {
        return chosen;
    }

    public void onLoad() {
        setState(State.HIDDEN);
    }

    public Map<GameMap, Double> calculatePercentages() {
        return calculatePercentages(this);
    }

    public State getState() {
        return State.state;
    }

    public void setState(State newstate) {
        State oldstate = State.state;
        State.state = newstate;
        onStateChange(oldstate, newstate);
        stateChange(this, oldstate, newstate);
    }

    public LobbyInventory getHubInventory() {
        return hubInventory;
    }

    public GameTimer getTimer() {
        return timer;
    }

    public PlayersList getPlayerList() {
        return list;
    }

    public void onEnable() {
        setInstance(this);
        SQLConnection connection = BubbleNetwork.getInstance().getConnection();
        try {
            BubbleNetwork.getInstance().logInfo("Finding map table");
            if (!SQLUtil.tableExists(connection, MapData.maptable)) {
                BubbleNetwork.getInstance().logInfo("Map table not found, creating new");
                //TODO - dynamic table creation
                SQLUtil.createTable(connection, MapData.maptable, new ImmutableMap.Builder<String, Map.Entry<SQLUtil.SQLDataType, Integer>>().put("map", new AbstractMap.SimpleImmutableEntry<>(SQLUtil.SQLDataType.TEXT, 32)).put("key", new AbstractMap.SimpleImmutableEntry<>(SQLUtil.SQLDataType.TEXT, -1)).put("value", new AbstractMap.SimpleImmutableEntry<>(SQLUtil.SQLDataType.TEXT, -1)).build());
                BubbleNetwork.getInstance().endSetup("Created map table");
                return;
            }
        } catch (Exception e) {
            BubbleNetwork.getInstance().endSetup("Could not get map table");
        }
        KitSelection.register(this);
        GameBoard.registerlistener(this);
        listener = new GameListener();
        hubInventory = new LobbyInventory();
        list = new PlayersList();
        runTask(new Runnable() {
            public void run() {
                setState(State.LOADING);
            }
        });
    }

    public void onDisable() {
        Bukkit.unloadWorld(lobbyworld, false);
        FileUTIL.deleteDir(new File(lobbyworld));
        setState(State.RESTARTING);
        KitSelection.unregister();
        VoteInventory.reset();
        setInstance(null);
    }


    public void startWaiting() {
        if (timer != null) {
            return;
        }
        BoardModule module = getPreset().getModule("Status");
        for (GameBoard board : GameBoard.getBoards()) {
            BoardScore score = board.getScore(preset, module);
            score.getTeam().setSuffix(LobbyPreset.STARTING);
        }
        timer = new GameTimer(20, 20) {
            public void run(int seconds) {
                BoardModule module = preset.getModule("StatusValue");
                for (GameBoard board : GameBoard.getBoards()) {
                    BoardScore score = board.getScore(preset, module);
                    score.getTeam().setSuffix(String.valueOf(seconds));
                }
                if (seconds <= 3 || seconds % 5 == 0) {
                    Messages.broadcastMessageTitle(ChatColor.BLUE + String.valueOf(seconds), "", new Messages.TitleTiming(5, 10, 2));
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation().getBlock().getLocation(), Sound.NOTE_BASS, 1f, 1f);
                }
            }

            public void end() {
                setState(State.PREGAME);
            }
        };
    }

    public void cancelWaiting() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = null;
        BoardModule module = getPreset().getModule("Status");
        for (GameBoard board : GameBoard.getBoards()) {
            BoardScore score = board.getScore(getPreset(), module);
            score.getTeam().setSuffix(LobbyPreset.PLAYERNEED);
        }
    }

    public LobbyPreset getPreset() {
        return preset;
    }

    public abstract void cleanup();

    public Map<UUID, Vote> getVotes() {
        return votes;
    }

    public void resetVotes(UUID u) {
        getVotes().remove(u);
    }

    public void addVote(UUID u, GameMap vote) {
        if (getVotes().containsKey(u)) {
            getVotes().get(u).setVote(vote);
        }
    }

    public void win(final Player p) {
        if (getState() != State.INGAME) {
            return;
        }
        p.playSound(p.getLocation().getBlock().getLocation(), Sound.LEVEL_UP, 5F, 5F);
        Messages.broadcastMessageTitle(ChatColor.BLUE + p.getName(), ChatColor.AQUA + "Has won the game", new Messages.TitleTiming(5, 20, 20));
        setState(State.ENDGAME);
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (t != p) {
                t.teleport(p);
            }
        }
        timer = new GameTimer(5, 60) {
            public void run(int left) {
                if (!p.isOnline()) {
                    cancel();
                    end();
                    return;
                }
                getChosen().spigot().playEffect(p.getLocation(), Effect.FLAME);
                Random r = BubbleNetwork.getRandom();
                if (left % 20 == 0 || (left % 4 == 0 && left / 4 < 5)) {
                    Messages.broadcastMessageAction(ChatColor.BLUE + "Restarting in " + ChatColor.AQUA + String.valueOf(left / 4));
                    final Firework firework = getChosen().spawn(p.getLocation(), Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();
                    Set<Color> colorSet = new HashSet<>();
                    int random = r.nextInt(3);
                    for (int i = 0; i < 2 + random; i++) {
                        colorSet.add(Color.fromRGB(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                    }
                    meta.addEffect(FireworkEffect.builder().flicker(r.nextBoolean()).trail(r.nextBoolean()).withColor(colorSet).build());
                    firework.setFireworkMeta(meta);
                    firework.setVelocity(p.getLocation().getDirection().multiply(2 + r.nextInt(2)));
                    new BubbleRunnable() {
                        public void run() {
                            if (!firework.isDead()) {
                                firework.detonate();
                            }
                        }
                    }.runTaskLater(BubbleGameAPI.this, TimeUnit.SECONDS, 2 + r.nextInt(3));
                }
            }

            public void end() {
                setState(State.RESTARTING);
                setState(State.LOBBY);
            }
        };
        p.setAllowFlight(true);
        p.setFlying(true);
    }

    public ServerType getType() {
        return ServerType.getType(type);
    }

    public Kit getDefaultKit() {
        return KitManager.getKit(defaultkit);
    }

    public abstract void onStateChange(State oldstate, State newstate);

    public abstract BoardPreset getScorePreset();

    public abstract GameMap loadMap(String name, MapData data, File yml, File zip);

    public abstract void teleportPlayers(GameMap map, World w);

    public String getTablesuffix() {
        return getName().toLowerCase();
    }

    public GameMode getGameMode() {
        return defaultgamemode;
    }

    public enum State {
        HIDDEN, LOADING, LOBBY, PREGAME, INGAME, ENDGAME, RESTARTING;

        protected static State state = null;

        public boolean joinable() {
            return this == LOBBY || this == INGAME;
        }

        public BoardPreset getPreset() {
            if (this == LOBBY) {
                return BubbleGameAPI.getInstance().getPreset();
            } else if (this == PREGAME || this == INGAME || this == ENDGAME) {
                return BubbleGameAPI.getInstance().getScorePreset();
            }
            return null;
        }
    }
}
