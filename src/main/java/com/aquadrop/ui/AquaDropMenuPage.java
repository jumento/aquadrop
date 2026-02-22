package com.aquadrop.ui;

import com.aquadrop.AquaDrop;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class AquaDropMenuPage extends InteractiveCustomUIPage<AquaDropMenuPage.MenuEventData> {

    public static class MenuEventData {
        public String action;

        public static final BuilderCodec<MenuEventData> CODEC = BuilderCodec
                .builder(MenuEventData.class, MenuEventData::new)
                .append(
                        new KeyedCodec<>("Action", Codec.STRING),
                        (MenuEventData o, String v) -> o.action = v,
                        (MenuEventData o) -> o.action)
                .add()
                .build();
    }

    public AquaDropMenuPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, MenuEventData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store) {
        cmd.append("Pages/AquaDropMenu.ui");

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReload",
                new EventData().append("Action", "reload"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBlockDrops",
                new EventData().append("Action", "block"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BtnMobDrops",
                new EventData().append("Action", "mob"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose",
                new EventData().append("Action", "close"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull MenuEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (data.action == null)
            return;

        switch (data.action) {
            case "reload":
                AquaDrop.get().reloadDrops();
                playerRef.sendMessage(Message.empty().insert("AquaDrop Hot-Reloaded successfully!").color("#FFD700"));
                player.getPageManager().setPage(ref, store, Page.None);
                break;
            case "block":
                player.getPageManager().openCustomPage(ref, store, new AquaDropFormPage(playerRef, true));
                break;
            case "mob":
                player.getPageManager().openCustomPage(ref, store, new AquaDropFormPage(playerRef, false));
                break;
            case "close":
                player.getPageManager().setPage(ref, store, Page.None);
                break;
        }
    }
}
