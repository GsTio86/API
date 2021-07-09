package com.envyful.api.forge.gui;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.forge.listener.LazyListener;
import com.envyful.api.player.EnvyPlayer;
import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.UUID;

/**
 *
 * A class to track all open {@link ForgeGui}s and update them every tick (to update any changed items after player clicks)
 *
 */
public class ForgeGuiTracker {

    private static final Map<UUID, ForgeGui> OPEN_GUIS = Maps.newConcurrentMap();

    static {
        new ForgeGuiTickListener();
    }

    public static void addGui(EnvyPlayer<?> player, ForgeGui gui) {
        OPEN_GUIS.put(player.getUuid(), gui);
    }

    public static void removePlayer(EnvyPlayer<?> player) {
        OPEN_GUIS.remove(player.getUuid());
    }

    private static final class ForgeGuiTickListener extends LazyListener {

        private int tick = 0;

        private ForgeGuiTickListener() {
            super();
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (tick % 10 != 0) {
                return;
            }

            ++tick;

            UtilConcurrency.runAsync(() -> {
                for (ForgeGui value : OPEN_GUIS.values()) {
                    value.update();
                }
            });
        }
    }
}