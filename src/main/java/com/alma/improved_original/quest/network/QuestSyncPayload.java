// 统一 S2C 任务同步包：替代 S2CQuestSyncPayload
// 携带完整 QuestData + 可选的完成信息（触发 toast）+ openScreen 标志
package com.alma.improved_original.quest.network;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.quest.QuestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record QuestSyncPayload(QuestData data, Optional<QuestCompletion> completion, boolean openScreen,
                               List<QuestData.ChainProgress> completedChains)
        implements CustomPacketPayload {

    public record QuestCompletion(String questDescription, String rewardText) {
        public static final StreamCodec<FriendlyByteBuf, QuestCompletion> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, QuestCompletion::questDescription,
                        ByteBufCodecs.STRING_UTF8, QuestCompletion::rewardText,
                        QuestCompletion::new
                );
    }

    public static final CustomPacketPayload.Type<QuestSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ImprovedOriginal.MOD_ID, "quest_sync"));

    // Helper: wrap ChainProgress list for StreamCodec since list codec needs explicit types
    private static final StreamCodec<FriendlyByteBuf, List<QuestData.ChainProgress>> CHAIN_LIST_STREAM_CODEC =
            QuestData.ChainProgress.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<FriendlyByteBuf, QuestSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    QuestData.STREAM_CODEC, QuestSyncPayload::data,
                    ByteBufCodecs.optional(QuestCompletion.STREAM_CODEC), QuestSyncPayload::completion,
                    ByteBufCodecs.BOOL, QuestSyncPayload::openScreen,
                    CHAIN_LIST_STREAM_CODEC, QuestSyncPayload::completedChains,
                    QuestSyncPayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static QuestSyncPayload syncOnly(QuestData data) {
        return new QuestSyncPayload(data, Optional.empty(), false, List.of());
    }

    public static QuestSyncPayload openScreen(QuestData data) {
        return new QuestSyncPayload(data, Optional.empty(), true, List.of());
    }

    public static QuestSyncPayload withCompletion(QuestData data, QuestCompletion completion) {
        return new QuestSyncPayload(data, Optional.of(completion), false, List.of());
    }

    public static QuestSyncPayload chainPanel(QuestData data, List<QuestData.ChainProgress> completedChains) {
        return new QuestSyncPayload(data, Optional.empty(), false, completedChains);
    }
}
