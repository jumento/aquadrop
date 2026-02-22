package com.aquadrop.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class AquaDropCommand extends AbstractPlayerCommand {

    public AquaDropCommand() {
        super("aquadrop", "Base command for AquaDrop Administration", true); // true to allow subcommands
        this.addSubCommand(new AquaDropReloadCommand());
        this.addSubCommand(new AquaDropConfigCommand());
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {
        // Base command alone will just guide the user
        playerRef.sendMessage(com.hypixel.hytale.server.core.Message.empty()
                .insert("AquaDrop Commands:\n /aquadrop config\n /aquadrop reload").color("#FFD700"));
    }
}
