package com.alma.improved_original.datagen;

import com.alma.improved_original.ImprovedOriginal;
import com.alma.improved_original.block.ModBlocks;
import com.alma.improved_original.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModZhCnLangProvider extends LanguageProvider {
    public ModZhCnLangProvider(PackOutput output) {
        super(output, ImprovedOriginal.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add(ModItems.RUBY.get(), "红宝石");
        add(ModItems.SAPPHIRE.get(), "蓝宝石");
        add(ModItems.TOPAZ.get(), "黄宝石");
        add(ModItems.AMETHYST.get(), "紫宝石");
        add(ModItems.ONYX.get(), "黑宝石");

        add(ModBlocks.RUBY_BLOCK.get(), "红宝石块");
        add(ModBlocks.SAPPHIRE_BLOCK.get(), "蓝宝石块");
        add(ModBlocks.TOPAZ_BLOCK.get(), "黄宝石块");
        add(ModBlocks.AMETHYST_BLOCK.get(), "紫宝石块");
        add(ModBlocks.ONYX_BLOCK.get(), "黑宝石块");

        add(ModBlocks.RUBY_ORE.get(), "红宝石矿石");
        add(ModBlocks.SAPPHIRE_ORE.get(), "蓝宝石矿石");
        add(ModBlocks.TOPAZ_ORE.get(), "黄宝石矿石");
        add(ModBlocks.AMETHYST_ORE.get(), "紫宝石矿石");
        add(ModBlocks.ONYX_ORE.get(), "黑宝石矿石");

        add(ModBlocks.DEEPSLATE_RUBY_ORE.get(), "深层红宝石矿石");
        add(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), "深层蓝宝石矿石");
        add(ModBlocks.DEEPSLATE_TOPAZ_ORE.get(), "深层黄宝石矿石");
        add(ModBlocks.DEEPSLATE_AMETHYST_ORE.get(), "深层紫宝石矿石");
        add(ModBlocks.DEEPSLATE_ONYX_ORE.get(), "深层黑宝石矿石");

        add("itemGroup.gemstones_tab", "宝石");

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
        add("quest.improved_original.toast.title", "任务完成！");
        add("quest.improved_original.toast.desc", "%s - +%d 绿宝石");
        add("quest.improved_original.refresh_notify", "每日任务已刷新！");
        add("quest.improved_original.refresh_button", "刷新 (%d 绿宝石)");
        add("quest.improved_original.refresh.success", "任务已刷新！花费: %d 绿宝石。");
        add("quest.improved_original.refresh.no_emeralds", "你需要 %d 个绿宝石来刷新任务！");
        add("quest.improved_original.countdown", "刷新倒计时: %02d:%02d");
    }
}
