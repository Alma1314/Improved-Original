// 数据生成-中文翻译：物品/方块名、任务系统所有文本
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
        add("key.categories.improved_original", "原版增强");
        add("key.improved_original.open_quest", "打开任务面板");
        add("screen.improved_original.quest", "每日任务");
        add("quest.improved_original.empty_slot", "暂无任务");
        add("quest.improved_original.reward", "奖励: %d %s");
        add("quest.improved_original.lock_button", "锁定");
        add("quest.improved_original.locked", "已锁定");
        add("quest.improved_original.done", "关闭");
        add("quest.improved_original.lock_cost_hint", "(%d 绿宝石)");

        add("quest.improved_original.desc.break", "破坏 %s x%d");
        add("quest.improved_original.desc.craft", "合成 %s x%d");
        add("quest.improved_original.desc.kill", "击杀 %s x%d");
        add("quest.improved_original.desc.collect", "收集 %s x%d");
        add("quest.improved_original.desc.find", "探索 %s");

        add("quest.improved_original.lock.success", "槽位 %d 的任务已锁定!");
        add("quest.improved_original.lock.no_emeralds", "你需要 %d 个绿宝石来锁定任务!");
        add("quest.improved_original.lock.already_locked", "该任务已被锁定。");
        add("quest.improved_original.lock.no_quest", "该槽位没有任务。");
        add("quest.improved_original.lock.completed", "无法锁定已完成的任务。");
        add("quest.improved_original.unlock.success", "槽位 %d 的任务已解锁。");
        add("quest.improved_original.unlock.not_locked", "该任务未锁定。");
        add("quest.improved_original.complete_success", "完成任务: %s! 获得 %d 绿宝石。");
        add("quest.improved_original.toast.title", "任务完成！");
        add("quest.improved_original.toast.desc", "%s - %s");
        add("quest.improved_original.refresh_notify", "每日任务已刷新！");
        add("quest.improved_original.refresh_button", "刷新 (%d 绿宝石)");
        add("quest.improved_original.refresh.success", "任务已刷新！花费: %d 绿宝石。");
        add("quest.improved_original.refresh.no_emeralds", "你需要 %d 个绿宝石来刷新任务！");
        add("quest.improved_original.countdown", "刷新倒计时: %02d:%02d");

        // Quest names and descriptions
        add("quest.improved_original.name.minecraft.stone", "石矿工");
        add("quest.improved_original.desc_text.minecraft.stone", "开采石头，获取绿宝石");
        add("quest.improved_original.name.minecraft.oak_log", "伐木工");
        add("quest.improved_original.desc_text.minecraft.oak_log", "砍伐橡树原木");
        add("quest.improved_original.name.minecraft.coal_ore", "煤矿工");
        add("quest.improved_original.desc_text.minecraft.coal_ore", "深入地下开采煤矿");
        add("quest.improved_original.name.minecraft.iron_ore", "铁矿工");
        add("quest.improved_original.desc_text.minecraft.iron_ore", "从大地中提取铁矿石");
        add("quest.improved_original.name.minecraft.dirt", "挖土工");
        add("quest.improved_original.desc_text.minecraft.dirt", "挖掘泥土方块");
        add("quest.improved_original.name.minecraft.deepslate", "深板岩挖掘者");
        add("quest.improved_original.desc_text.minecraft.deepslate", "深入地下挖掘深板岩");
        add("quest.improved_original.name.minecraft.sand", "采沙工");
        add("quest.improved_original.desc_text.minecraft.sand", "从沙滩和沙漠收集沙子");
        add("quest.improved_original.name.minecraft.gravel", "采砾工");
        add("quest.improved_original.desc_text.minecraft.gravel", "从河床收集砂砾");
        add("quest.improved_original.name.minecraft.netherrack", "下界矿工");
        add("quest.improved_original.desc_text.minecraft.netherrack", "在下界开采下界岩");

        add("quest.improved_original.name.minecraft.zombie", "僵尸猎手");
        add("quest.improved_original.desc_text.minecraft.zombie", "击败僵尸，保卫村庄");
        add("quest.improved_original.name.minecraft.skeleton", "骷髅猎人");
        add("quest.improved_original.desc_text.minecraft.skeleton", "远程击倒骷髅");
        add("quest.improved_original.name.minecraft.spider", "蜘蛛杀手");
        add("quest.improved_original.desc_text.minecraft.spider", "消灭蜘蛛和它们的网");
        add("quest.improved_original.name.minecraft.creeper", "苦力怕克星");
        add("quest.improved_original.desc_text.minecraft.creeper", "在苦力怕爆炸前将其消灭");
        add("quest.improved_original.name.minecraft.enderman", "末影征服者");
        add("quest.improved_original.desc_text.minecraft.enderman", "挑战高大黑暗的末影人");
        add("quest.improved_original.name.minecraft.witch", "女巫终结者");
        add("quest.improved_original.desc_text.minecraft.witch", "阻止女巫和她的药水");
        add("quest.improved_original.name.minecraft.drowned", "溺尸猎手");
        add("quest.improved_original.desc_text.minecraft.drowned", "在深水中猎杀溺尸");
        add("quest.improved_original.name.minecraft.husk", "尸壳清除者");
        add("quest.improved_original.desc_text.minecraft.husk", "在沙漠中清除尸壳");

        add("quest.improved_original.name.minecraft.crafting_table", "工作台工匠");
        add("quest.improved_original.desc_text.minecraft.crafting_table", "合成基础工作台");
        add("quest.improved_original.name.minecraft.furnace", "熔炉工匠");
        add("quest.improved_original.desc_text.minecraft.furnace", "合成一个熔炉");
        add("quest.improved_original.name.minecraft.iron_pickaxe", "工具锻造师");
        add("quest.improved_original.desc_text.minecraft.iron_pickaxe", "锻造一把铁镐");
        add("quest.improved_original.name.minecraft.iron_sword", "武器锻造师");
        add("quest.improved_original.desc_text.minecraft.iron_sword", "锻造一把铁剑");
        add("quest.improved_original.name.minecraft.torch", "火把匠人");
        add("quest.improved_original.desc_text.minecraft.torch", "用火把照亮黑暗");
        add("quest.improved_original.name.minecraft.bread", "面包师");
        add("quest.improved_original.desc_text.minecraft.bread", "用小麦烘焙面包");
        add("quest.improved_original.name.minecraft.stick", "木工");
        add("quest.improved_original.desc_text.minecraft.stick", "用木板制作木棍");
        add("quest.improved_original.name.minecraft.iron_chestplate", "盔甲锻造师");
        add("quest.improved_original.desc_text.minecraft.iron_chestplate", "锻造一件铁胸甲");

        add("quest.improved_original.name.minecraft.coal", "煤炭收集者");
        add("quest.improved_original.desc_text.minecraft.coal", "通过挖矿或战利品收集煤炭");
        add("quest.improved_original.name.minecraft.iron_ingot", "铁锭收藏家");
        add("quest.improved_original.desc_text.minecraft.iron_ingot", "收集冶炼的铁锭");
        add("quest.improved_original.name.minecraft.wheat", "小麦农夫");
        add("quest.improved_original.desc_text.minecraft.wheat", "从你的农场收割小麦");
        add("quest.improved_original.name.minecraft.apple", "苹果采集者");
        add("quest.improved_original.desc_text.minecraft.apple", "从橡树收集苹果");
        add("quest.improved_original.name.minecraft.rotten_flesh", "腐肉收集者");
        add("quest.improved_original.desc_text.minecraft.rotten_flesh", "从亡灵生物收集腐肉");
        add("quest.improved_original.name.minecraft.bone", "骨头收集者");
        add("quest.improved_original.desc_text.minecraft.bone", "从骷髅收集骨头");
        add("quest.improved_original.name.minecraft.gunpowder", "火药收集者");
        add("quest.improved_original.desc_text.minecraft.gunpowder", "从苦力怕收集火药");
        add("quest.improved_original.name.minecraft.ender_pearl", "珍珠搜寻者");
        add("quest.improved_original.desc_text.minecraft.ender_pearl", "从末影人收集末影珍珠");

        add("quest.improved_original.name.minecraft.village_plains", "村庄探险家");
        add("quest.improved_original.desc_text.minecraft.village_plains", "找到一个平原村庄");
        add("quest.improved_original.name.minecraft.village_desert", "沙漠探险家");
        add("quest.improved_original.desc_text.minecraft.village_desert", "发现一个沙漠村庄");
        add("quest.improved_original.name.minecraft.village_savanna", "草原探险家");
        add("quest.improved_original.desc_text.minecraft.village_savanna", "定位一个热带草原村庄");
        add("quest.improved_original.name.minecraft.village_taiga", "针叶林探险家");
        add("quest.improved_original.desc_text.minecraft.village_taiga", "找到针叶林村庄");
        add("quest.improved_original.name.minecraft.village_snowy", "雪原探险家");
        add("quest.improved_original.desc_text.minecraft.village_snowy", "冒着严寒找到雪原村庄");
        add("quest.improved_original.name.minecraft.desert_pyramid", "金字塔探险者");
        add("quest.improved_original.desc_text.minecraft.desert_pyramid", "发现沙漠神殿");
        add("quest.improved_original.name.minecraft.jungle_pyramid", "丛林探险者");
        add("quest.improved_original.desc_text.minecraft.jungle_pyramid", "在丛林中找到隐藏的丛林神庙");
        add("quest.improved_original.name.minecraft.pillager_outpost", "前哨站侦察兵");
        add("quest.improved_original.desc_text.minecraft.pillager_outpost", "侦察掠夺者前哨站");
        add("quest.improved_original.name.minecraft.mineshaft", "矿井探险者");
        add("quest.improved_original.desc_text.minecraft.mineshaft", "发现废弃矿井");
        add("quest.improved_original.name.minecraft.stronghold", "要塞追寻者");
        add("quest.improved_original.desc_text.minecraft.stronghold", "找到隐藏的要塞");
        add("quest.improved_original.name.minecraft.ruined_portal", "传送门发现者");
        add("quest.improved_original.desc_text.minecraft.ruined_portal", "定位废弃的下界传送门");
        add("quest.improved_original.name.minecraft.ocean_ruin_cold", "海洋探险者");
        add("quest.improved_original.desc_text.minecraft.ocean_ruin_cold", "探索冷水海洋遗迹");
        add("quest.improved_original.name.minecraft.shipwreck", "沉船打捞者");
        add("quest.improved_original.desc_text.minecraft.shipwreck", "在海底找到沉船");
        add("quest.improved_original.name.minecraft.buried_treasure", "宝藏猎人");
        add("quest.improved_original.desc_text.minecraft.buried_treasure", "寻找埋藏的宝藏");
        add("quest.improved_original.name.minecraft.swamp_hut", "沼泽探险者");
        add("quest.improved_original.desc_text.minecraft.swamp_hut", "发现沼泽小屋");
        add("quest.improved_original.name.minecraft.igloo", "雪屋探索者");
        add("quest.improved_original.desc_text.minecraft.igloo", "在冰原找到雪屋");

        // JEI integration
        add("quest.improved_original.jei.category", "每日任务");
        add("quest.improved_original.jei.targets_title", "提交");
        add("quest.improved_original.jei.rewards_title", "奖励");
        add("quest.improved_original.jei.scroll_hint", "滚动查看更多物品");
        add("quest.improved_original.jei.no_description", "暂无简介");

        // Gem exchange quest
        add("quest.improved_original.name.gem_exchange", "宝石收藏家");
        add("quest.improved_original.desc_text.gem_exchange", "收集全部6种宝石换取一颗钻石");
    }
}
