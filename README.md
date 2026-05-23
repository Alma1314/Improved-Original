# Improved Original — 原版增强Mod

基于 NeoForge 1.21.1 的 Minecraft 模组，添加宝石系统与每日任务系统。

---

## 模组内容

### 宝石系统
- **5种宝石**：红宝石(Ruby)、蓝宝石(Sapphire)、黄玉(Topaz)、紫水晶(Amethyst)、玛瑙(Onyx)
- **15种方块**：宝石块、矿石、深层矿石各5种
- 矿石可用镐开采，受时运/精准采集影响
- 烧炼和高炉配方产出宝石
- 3x3合成宝石块，宝石块可分解为9个宝石

### 每日任务系统
- 每位玩家独立3个随机任务，每小时自动刷新
- 4种任务类型：破坏方块、合成物品、击杀实体、收集物品
- 任务池27条，加权随机，同一玩家不会出现重复目标
- 进度自动追踪，完成后绿宝石奖励自动入包并通过Toast通知
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

### 配置
在 `config/improved_original-common.toml` 中：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `questRefreshIntervalMinutes` | 60 | 任务刷新间隔（分钟） |
| `emeraldLockCost` | 5 | 锁定槽位消耗绿宝石 |
| `emeraldRefreshCost` | 10 | 手动刷新消耗绿宝石 |
| `questTargetCountMin` | 5 | 任务目标数量下限 |
| `questTargetCountMax` | 32 | 任务目标数量上限 |
| `questRewardMin` | 1 | 任务奖励下限 |
| `questRewardMax` | 10 | 任务奖励上限 |

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
│   ├── QuestType.java                 # 任务类型枚举
│   ├── QuestDefinition.java           # 任务定义（不可变记录）
│   ├── QuestSlotData.java             # 单槽位状态（不可变记录）
│   ├── QuestData.java                 # 玩家完整任务数据
│   ├── ModAttachments.java            # AttachmentType注册
│   ├── QuestManager.java              # 核心逻辑
│   ├── command/
│   │   └── QuestCommand.java          # /quest命令
│   ├── network/
│   │   ├── S2CQuestSyncPayload.java   # 服务端→客户端同步
│   │   ├── C2SQuestLockPayload.java   # 客户端→服务端锁定
│   │   ├── C2SQuestRefreshPayload.java # 客户端→服务端刷新
│   │   └── ModPayloadHandlers.java    # 网络包注册
│   ├── event/
│   │   ├── QuestEventHandlers.java    # 游戏事件监听
│   │   └── QuestServerEvents.java     # Tick刷新计时器
│   ├── screen/
│   │   └── QuestScreen.java           # 任务面板UI
│   └── client/
│       ├── ClientQuestCache.java      # 客户端数据缓存
│       ├── QuestClientEvents.java     # 客户端网络处理
│       └── QuestToast.java            # 完成通知Toast
└── datagen/                           # 数据生成（8个Provider）
```

---

## 数据流

```
服务端:
  QuestEventHandlers (方块/击杀/合成/拾取)
       │
       ▼
  QuestManager (进度/生成/锁定/刷新)
       │
  player.getData()/setData() ← Attachment持久化
       │
  S2CQuestSyncPayload → 网络 → 客户端
       │
客户端:
  QuestClientEvents → ClientQuestCache → QuestScreen/QuestToast
```

---

## 构建

```bash
# 编译
./gradlew build

# 运行数据生成（生成JSON资源文件）
./gradlew runData

# 运行客户端测试
./gradlew runClient

# 运行服务端测试
./gradlew runServer
```

---

## 许可证

MIT
