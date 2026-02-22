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

public class AquaDropFormPage extends InteractiveCustomUIPage<AquaDropFormPage.FormEventData> {

    private final boolean isBlockForm;

    public static class FormEventData {
        public String action;
        public String sourceId;
        public String dropId;
        public String prob;
        public String quantity;

        public static final BuilderCodec<FormEventData> CODEC = BuilderCodec
                .builder(FormEventData.class, FormEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (FormEventData o, String v) -> o.action = v,
                        (FormEventData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("@SourceId", Codec.STRING), (FormEventData o, String v) -> o.sourceId = v,
                        (FormEventData o) -> o.sourceId)
                .add()
                .append(new KeyedCodec<>("@DropId", Codec.STRING), (FormEventData o, String v) -> o.dropId = v,
                        (FormEventData o) -> o.dropId)
                .add()
                .append(new KeyedCodec<>("@Probability", Codec.STRING), (FormEventData o, String v) -> o.prob = v,
                        (FormEventData o) -> o.prob)
                .add()
                .append(new KeyedCodec<>("@Quantity", Codec.STRING), (FormEventData o, String v) -> o.quantity = v,
                        (FormEventData o) -> o.quantity)
                .add()
                .build();
    }

    public AquaDropFormPage(@Nonnull PlayerRef playerRef, boolean isBlockForm) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, FormEventData.CODEC);
        this.isBlockForm = isBlockForm;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store) {
        cmd.append("Pages/AquaDropForm.ui");

        String title = isBlockForm ? "Add / Edit Block Drop" : "Add / Edit Mob Drop";
        cmd.set("#FormTitle.Text", title);

        String placeholder = isBlockForm ? "E.g. Example_Block_ID" : "E.g. Example_Mob_ID";
        cmd.set("#InputSourceId.PlaceholderText", placeholder);

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSave", new EventData()
                .append("Action", "save")
                .append("@SourceId", "#InputSourceId.Value")
                .append("@DropId", "#InputDropId.Value")
                .append("@Probability", "#InputProbability.Value")
                .append("@Quantity", "#InputQuantity.Value"));

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCancel", new EventData()
                .append("Action", "cancel"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull FormEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (data.action == null)
            return;

        if ("save".equals(data.action)) {
            try {
                if (data.sourceId == null || data.sourceId.trim().isEmpty()) {
                    data.sourceId = isBlockForm ? "Example_Block_ID" : "Example_Mob_ID";
                }
                if (data.dropId == null || data.dropId.trim().isEmpty()) {
                    data.dropId = "Example_Item_Drop_ID";
                }
                float probability = 100f;
                if (data.prob != null && !data.prob.isEmpty()) {
                    probability = Float.parseFloat(data.prob);
                }

                int quantity = 1;
                if (data.quantity != null && !data.quantity.trim().isEmpty()) {
                    quantity = Integer.parseInt(data.quantity);
                }

                // Call loader to save to JSON physically
                new com.aquadrop.core.registry.LocalConfigLoader(
                        (com.aquadrop.core.registry.CustomDropRegistryImpl) AquaDrop.get().getRegistry(),
                        AquaDrop.get())
                        .addAndSaveDropConfig(data.sourceId, data.dropId, probability, quantity, isBlockForm);

                playerRef.sendMessage(Message.empty()
                        .insert("Drop saved successfully. (System has been automatically hot-reloaded).")
                        .color("#FFD700"));
                player.getPageManager().setPage(ref, store, Page.None);

            } catch (NumberFormatException ex) {
                playerRef.sendMessage(Message.empty()
                        .insert("Error: Probability (" + data.prob + ") is invalid. Try 100.0, 50.5, etc.")
                        .color("#FFD700"));
            } catch (Exception ex) {
                playerRef.sendMessage(
                        Message.empty().insert("Fatal error saving drop: " + ex.getMessage()).color("#FFD700"));
            }
        } else if ("cancel".equals(data.action)) {
            // Retroceder al menú principal
            player.getPageManager().openCustomPage(ref, store, new AquaDropMenuPage(playerRef));
        }
    }
}
