package com.envyful.api.reforged.pixelmon.sprite;

import com.envyful.api.forge.items.ItemBuilder;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UtilSprite {

    private static final Map<EnumSpecies, String> ABILITIES = Maps.newHashMap();

    public static ItemStack getPokemonSprite(final Pokemon pokemon, final Function<Pokemon, List<String>> loreFunction) {
        ItemStack itemStack = getPixelmonSprite(pokemon);
        String nickname = pokemon.getNickname();
        itemStack.setStackDisplayName("§b" + pokemon.getSpecies().getLocalizedName() + ((nickname != null && !nickname.isEmpty()) ? " (" + nickname + ")" : "") + "");
        NBTTagCompound compound = itemStack.getOrCreateSubCompound("display");
        NBTTagList lore = new NBTTagList();

        for (String s : loreFunction.apply(pokemon)) {
            lore.appendTag(new NBTTagString(s));
        }

        compound.setTag("Lore", lore);
        itemStack.setTagInfo("display", compound);

        return itemStack;
    }

    public static ItemStack getStarterPixelmonSprite(EnumSpecies pokemon, String nameColour) {
        ItemStack itemStack = new ItemBuilder().type(PixelmonItems.itemPixelmonSprite)
                .name(nameColour + pokemon.getLocalizedName())
                .lore(
                        "§fGeneration: " + nameColour + pokemon.getGeneration(),
                        "§fType: " + nameColour + pokemon.getBaseStats().getType1().getLocalizedName(),
                        "§fAbilities: " + nameColour + getAbilities(pokemon)
                )
                .build();

        itemStack.setTagInfo("ndex", new NBTTagShort((short) pokemon.getNationalPokedexInteger()));
        return itemStack;
    }

    private static String getAbilities(EnumSpecies species) {
        if (ABILITIES.containsKey(species)) {
            return ABILITIES.get(species);
        }

        StringBuilder builder = new StringBuilder();
        String[] abilities = species.getBaseStats().abilities;

        for (int i = 0; i < 3; i++) {
            if (abilities.length <= i || abilities[i] == null) {
                continue;
            }

            builder.append(abilities[i]);

            if (i == 2) {
                builder.append(" (HA)");
            }

            builder.append(", ");
        }

        ABILITIES.put(species, builder.substring(0, builder.length() - 2));
        return builder.substring(0, builder.length() - 2);
    }

    public static ItemStack getPixelmonSprite(EnumSpecies pokemon) {
        ItemStack itemStack = new ItemStack(PixelmonItems.itemPixelmonSprite);
        itemStack.setStackDisplayName("§b" + pokemon.getLocalizedName());
        itemStack.setTagInfo("ndex", new NBTTagShort((short) pokemon.getNationalPokedexInteger()));
        return itemStack;
    }

    public static ItemStack getPokemonElement(Pokemon pokemon) {
        ItemStack itemStack = getPixelmonSprite(pokemon);
        itemStack.setStackDisplayName("§b" + pokemon.getSpecies().getLocalizedName() + (!pokemon.getDisplayName().isEmpty() ? "(" + pokemon.getDisplayName() + ")" : "") + "");
        NBTTagCompound compound = itemStack.getOrCreateSubCompound("display");
        NBTTagList lore = new NBTTagList();

        for (String s : getPokemonDesc(pokemon)) {
            lore.appendTag(new NBTTagString(s));
        }

        compound.setTag("Lore", lore);
        itemStack.setTagInfo("display", compound);

        return itemStack;
    }

    public static ItemStack getPixelmonSprite(Pokemon pokemon) {
        ItemStack itemStack = new ItemStack(PixelmonItems.itemPixelmonSprite);
        itemStack.setTagInfo("ndex", new NBTTagShort((short) pokemon.getSpecies().getNationalPokedexInteger()));
        itemStack.setTagInfo("form", new NBTTagByte((byte) pokemon.getForm()));
        itemStack.setTagInfo("gender", new NBTTagByte(pokemon.getGender().getForm()));
        itemStack.setTagInfo("Shiny", new NBTTagByte(pokemon.isShiny() ? (byte) 1 : (byte) 0));

        if (pokemon.getFormEnum() != EnumSpecial.Base) {
            itemStack.setTagInfo("specialTexture", new NBTTagByte(pokemon.getFormEnum().getForm()));
        }

        if (pokemon.getNickname() != null && !pokemon.getNickname().isEmpty()) {
            itemStack.setTagInfo("Nickname", new NBTTagString(pokemon.getNickname()));
        }

        return itemStack;
    }

    public static List<String> getPokemonDesc(Pokemon pokemon) {
        List<String> lore = new ArrayList<>();

        lore.add("§7Nature: §b" + pokemon.getNature().name());
        lore.add("§7Ability: §b" + pokemon.getAbility().getName());
        lore.add("§7Friendship: §b" + pokemon.getFriendship());

        float ivHP = pokemon.getIVs().get(StatsType.HP);
        float ivAtk = pokemon.getIVs().get(StatsType.Attack);
        float ivDef = pokemon.getIVs().get(StatsType.Defence);
        float ivSpeed = pokemon.getIVs().get(StatsType.Speed);
        float ivSAtk = pokemon.getIVs().get(StatsType.SpecialAttack);
        float ivSDef = pokemon.getIVs().get(StatsType.SpecialDefence);
        int percentage = Math.round(((ivHP + ivDef + ivAtk + ivSpeed + ivSAtk + ivSDef) / 186f) * 100);

        lore.add("§7IVs " + "(§b" + percentage + "%§7):");
        lore.add("    §7HP: §b" + (int) ivHP + " §d| §7Atk: §b" + (int) ivAtk + " §d| §7Def: §b" + (int) ivDef);
        lore.add("    §7SAtk: §b" + (int) ivSAtk + " §d| §7SDef: §b" + (int) ivSDef + " §d| §7Spd: §b" + (int) ivSpeed);

        float evHP = pokemon.getEVs().get(StatsType.HP);
        float evAtk = pokemon.getEVs().get(StatsType.Attack);
        float evDef = pokemon.getEVs().get(StatsType.Defence);
        float evSpeed = pokemon.getEVs().get(StatsType.Speed);
        float evSAtk = pokemon.getEVs().get(StatsType.SpecialAttack);
        float evSDef = pokemon.getEVs().get(StatsType.SpecialDefence);

        lore.add("§7EVs:");
        lore.add("    §7HP: §b" + (int) evHP + " §d| §7Atk: §b" + ((int) evAtk) + " §d| §7Def: §b" + (int) evDef);
        lore.add("    §7SAtk: §b" + (int) evSAtk + " §d| §7SDef: §b" + ((int) evSDef) + " §d| §7Spd: §b" + (int) evSpeed);
        lore.add("§7Moves:");

        if (pokemon.getMoveset() != null) {
            for (Attack attack : pokemon.getMoveset().attacks) {
                if (attack != null) {
                    lore.add("    §b" + attack.getActualMove().getAttackName());
                }
            }
        }

        return lore;
    }
}