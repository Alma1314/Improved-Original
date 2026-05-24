# Improved Original — 原版增强Mod

基于 NeoForge 1.21.1 的 Minecraft 模组，添加宝石系统与每日任务系统。

---

## 模组内容

### 宝石系统
- **5种宝石**：红宝石(Ruby)、蓝宝石(Sapphire)、黄宝石(Topaz)、紫宝石(Amethyst)、黑宝石(Onyx)
- **15种方块**：宝石块、矿石、深层矿石各5种
- 矿石可用镐开采，受时运/精准采集影响
- 烧炼和高炉配方产出宝石
- 3x3合成宝石块，宝石块可分解为9个宝石

### 每日任务系统
- 每位玩家独立3个随机任务，每小时自动刷新
- **5种任务类型**：破坏方块、合成物品、击杀实体、收集物品、探索结构
- 任务池由JSON驱动（`./config/improved_original/quests/`），自定义任务与奖励
- 奖励支持任意物品（ID+数量），不再限制绿宝石
- 进度自动追踪，完成后奖励自动入包并通过Toast通知
- 可消耗绿宝石锁定槽位（防刷新），也可免费解锁
- 手动刷新按钮（消耗10绿宝石）
- 面板显示刷新倒计时
- 首次打开面板前任务不激活（不追踪进度）
- 自然刷新时聊天栏提示
- **JEI 集成**：在 JEI 中点击书图标查看所有任务（三行两列提交物品 → 实心箭头 → 三行两列奖励物品，不足时自动居中）

---

## 使用方法

### 按键
- **O键**（默认）— 打开任务面板，可在游戏设置「控制」中修改

### 命令
```
/quest                  — 打开任务面板
/quest lock <1-3>       — 锁定指定槽位（消耗绿宝石）
/quest unlock <1-3>     — 解锁指定槽位（免费）
```

### 任务面板
- 标题 + 刷新倒计时
- 3个槽位：任务名称、任务描述、进度条、奖励、锁定按钮
- 鼠标悬停在任务名称上可查看简介
- 手动刷新按钮（消耗绿宝石）
- 关闭按钮
- 任务完成时右上角弹出Toast通知

### 配置文件

**模组配置**（`config/improved_original-common.toml`）：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `questRefreshIntervalMinutes` | 60 | 任务刷新间隔（分钟） |
| `emeraldLockCost` | 5 | 锁定槽位消耗绿宝石 |
| `emeraldRefreshCost` | 10 | 手动刷新消耗绿宝石 |

**任务池配置**（`config/improved_original/quests/default_pool.json`）：

```json
{
  "entries": [
    {
      "type": "BREAK_BLOCK",
      "targets": [
        {"item": "minecraft:stone", "countMin": 10, "countMax": 64}
      ],
      "rewards": [
        {"item": "minecraft:emerald", "countMin": 1, "countMax": 10}
      ],
      "weight": 20,
      "name": "quest.improved_original.name.minecraft.stone",
      "description": "quest.improved_original.desc_text.minecraft.stone"
    },
    {
      "type": "BREAK_BLOCK",
      "targets": [
        {"item": "minecraft:stone", "countMin": 10, "countMax": 64},
        {"item": "minecraft:oak_log", "countMin": 5, "countMax": 20}
      ],
      "rewards": [
        {"item": "minecraft:emerald", "countMin": 1, "countMax": 10},
        {"item": "minecraft:diamond", "countMin": 1, "countMax": 3}
      ],
      "weight": 15,
      "name": "quest.improved_original.name.minecraft.stone",
      "description": "quest.improved_original.desc_text.minecraft.stone"
    }
  ]
}
```

- `targets` / `rewards` 数组：每个元素含 `item`（ResourceLocation）、`countMin`/`countMax`（数量范围）
- `weight`：权重越大被选中的概率越高
- `name` / `description`：任务名称和简介的翻译键，鼠标悬停显示简介
- 翻译键命名规则：`quest.improved_original.name.<ns>.<path>` 和 `quest.improved_original.desc_text.<ns>.<path>`
- 首次启动自动生成默认配置，修改后重启生效
- 支持多个 JSON 文件，所有 entries 自动合并
- JEI 中最多显示 6 个提交物品和 6 个奖励物品（三行两列）

---

## 项目结构

```
src/main/java/com/alma/improved_original/
├── ImprovedOriginal.java              # 主模组类
├── ImprovedOriginalClient.java        # 客户端类
├── Config.java                        # 配置文件
├── ModDataGenerator.java              # 数据生成入口
├── block/ModBlocks.java               # 方块注册（15种）
├── item/
│   ├── ModItems.java                  # 物品注册（5种宝石）
│   └── ModCreativeModeTabs.java       # 创造模式标签页
├── quest/
│   ├── QuestType.java                 # 任务类型枚举（5种）
│   ├── QuestDefinition.java           # 任务定义（多目标/多奖励）
│   ├── QuestSlotData.java             # 单槽位状态（每目标独立进度）
│   ├── QuestData.java                 # 玩家完整任务数据
│   ├── ModAttachments.java            # AttachmentType注册
│   ├── QuestManager.java              # 核心逻辑
│   ├── command/QuestCommand.java      # /quest命令
│   ├── network/
│   │   ├── S2CQuestSyncPayload.java   # 服务端→客户端同步
│   │   ├── C2SQuestLockPayload.java   # 客户端→服务端锁定
│   │   ├── C2SQuestRefreshPayload.java # 客户端→服务端刷新
│   │   ├── C2SQuestOpenPayload.java   # 客户端→服务端打开面板
│   │   └── ModPayloadHandlers.java    # 网络包注册
│   ├── event/
│   │   ├── QuestEventHandlers.java    # 游戏事件+结构检测
│   │   └── QuestServerEvents.java     # Tick刷新计时器
│   ├── screen/QuestScreen.java        # 任务面板UI
│   └── client/
│       ├── ClientQuestCache.java      # 客户端数据缓存
│       ├── QuestClientEvents.java     # 客户端网络处理
│       ├── QuestKeyMapping.java       # 按键绑定（O键）
│       └── QuestToast.java            # 完成通知Toast
├── jei/
│   ├── QuestJeiPlugin.java            # JEI插件注册
│   ├── QuestRecipe.java               # JEI配方记录
│   └── QuestRecipeCategory.java       # JEI配方类别渲染
└── datagen/                           # 数据生成
    ├── ModBlockLootTablesProvider.java # 战利品表
    ├── ModBlockStatesProvider.java     # 方块状态/模型
    ├── ModItemModelsProvider.java      # 物品模型
    ├── ModItemTagsProvider.java        # 物品标签
    ├── ModBlockTagsProvider.java       # 方块标签
    ├── ModRecipesProvider.java         # 合成配方
    ├── ModQuestPoolProvider.java       # 任务池JSON
    ├── QuestPoolConfig.java            # 任务池加载/默认生成
    ├── ModEnUsLangProvider.java        # 英文翻译
    └── ModZhCnLangProvider.java        # 中文翻译
```

---

## 构建

```bash
# 编译
./gradlew build

# 运行数据生成
./gradlew runData

# 运行客户端测试
./gradlew runClient

# 运行服务端测试
./gradlew runServer
```

---

## 许可证

MIT
