package com.thebubblenetwork.api.framework.interaction;

import com.thebubblenetwork.api.framework.BubbleNetwork;
import com.thebubblenetwork.api.global.bubblepackets.messaging.messages.request.PlayerDataRequest;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Copyright Statement
 * ----------------------
 * Copyright (C) The Bubble Network, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Wrote by Jacob Evans <jacobevansminor@gmail.com>, 01 2016
 * <p/>
 * <p/>
 * Class information
 * ---------------------
 * Package: com.thebubblenetwork.api.framework.interaction
 * Date-created: 30/01/2016 00:19
 * Project: BubbleFramework
 */
public class DataRequestTask extends BukkitRunnable {
    public static void setData(String name, Map<String, String> received) {
        if (taskMap.containsKey(name.toLowerCase())) {
            taskMap.remove(name.toLowerCase()).setResult(received);
            BubbleNetwork.getInstance().getLogger().log(Level.INFO, "Received pending data for " + name);
        } else {
            BubbleNetwork.getInstance().getLogger().log(Level.WARNING, "Set data for invalid player " + name);
        }
    }

    public static Map<String, String> requestAsync(String name) {
        BubbleNetwork.getInstance().getLogger().log(Level.INFO, "Requesting data for " + name);
        DataRequestTask task = new DataRequestTask(name);
        taskMap.put(name.toLowerCase(), task);
        task.runTask(BubbleNetwork.getInstance().getPlugin());
        while (taskMap.containsKey(name.toLowerCase())) {

        }
        BubbleNetwork.getInstance().getLogger().log(Level.INFO, "Found data for " + name);
        return task.getResult();
    }

    private static ConcurrentMap<String, DataRequestTask> taskMap = new ConcurrentHashMap<>();
    private String name;
    private Map<String, String> result = null;

    private DataRequestTask(String name) {
        this.name = name;
    }

    public void run() {
        try {
            BubbleNetwork.getInstance().getPacketHub().sendMessage(BubbleNetwork.getInstance().getProxy(), new PlayerDataRequest(getName()));
        } catch (IOException e) {
            result = new HashMap<>();
        }
    }

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(Map<String, String> result) {
        this.result = result;
    }

    public String getName() {
        return name;
    }
}
