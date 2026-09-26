# v0.1 验证状态

## 已完成

- 所有 `*.json` 与 `pack.mcmeta` 可解析。
- 11 张游戏内静态材质均检查为 16×16 PNG；模组 `logo.png` 为独立 64×64 图标。
- `creationcore:` 模型/纹理引用完整。
- 核心四个配方文件存在并通过约定内容检查。
- `#creationcore:creative_core_containers` 包含 17 种原版潜影盒。
- Java 源码已通过 `javac` 的解析级语法检查，没有发现缺括号、缺分号、非法语句等 parser-level 错误。
- 内部命名空间已统一为 `creationcore`。

## 当前环境无法完成的验证

本次生成环境中：

- Java 21 可用；
- 没有预装 Gradle；
- 没有 NeoForge/Minecraft Gradle 依赖缓存；
- 容器无法直接下载 Gradle/NeoForge 二进制依赖。

因此 **本次交付没有声称已完成真实 `gradle build`，也没有伪造 JAR**。

工程已经加入 GitHub Actions。推送到 GitHub 后，工作流会使用与 NeoForge 1.21.1 当前 ModDevGradle 模板一致的 Gradle 9.2.1，执行：

```text
python3 tools/validate_project.py
gradle build --stacktrace
```

成功后会上传 `build/libs/*.jar` 作为 Artifact。

## 第一次实机测试建议

按以下顺序测试最容易定位问题：

1. 纸 -> 空白物质。
2. 凋灵出生爆炸吞多个空白物质 -> 击杀 -> 恰好 1 基底物质。
3. 基底物质碰撞、破坏速度、爆炸/龙冲撞免疫。
4. 龙战中心纵轴放多个基底物质 -> 龙死亡 -> 全部消耗 -> 出口传送门完成后恰好 1 创造物质。
5. 合法/非法潜影盒分别扔入末地返回传送门。
6. 创造核心实体重进世界后是否保留、无法被攻击，以及进入 5 格范围是否恰好转化一次。
7. 锻造创造工作台。
8. 原版普通工作台配方、牛刷怪蛋专属配方、Shift 点击。
9. 三个维度分别测试空桶虚空打捞；特别测试返回路径被方块堵住时不会穿墙。
10. “空”桶右键水、岩浆、炼药锅，确认没有桶类交互。
11. 四玻璃 + “空”桶 -> 3 瓶装“　” + 返还 1 桶。


## Build-fix-1 (after first GitHub Actions compile)
The first real compile exposed two 1.21.1 API mismatches that static syntax parsing could not detect. They have now been corrected:

- `net.minecraft.world.inventory.Container` -> `net.minecraft.world.Container` in the creative crafting result slot.
- `CustomRecipe.Serializer` -> `SimpleCraftingRecipeSerializer` for the void bottling special recipe.

The deprecation messages for `EventBusSubscriber.Bus.MOD` are warnings only and do not fail the build.


## build-fix-2 changes checked statically

- End void threshold changed to -10 +/- 5.
- Return speed doubled to 0.15 blocks/tick.
- Void bucket remains no-gravity and pickable after surfacing.
- Void return entity update interval changed to 1 tick.
- Creative Matter spawn moved to the fight-origin centre and made no-gravity.

A real NeoForge/Gradle compile still needs to be run in GitHub Actions.

## build-fix-6 Mine Craft 静态检查

- `creationcore:mine_craft` 已注册并加入工具创造栏，但未添加生存配方。
- `creationcore.mixins.json` 包含异常硬度归一化破坏、Light Block 选中、GameMasterBlock 破坏与客户端 Barrier/Light 标记扩展。
- 已取消 `#creationcore:mine_craft_pickaxe_bonus` 硬编码列表；挖掘工艺会检测注册表中是否存在任何能加速该方块的物品，若没有则按下界合金镐效率处理。
- `#creationcore:mine_craft_drop_fallback_blacklist` 默认包含龙蛋、下界传送门、末地传送门和末地折跃门。
- 硬度不在 `[0,50]` 内的方块统一按硬度 50 计算挖掘进度。
- 掉落优先使用模拟精准采集镐的原始 loot table；无物品时才进入自身方块回退掉落。
- `#minecraft:enchantable/sword`、`sharp_weapon`、`mining`、`vanishing` 已追加 Mine Craft；没有加入 `mining_loot` 或 `durability`。
- `mine_craft.png` 已检查为 16×16。
- 仍需 GitHub Actions 的真实 NeoForge 编译与实机测试来确认 Mixin 目标和交互调用在 21.1.248 上的运行结果。

## build-fix-9 多格掉落 / 基底物质测试重点

静态检查已经确认：

- `CoreGameplayEvents` 不再包含 `BedBlock` / `DoorBlock` / `DoublePlantBlock` 之类的多格结构硬编码。
- 挖掘工艺掉落改为围绕一次 `ServerPlayerGameMode#destroyBlock` 的事务：破坏过程中清除属于该操作的 BlockDrops，破坏成功后仅在玩家主动选中的主位置生成 1 个主体方块物品。
- `destroyBlock` 使用整方法包装并在 `finally` 中结束事务，避免其他模组提前返回/取消后留下脏事务状态。
- 保留一个 2 tick 的极短延迟窗口，用于拦截部分模组稍后才拆除附属块的情况；无 breaker 的附属掉落也会按本次主体物品关系判断。
- 基底物质仍位于 `minecraft:mineable/pickaxe`，并通过 `ItemAbilities.PICKAXE_DIG` 接受原版或模组镐类工具；实际加速值来自 `player.getDestroySpeed(state)`，因此不同工具效率、效率附魔和玩家挖掘状态仍由正常 Minecraft 速度系统参与。

实机建议至少覆盖：

1. 原版床：分别挖床头/床尾，每次总计只得到 1 张床。
2. 原版门：分别挖上半/下半，每次总计只得到 1 扇门。
3. 双高植物：分别挖上半/下半，只得到 1 个对应植物自身。
4. Waystones：分别从可选中的不同组成部分挖掘，整个结构只产生 1 个主体 Waystone 物品。
5. 另选一个不同模组的 2～3 格结构，确认没有依赖 Waystones 特判。
6. 连续快速挖两个相同普通方块，确认第二次正常再掉 1 个，不会被上一事务误吞。
7. 两名玩家在同一区域同时用挖掘工艺挖方块，确认事务不会互相吞掉。
8. 虫蚀方块、末影龙蛋，确认仍维持例外行为。
9. 基底物质：空手、木/石/铁/金/钻石/下界合金镐分别测试破坏时间，并测试至少一把其他模组正常声明 `PICKAXE_DIG` 的镐。
10. 挖掘工艺应保持单一形态：无 R 键切换；攻击伤害 10、攻击速度 1.6；右键只验证斧子去皮/除锈/去蜡与铲子土径，不应再触发刷子、锄地或剪刀类交互。

注意：如果某个第三方模组完全绕过 NeoForge 的 BlockDrops 流程、在自定义逻辑里直接生成 `ItemEntity`，仅靠通用 BlockDrops 事务无法安全地区分“结构重复物品”和“该模组有意额外生成的物品”。这种极端实现需要针对该模组的实际掉落路径再决定是否增加兼容层；当前方案不通过全局拦截所有 ItemEntity 来冒险吞掉容器内容或其他合法附加掉落。
