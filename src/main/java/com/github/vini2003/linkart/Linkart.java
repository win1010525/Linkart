package com.github.vini2003.linkart;

import com.github.vini2003.linkart.configuration.LinkartConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Linkart implements ModInitializer {
    public static final String ID = "linkart";
    public static LinkartConfiguration CONFIG;
    public static final Map<PlayerEntity, AbstractMinecartEntity> LINKING_CARTS = new HashMap<>();
    public static final Map<PlayerEntity, AbstractMinecartEntity> UNLINKING_CARTS = new HashMap<>();
    public static final Tag<Item> LINKERS = TagFactory.ITEM.create(new Identifier(ID, "linkers"));

    static {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path config = FabricLoader.getInstance().getConfigDir().resolve("linkart.json");
        if (Files.exists(config)) {
            try {
                CONFIG = gson.fromJson(Files.newBufferedReader(config), LinkartConfiguration.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            CONFIG = new LinkartConfiguration();
            try {
                Files.createFile(config);
                Files.write(config, gson.toJson(CONFIG).getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onInitialize() {
    }
}
