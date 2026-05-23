# Minecraft原版增强mod
一、项目结构
code
26 lines
Copy
src/main/java/com/alma/improved_original/

├── ImprovedOriginal.java          # 主模组类 (注册所有组件)

├── ImprovedOriginalClient.java    # 客户端类 (配置界面)

├── Config.java                     # 配置文件 (任务相关配置)

├── block/ModBlocks.java            # 方块注册

├── item/ModItems.java              # 物品注册

├── quest/

│   ├── QuestType.java              # 任务类型枚举

│   ├── QuestDefinition.java        # 任务定义 (不可变数据)

│   ├── QuestSlotData.java          # 单个槽位状态

│   ├── QuestData.java              # 玩家完整任务数据 (3槽位+刷新时间)

│   ├── ModAttachments.java         # 玩家数据附加器注册

│   ├── QuestManager.java           # 核心逻辑 (生成/刷新/进度/锁定/奖励)

│   ├── command/QuestCommand.java   # /quest 命令

│   ├── network/

│   │   ├── S2CQuestSyncPayload.java   # 服务端→客户端同步包

│   │   ├── C2SQuestLockPayload.java   # 客户端→服务端锁定包

│   │   └── ModPayloadHandlers.java    # 网络包注册

│   ├── event/

│   │   ├── QuestEventHandlers.java    # 游戏事件监听
Show 6 more lines
二、核心数据模型
code
20 lines
Copy
QuestType (枚举)

├── BREAK_BLOCK  (破坏方块)

├── CRAFT_ITEM   (合成物品)

├── KILL_ENTITY  (击杀实体)

└── COLLECT_ITEM (拾取物品)


QuestDefinition (不可变记录) → 一个任务的模板

├── type: QuestType

├── targetId: ResourceLocation   (如 minecraft:stone)

├── targetCount: int             (目标数量)

└── rewardEmeralds: int          (奖励绿宝石数量)


QuestSlotData (不可变记录) → 一个槽位的状态

├── quest: Optional<QuestDefinition>

├── progress: int                (当前进度)

└── locked: boolean              (是否锁定)


QuestData (可变容器) → 一个玩家的全部任务

├── slots: List<QuestSlotData>   (固定3个)

└── lastRefreshTick: long        (上次刷新时间戳)
注意：QuestData 的每个修改方法（setQuest、setProgress、setSlotLocked、clearSlot）都不是直接修改字段，而是创建新的 QuestSlotData 记录替换旧值。这保证了数据一致性和序列化安全。

三、完整数据流
code
36 lines
Copy
┌────────────────────────── 服务端 ──────────────────────────┐

│                                                            │

│  QuestEventHandlers          QuestServerEvents             │

│  (方块破坏/击杀/合成/拾取)     (每tick检查刷新计时)          │

│         │                         │                        │

│         └─────────┬───────────────┘                        │

│                   ▼                                        │

│            QuestManager                                    │

│            (进度更新/任务生成/锁定/完成)                      │

│                   │                                        │

│         ┌─────────┼─────────┐                              │

│         ▼                   ▼                              │

│  player.getData()    player.setData()                      │

│  (从Attachment读取)   (写回Attachment持久化)                 │

│                   │                                        │

│                   ▼                                        │

│         syncToPlayer()                                     │

│         → S2CQuestSyncPayload                              │

│         → PacketDistributor.sendToPlayer()                  │

│                                                            │
Show 16 more lines
四、任务生命周期
阶段1：生成
两种触发方式：

玩家首次输入 /quest

QuestCommand.openQuestScreen() → QuestManager.ensureQuestsInitialized()
检查 data.hasAnyQuest() 是否为 false → 填充3个槽位
定时刷新（服务端每tick检查）

QuestServerEvents.onServerTick() → QuestManager.onServerTick()
计算：gameTime / (20 * 60 * intervalMinutes) 是否变化
变化时刷新所有在线玩家的未锁定槽位
生成算法 generateRandomQuest()：

统计玩家已有槽位的 targetId，建立排除集合
从27条任务池中过滤掉已有的（防重复）
按权重加权随机选择
在配置范围内随机生成目标数量和奖励
任务池（27条，总权重334）
类型	目标	权重
破坏	石头(20)、橡木(15)、煤矿(10)、铁矿(8)、泥土(20)、深板岩(18)、沙子(15)	106
击杀	僵尸(20)、骷髅(20)、蜘蛛(15)、苦力怕(15)、末影人(8)	78
合成	工作台(10)、熔炉(10)、铁镐(12)、铁剑(10)、火把(15)、面包(12)、木棍(8)	77
收集	煤炭(15)、铁锭(10)、小麦(12)、苹果(8)、腐肉(10)、骨头(10)	73
阶段2：进度追踪
通过 QuestEventHandlers 监听的4个事件：

事件	条件	任务类型匹配
BlockEvent.BreakEvent	仅生存模式	方块ID == 任务的 targetId
LivingDeathEvent	击杀者是玩家	实体类型ID == 任务的 targetId
PlayerEvent.ItemCraftedEvent	合成出物品	物品ID == 任务的 targetId
ItemEntityPickupEvent.Pre	拾取掉落物	物品ID == 任务的 targetId
进度更新逻辑：

code
6 lines
Copy
updateProgress(player, type, targetId, amount):

遍历3个槽位:

    如果 quest.type == type 且 quest.targetId == targetId:

      progress = min(progress + amount, targetCount)

      如果 progress >= targetCount → completeQuest()

如果有变化 → 写回Attachment → syncToPlayer()
阶段3：完成
code
6 lines
Copy
completeQuest(player, data, slot):

创建奖励 ItemStack(绿宝石, rewardEmeralds个)

尝试放入玩家背包

    放不下 → 掉落在玩家脚下

清除槽位 (变为空)

发送完成消息："完成任务: 石头! 获得 5 绿宝石。"
注意：完成任务后槽位变空，锁定状态也随之清除。

阶段4：刷新
code
5 lines
Copy
配置间隔(默认60分钟) → 服务器tick检测

→ 遍历所有在线玩家

    → 遍历3个槽位

      → 如果 locked == true → 保留原任务不变

      → 如果 locked == false → 生成新任务，进度归零
五、锁定机制
加锁流程
code
11 lines
Copy
玩家在UI点击"锁定"或执行 /quest lock 1

→ 客户端发送 C2SQuestLockPayload(0)

→ 服务端 QuestManager.handleLockPacket()

→ 检查4个前置条件:

     1. 已锁定? → 拒绝消息

     2. 无任务? → 拒绝消息

     3. 已完成? → 拒绝消息

     4. 绿宝石不够? → "你需要5个绿宝石来锁定任务!"

→ 消耗5个绿宝石 (从背包逐个扣除)

→ 设置 locked = true

→ 写回Attachment + 同步客户端
解锁流程
code
5 lines
Copy
/quest unlock 1 或再次点击锁定按钮

→ 发送 C2SQuestLockPayload(0)

→ 服务端检查已锁定状态

→ 设置 locked = false

→ 写回 + 同步 (免费，不退还绿宝石)
六、网络协议
包名	方向	频道	携带数据
S2CQuestSyncPayload	服务端→客户端	improved_original:quest_sync	完整 QuestData (3槽位+刷新时间)
C2SQuestLockPayload	客户端→服务端	improved_original:quest_lock	单个 int 槽位索引 (0-2)
特点：

采用全量同步而非增量 — 每次进度变化都发送完整数据
optional() 注册 — 对方没有该包时不影响连接
客户端收包在网络线程，通过 Minecraft.getInstance().execute() 转到渲染线程创建界面
七、界面逻辑（QuestScreen）
布局（每个槽位 60px 高度）
code
13 lines
Copy
┌──────────────────────────────────────────┐

│              每日任务                      │  标题 (y=15)

│                                          │

│  [破坏 石头 x32]                    [锁定] │  描述 (y=slot*60+2)

│  [████████░░░░░░] 12/32         (5 绿宝石)│  进度条 (y+16)

│  奖励: 5 绿宝石                           │  奖励 (y+34)

│                                          │

│  [击杀 僵尸 x10]                    [已锁定]│

│  [██████████░░] 10/10                   │

│  奖励: 3 绿宝石 (绿色表示完成)             │

│                                          │

│                    [关闭]                  │

└──────────────────────────────────────────┘
按钮状态
槽位状态	按钮文字	可点击
无任务	"锁定"	❌ 禁用
进行中，未锁定	"锁定"	✅
进行中，已锁定	"已锁定"	✅ (切换为解锁)
已完成	"锁定"	❌ 禁用
颜色方案
进度条未完成填充：蓝色 #4488FF
进度条已完成填充：绿色 #00AA00
进度条背景：深灰 #444444
奖励文字进行中：橙色 #FFAA00
奖励文字已完成：绿色 #55FF55
锁定费用提示：浅灰 #AAAAAA
空槽位文字：灰色 #888888
八、配置项
在 config/improved_original-common.toml 中：

配置键	默认值	范围	作用
questRefreshIntervalMinutes	60	1-1440	任务刷新间隔（分钟）
emeraldLockCost	5	1-64	锁定消耗绿宝石数量
questTargetCountMin	5	1-1024	任务目标数量下限
questTargetCountMax	32	1-1024	任务目标数量上限
questRewardMin	1	1-64	任务奖励绿宝石下限
questRewardMax	10	1-64	任务奖励绿宝石上限
九、事件总线注册总结
事件	订阅者	事件总线
FMLCommonSetupEvent	ImprovedOriginal	Mod总线 (构造中)
RegisterPayloadHandlersEvent	ModPayloadHandlers	Mod总线 (构造中)
ServerStartingEvent	ImprovedOriginal	NeoForge总线 (构造中)
ServerTickEvent.Post	QuestServerEvents	NeoForge总线 (@EventBusSubscriber)
BlockEvent.BreakEvent	QuestEventHandlers	NeoForge总线 (@EventBusSubscriber)
LivingDeathEvent	QuestEventHandlers	NeoForge总线 (@EventBusSubscriber)
PlayerEvent.ItemCraftedEvent	QuestEventHandlers	NeoForge总线 (@EventBusSubscriber)
ItemEntityPickupEvent.Pre	QuestEventHandlers	NeoForge总线 (@EventBusSubscriber)
RegisterCommandsEvent	QuestCommand	NeoForge总线 (@EventBusSubscriber)
@EventBusSubscriber(modid = "improved_original") 的类会被 NeoForge 自动发现并注册到 NeoForge.EVENT_BUS，无需在构造函数中显式调用。