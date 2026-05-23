package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModZhCnLangProvider extends LanguageProvider {
    public ModZhCnLangProvider(PackOutput output) {
        super(output, ImprovedOriginal.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        // Quest system
        add("screen.improved_original.quest", "每日任务");
        add("quest.improved_original.empty_slot", "暂无任务");
        add("quest.improved_original.reward", "奖励: %d 绿宝石");
        add("quest.improved_original.lock_button", "锁定");
        add("quest.improved_original.locked", "已锁定");
        add("quest.improved_original.done", "关闭");
        add("quest.improved_original.lock_cost_hint", "(%d 绿宝石)");

        add("quest.improved_original.desc.break", "破坏 %s x%d");
        add("quest.improved_original.desc.craft", "合成 %s x%d");
        add("quest.improved_original.desc.kill", "击杀 %s x%d");
        add("quest.improved_original.desc.collect", "收集 %s x%d");

        add("quest.improved_original.lock.success", "槽位 %d 的任务已锁定!");
        add("quest.improved_original.lock.no_emeralds", "你需要 %d 个绿宝石来锁定任务!");
        add("quest.improved_original.lock.already_locked", "该任务已被锁定。");
        add("quest.improved_original.lock.no_quest", "该槽位没有任务。");
        add("quest.improved_original.lock.completed", "无法锁定已完成的任务。");
        add("quest.improved_original.unlock.success", "槽位 %d 的任务已解锁。");
        add("quest.improved_original.unlock.not_locked", "该任务未锁定。");
        add("quest.improved_original.complete_success", "完成任务: %s! 获得 %d 绿宝石。");
    }
}
