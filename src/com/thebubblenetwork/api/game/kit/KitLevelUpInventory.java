package com.thebubblenetwork.api.game.kit;

import com.thebubblenetwork.api.framework.BubbleNetwork;
import com.thebubblenetwork.api.framework.BubblePlayer;
import com.thebubblenetwork.api.framework.util.mc.menu.BuyInventory;
import com.thebubblenetwork.api.game.BubbleGameAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.UUID;

/**
 * Copyright Statement
 * ----------------------
 * Copyright (C) The Bubble Network, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Wrote by Jacob Evans <jacobevansminor@gmail.com>, 01 2016
 *
 *
 * Class information
 * ---------------------
 * Package: com.thebubblenetwork.api.game.kit
 * Date-created: 17/01/2016 13:43
 * Project: BubbleFramework
 */

public class KitLevelUpInventory extends BuyInventory{
    private static final Sound
            BUYKIT = Sound.LEVEL_UP,
            CANCELBUY = Sound.BLAZE_HIT;

    public static KitLevelUpInventory getInventoryManual(Player p){
        return (KitLevelUpInventory) BubbleNetwork.getInstance().getManager().getMenu("kit_levelup_" + p.getUniqueId());
    }

    private int cost;
    private Kit k;
    private boolean cancelled = false;

    public KitLevelUpInventory(Kit k, int cost,int level, final Player player) {
        super(ChatColor.GOLD + k.getNameClear() + ChatColor.DARK_GRAY + " -> Lv" + ChatColor.RED + String.valueOf(level) + ChatColor.DARK_GRAY + " T" +
                      ChatColor.GREEN + String.valueOf(k.getPrice()),"kit_levelup_" + player.getUniqueId())  ;
        this.k = k;
        this.cost = cost;
        BubbleNetwork.getInstance().registerListener(new Listener() {
            @EventHandler
            public void onInventoryClose(InventoryCloseEvent e){
                if(e.getInventory() == getInventory()){
                    cancelled = true;
                    BubbleNetwork.getInstance().getManager().remove("kit_levelup_" + player.getUniqueId());
                    HandlerList.unregisterAll(this);
                }
            }
        });
    }

    public Kit getKit(){
        return k;
    }

    public int getCost(){
        return cost;
    }

    public boolean isCancelled(){
        return cancelled;
    }

    @Override
    public void onCancel(Player player) {
        player.closeInventory();
        KitSelection.openMenu(player);
        player.playSound(player.getLocation().getBlock().getLocation(),CANCELBUY,1f,1f);
    }

    @Override
    public void onAllow(Player player) {
        player.closeInventory();
        //TODO - Kitbuying
        KitSelection.openMenu(player);
        player.playSound(player.getLocation().getBlock().getLocation(),BUYKIT,1f,1f);

    }
}