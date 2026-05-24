// 玩家数据附加器：注册AttachmentType<QuestData>，为每个玩家附加独立的每日任务数据
// NeoForge Attachment 系统：服务端数据自动持久化到玩家NBT，无需手动保存
// QuestData.CODEC 负责序列化/反序列化，确保退出重进后任务数据不丢失
package com.alma.improved_original.quest;

import com.alma.improved_original.ImprovedOriginal;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ImprovedOriginal.MOD_ID);

    public static final Supplier<AttachmentType<QuestData>> QUEST_DATA =
            ATTACHMENT_TYPES.register("quest_data",
                    () -> AttachmentType.builder(QuestData::createFresh)
                            .serialize(QuestData.CODEC)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
