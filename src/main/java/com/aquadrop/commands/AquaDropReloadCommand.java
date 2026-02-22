package com.aquadrop.commands;

import com.aquadrop.AquaDrop;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class AquaDropReloadCommand extends AbstractPlayerCommand {

    public AquaDropReloadCommand() {
        super("reload", "Reloads the AquaDrop configurations from json files", false);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {

        AquaDrop.get().reloadDrops();
        playerRef.sendMessage(
                Message.empty().insert("AquaDrop configurations have been reloaded dynamically.").color("#FFD700"));
    }
}
