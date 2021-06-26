package com.envyful.api.forge.player;

import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Forge implementation of the {@link EnvyPlayer} interface
 *
 */
public class ForgeEnvyPlayer implements EnvyPlayer<EntityPlayerMP> {

    private final EntityPlayerMP player;

    protected final Map<Class<?>, PlayerAttribute<?>> attributes = Maps.newHashMap();

    protected ForgeEnvyPlayer(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return this.player.getUniqueID();
    }

    @Override
    public EntityPlayerMP getParent() {
        return this.player;
    }

    @Override
    public void message(String message) {
        this.player.sendMessage(new TextComponentString(message));
    }

    @Override
    public void message(String... messages) {
        for (String message : messages) {
            this.message(message);
        }
    }

    @Override
    public void message(List<String> messages) {
        for (String message : messages) {
            this.message(message);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends PlayerAttribute<B>, B> A getAttribute(Class<B> plugin) {
        return (A) this.attributes.get(plugin);
    }
}