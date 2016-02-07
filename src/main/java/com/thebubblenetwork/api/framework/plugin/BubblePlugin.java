package com.thebubblenetwork.api.framework.plugin;

import com.avaje.ebean.EbeanServer;
import com.thebubblenetwork.api.framework.BubbleNetwork;
import com.thebubblenetwork.api.framework.P;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Jacob on 09/12/2015.
 */
public class BubblePlugin implements Plugin {
    private PluginDescriptionFile descriptionFile;

    public org.bukkit.plugin.PluginDescriptionFile getDescription() {
        return PluginDescriptionFile.asMirror(descriptionFile);
    }

    public PluginDescriptionFile getDescriptionBubble() {
        return descriptionFile;
    }

    public FileConfiguration getConfig() {
        return getPlugin().getConfig();
    }

    public InputStream getResource(String s) {
        return getPlugin().getResource(s);
    }

    @Deprecated
    public void saveConfig() {
    }

    @Deprecated
    public void saveDefaultConfig() {
    }

    @Deprecated
    public void saveResource(String s, boolean b) {
    }

    @Deprecated
    public void reloadConfig() {

    }

    public PluginLoader getPluginLoader() {
        return getPlugin().getPluginLoader();
    }

    public boolean isEnabled() {
        return getPlugin().isEnabled();
    }

    public void __init__(BubblePluginLoader loader) {
        descriptionFile = loader.getFile();
    }

    public void onLoad() {
    }

    public boolean isNaggable() {
        return false;
    }

    @Deprecated
    public void setNaggable(boolean b) {
    }

    public EbeanServer getDatabase() {
        return getPlugin().getDatabase();
    }

    public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
        return getPlugin().getDefaultWorldGenerator(s, s1);
    }

    public Logger getLogger() {
        return getPlugin().getLogger();
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;
    }

    @Deprecated
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<String>();
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public P getPlugin() {
        return BubbleNetwork.getInstance().getPlugin();
    }

    public File getDataFolder() {
        return new File(getPlugin().getDataFolder() + File.separator + getName());
    }

    public Server getServer() {
        return getPlugin().getServer();
    }

    public String getName() {
        return descriptionFile.getName();
    }

    public void registerListener(Listener l) {
        getServer().getPluginManager().registerEvents(l, this);
    }
}