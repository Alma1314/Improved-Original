package com.alma.improved_original.quest.command;

import com.alma.improved_original.Config;
import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.ModAttachments;
import com.alma.improved_original.quest.QuestData;
import com.alma.improved_original.quest.engine.QuestEngine;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ImprovedOriginal.MOD_ID)
public class QuestCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("quest")
                        .executes(ctx -> openQuestScreen(ctx.getSource()))
                        .then(Commands.literal("lock")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(1, Config.getTotalSlotCount()))
                                        .executes(ctx -> lockQuest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "slot")))))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(1, Config.getTotalSlotCount()))
                                        .executes(ctx -> unlockQuest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "slot")))))
        );
    }

    private static int openQuestScreen(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            QuestEngine.get().handleOpenScreenPacket(player);
        }
        return 1;
    }

    private static int lockQuest(CommandSourceStack source, int slot) {
        if (source.getEntity() instanceof ServerPlayer player) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            QuestEngine.get().lockSlot(player, data, slot - 1);
        }
        return 1;
    }

    private static int unlockQuest(CommandSourceStack source, int slot) {
        if (source.getEntity() instanceof ServerPlayer player) {
            QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
            QuestEngine.get().unlockSlot(player, data, slot - 1);
        }
        return 1;
    }
}
