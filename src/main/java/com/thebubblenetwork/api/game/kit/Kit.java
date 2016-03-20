package com.thebubblenetwork.api.game.kit;

import com.thebubblenetwork.api.framework.BubbleNetwork;
import com.thebubblenetwork.api.framework.BukkitBubblePlayer;
import com.thebubblenetwork.api.framework.util.mc.chat.ChatColorAppend;
import com.thebubblenetwork.api.game.BubbleGameAPI;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Jacob on 12/12/2015.
 */
public class Kit {
    private final String name;
    private Material display;
    private List<ItemStack[]> inventorypreset;
    private String[] description;
    private int price;
    private KitBuyInventory buyInventory;

    public Kit(Material display, List<ItemStack[]> inventorypreset, String name, String[] description, int price) {
        Validate.notEmpty(inventorypreset);
        Validate.noNullElements(inventorypreset);
        this.display = display;
        this.inventorypreset = inventorypreset;
        this.name = name;
        this.description = description;
        this.price = price;
        buyInventory = new KitBuyInventory(this);
    }

    public int getMaxlevel() {
        return inventorypreset.size();
    }

    public boolean isOwned(BukkitBubblePlayer player) {
        return this == BubbleGameAPI.getInstance().getDefaultKit() || getLevel(player) > 0;
    }

    public int getLevel(BukkitBubblePlayer player) {
        int level = player.getKit(BubbleGameAPI.getInstance().getName(),getName());
        if(level > 0){
            return level;
        }
        return BubbleGameAPI.getInstance().getDefaultKit() == this ? 1 : 0;
    }

    public int getLevelUpcost(BukkitBubblePlayer player) {
        int level = getLevel(player);
        if (level == getMaxlevel()) {
            return -1;
        }
        return (price * (level + 1)) / (getMaxlevel() - level);
    }

    public void apply(BukkitBubblePlayer p) {
        Player bukkitPlayer = p.getPlayer();
        int level = getLevel(p);
        bukkitPlayer.getInventory().clear();
        bukkitPlayer.getInventory().setArmorContents(new ItemStack[4]);
        bukkitPlayer.getInventory().setContents(getInventorypreset(level));
    }

    public void buy(BukkitBubblePlayer player) {
        level(player, 1);
    }

    public void level(BukkitBubblePlayer player, int level) {
        player.setKit(BubbleGameAPI.getInstance().getName(), getNameClear(), level);
        player.save();
    }

    public KitBuyInventory getBuyInventory() {
        return buyInventory;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public ItemStack[] getInventorypreset(int level) {
        return inventorypreset.get(level-1);
    }

    public void setInventorypreset(ItemStack[] inventorypreset, int level) {
        this.inventorypreset.set(level-1,inventorypreset);
    }

    public Material getDisplay() {
        return display;
    }

    public String getName() {
        return name;
    }

    public String getNameClear() {
        return ChatColorAppend.wipe(name);
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }
}
