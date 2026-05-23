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
- 任务池由JSON驱动（`config/improved_original/quests/`），自定义任务与奖励
- 奖励支持任意物品（ID+数量），不再限制绿宝石
- 进度自动追踪，完成后奖励自动入包并通过Toast通知
- 可消耗绿宝石锁定槽位（防刷新），也可免费解锁
- 手动刷新按钮（消耗10绿宝石）
- 面板显示刷新倒计时
- 首次打开面板前任务不激活（不追踪进度）
- 自然刷新时聊天栏提示

---

## 使用方法

### 命令
```
/quest                  — 打开任务面板
/quest lock <1-3>       — 锁定指定槽位（消耗绿宝石）
/quest unlock <1-3>     — 解锁指定槽位（免费）
```

### 任务面板
- 标题 + 刷新倒计时
- 3个槽位：任务描述、进度条、奖励、锁定按钮
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
      "target": "minecraft:stone",
      "countMin": 10, "countMax": 64,
      "reward": {"item": "minecraft:emerald", "countMin": 1, "countMax": 10},
      "weight": 20
    },
    {
      "type": "FIND_STRUCTURE",
      "target": "minecraft:village_plains",
      "countMin": 1, "countMax": 1,
      "reward": {"item": "minecraft:diamond", "countMin": 3, "countMax": 5},
      "weight": 10
    }
  ]
}
```

- 字段 `countMin`/`countMax` 控制目标数量，`reward.countMin`/`reward.countMax` 控制奖励数量
- 字段 `weight` 越大，被选中的概率越高
- JSON 中 `target` 和 `reward.item` 使用 ResourceLocation 格式（`minecraft:xxx`）
- 首次启动自动生成默认配置，修改后重启即可生效
- 支持多个 JSON 文件，所有文件中的 entries 会被合并

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
│   ├── QuestDefinition.java           # 任务定义（含自定义奖励）
│   ├── QuestSlotData.java             # 单槽位状态
│   ├── QuestData.java                 # 玩家完整任务数据
│   ├── ModAttachments.java            # AttachmentType注册
│   ├── QuestManager.java              # 核心逻辑
│   ├── QuestPoolConfig.java           # JSON任务池配置
│   ├── command/QuestCommand.java      # /quest命令
│   ├── network/
│   │   ├── S2CQuestSyncPayload.java   # 服务端→客户端同步
│   │   ├── C2SQuestLockPayload.java   # 客户端→服务端锁定
│   │   ├── C2SQuestRefreshPayload.java # 客户端→服务端刷新
│   │   └── ModPayloadHandlers.java    # 网络包注册
│   ├── event/
│   │   ├── QuestEventHandlers.java    # 游戏事件+结构检测
│   │   └── QuestServerEvents.java     # Tick刷新计时器
│   ├── screen/QuestScreen.java        # 任务面板UI
│   └── client/
│       ├── ClientQuestCache.java      # 客户端数据缓存
│       ├── QuestClientEvents.java     # 客户端网络处理
│       └── QuestToast.java            # 完成通知Toast
└── datagen/                           # 数据生成（8个Provider）
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
