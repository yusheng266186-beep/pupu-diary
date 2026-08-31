# ARCHITECTURE · 架构文档

> 本文档描述本仓库（噗噗日记 × 便便晴天 双分支 monorepo）的整体结构与两款 App 各自的技术架构。
> 噗噗日记的产品与设计规格另见 [`DESIGN.md`](DESIGN.md)。

---

## 1. 仓库结构

```
pupu-diary（GitHub 单仓库）
├── 分支 main        → 噗噗日记 Pupu Diary     根目录级 Android 工程，标签 v*
├── 分支 shunshun    → 便便晴天                根目录级 Android 工程，标签 shunshun-v*
└── 文档（main 分支）：README.md（合集总览）、ARCHITECTURE.md（本文）、DESIGN.md（噗噗日记设计）
```

- 两分支为**不相关历史**，由 main 上一个 `-s ours` 策略的 merge 提交联结：main 的文件树在合并中保持不变，便便晴天的全部历史经第二父提交可达。
- 每个 App 独立发版：标签命名空间 `v*` / `shunshun-v*`，Release 共用同一 Releases 页面，资产文件名以 `pupu-diary-` / `shunshun-` 前缀区分。
- CI 归属各分支：`main` 上的 `.github/workflows/build-apk.yml`（ubuntu，单测 + Debug 构建）；`shunshun` 分支自带 workflow（windows，Debug 构建），其触发分支名沿袭原仓库配置。

---

## 2. 噗噗日记（main 分支）

**一句话架构**：零第三方依赖的极简单体 —— Compose UI + 裸 SQLite + 应用级状态单例，自研导航。

### 2.1 模块地图（24 文件 / ~6000 行 Kotlin）

```
app/src/main/java/com/pupu/diary/
├── PupuApp.kt             # Application：进程级持有 AppState 单例（v4.0.0）
├── data/
│   ├── PoopRecord.kt      # 记录模型（含颜色/腹痛/费力/未排净）+ Bristol 分型 + PoopColors 八色 + extraInfo 摘要
│   ├── RecordDb.kt        # SQLiteOpenHelper，DB v5（records + checkins），v1→v5 增量迁移
│   └── Prefs.kt           # SharedPreferences 封装（主题/夜间/隐私/双提醒/打卡/昵称/userId/自动备份）
├── ui/
│   ├── AppState.kt        # 应用级状态单例：内存记录列表 + 全部偏好 + 写操作（DB 在 IO、列表变更回主线程）
│   ├── Root.kt            # AppRoot（夜间解析/色板过渡/隐私即时/状态栏同步/减动效）+ MainScaffold（Tab/贴士/时间线/编辑四层）
│   ├── screens/           # Home / Record / Stats / Tips / Profile / AllRecords（全部记录时间线，v4.0.0）
│   ├── components/        # 便便角色（呼吸/眨眼/可关眨眼）、Bristol 图标、心情脸、图表、糖果按钮、撒花、PupuStepper/DateTimeDialog
│   └── theme/             # 5×明暗色板 → MaterialTheme 映射（450ms 全色过渡）+ LocalReduceMotion
└── util/
    ├── Backup.kt          # JSON 备份（解析容错）+ 自动备份与 isBackupDue 纯函数 + 备份文件列举
    ├── Csv.kt             # 医生查看版 CSV（十列 + #元信息行，UTF-8 BOM）
    ├── Insights.kt        # 纯逻辑统计层（buildBars/buildInsights/streakDays，含打卡关联洞察，有单测）
    ├── Reminders.kt       # AlarmManager 每日提醒（触发后自续期）
    ├── BootReceiver.kt    # 开机 / 升级后重挂闹钟
    ├── CrashLog.kt        # 环形崩溃日志 + 应用内 tail 查看
    └── Dates.kt           # 日期工具（纯 JVM，有单测）
```

### 2.2 数据流

```
SQLite(pupu.db) ──启动全量加载──▶ AppState.records(mutableStateListOf)
                                      │
UI 层直接读取重组 ◀──────────────────┘
写入：UI → AppState.addRecord/update → CoroutineScope(IO) → SQLite → 同步刷新内存列表
```

### 2.3 关键决策与权衡

| 决策 | 收益 | 代价 / 天花板 |
|---|---|---|
| 零第三方依赖（仅 Compose BOM + 基础 androidx） | 构建快、包体小、升级风险低 | 没有响应式数据层，统计在内存计算 |
| 启动全量加载记录进内存 | 实现简单，任意统计一次算完 | 数据量增长后启动变慢（个人量级数千条内无感） |
| 自研导航 + AppState 单例 | 页面动效完全可控、无 ViewModel 样板 | 无生命周期感知，依赖进程内存活 |
| 裸 SQLiteOpenHelper + 手写迁移 | 迁移逻辑直观可审计（v1→v5） | 无编译期 SQL 校验 |

---

## 3. 便便晴天（shunshun 分支）

**一句话架构**：标准 Android 范式 —— Room + Flow 响应式数据层，ViewModel + Navigation Compose，DataStore 偏好。

### 3.1 模块地图（16 文件 / ~2200 行 Kotlin）

```
app/src/main/java/com/shunshun/app/
├── data/
│   ├── PoopRecord.kt        # Room Entity：stoolType / colorType? / mood / smoothness /
│   │                        #   durationMinutes? / abdominalPain / straining / incompleteFeeling / note
│   ├── PoopDao.kt           # Flow 响应式查询（全量 + 时间区间）
│   ├── AppDatabase.kt       # RoomDatabase v2，Migration 1→2（新增 colorType）
│   ├── PoopRepository.kt    # 数据仓库分层
│   ├── AppPreferences.kt    # DataStore Preferences
│   └── ReminderScheduler.kt # 提醒权限/调度/重启恢复
├── navigation/AppNavigation.kt   # Navigation Compose 路由
└── ui/
    ├── Theme.kt             # 鼠尾草系单主题
    ├── home/ record/ statistics/ profile/   # 273 / 436 / 310 / 416（行）
    └── components/          # HandDrawnAssets（手绘 Canvas 资产）、AppComponents、Haptics
```

### 3.2 数据流

```
Room(shunshun.db) ──Flow<List<PoopRecord>>──▶ ViewModel / collectAsState
     ▲                                          │
     └── suspend insert/update/delete ◀─────────┘
统计按日期区间下推到 SQL（observeBetween），UI 只接收已筛选数据
```

### 3.3 关键决策与权衡

| 决策 | 收益 | 代价 |
|---|---|---|
| Room + Flow | 列表自动响应、区间查询下推 SQL、编译期校验 | 引入注解处理器与库体积 |
| KSP（v1.11.0 起，替代 kapt） | 构建提速，跟进官方推荐 | 需与 Kotlin 版本严格配对（2.0.21-1.0.28） |
| ViewModel + Navigation Compose | 生命周期安全、路由标准化 | 样板代码多于自研方案 |
| DataStore | 协程友好的偏好存储 | — |
| 记录维度更细（腹痛/费力/未排净/颜色） | 健康信息价值更高 | 单条记录 UI 更复杂 |

---

## 4. 两套架构对照

| 维度 | 噗噗日记 | 便便晴天 |
|---|---|---|
| 数据存储 | SQLiteOpenHelper（手写 SQL + 迁移） | Room 2.6.1（注解 + 自动迁移声明） |
| 响应式 | 无（内存列表 + 手动刷新） | Flow 全链路 |
| 偏好存储 | SharedPreferences | DataStore Preferences |
| 状态管理 | AppState 单例（Compose state） | ViewModel + Compose state |
| 导航 | 自研（sealed page + BackHandler） | Navigation Compose |
| 依赖注入 | 无（手动单例） | 无（手动单例） |
| UI 主题 | 五套可切换色板 | 单套鼠尾草色板 |
| 测试 | JUnit 26 用例（Dates / Bristol / PoopColors / BackupDue / Csv / Insights） | 无 |
| 包体（release，R8） | ~1.2MB | ~1.36MB |
| 构建链 | Gradle 8.9 + AGP 8.4.2 | Gradle 8.9 + AGP 8.7.3 |

**结论**：两者是同一产品方向的两种工程答案 —— 噗噗日记证明了"极简依赖能走多远"，便便晴天证明了"标准范式长什么样"。 Neither 是银弹，见下方演进路线。

## 5. 构建与发布工程（两分支通用约定）

- **签名**：密钥库（`*.jks`）与密码一律不入库；由 gitignore 的根目录 `local.properties` 提供（噗噗日记 `pupu.storeFile/storePassword/keyAlias/keyPassword`，便便晴天 `shunshun.*` 同构）。文件缺失时 release 构建产出未签名包，debug 不受影响。
- **优化**：两分支 release 均开启 R8 混淆 + 资源收缩（噗噗日记 8.9MB→1.2MB 的关键）。
- **CI**：GitHub Actions 仅构建 Debug 包并上传 artifact（CI 环境无签名密钥，属有意设计）；签名 Release 一律本地构建后经 `gh release create` 发布。
- **版本号**：`versionCode` 单调递增（两项目各自独立计数），`versionName` 语义化。
- **迁移安全**：两 App 的数据库升级均走增量迁移（噗噗日记手写 `onUpgrade`，便便晴天 Room `Migration`），覆盖安装保留历史数据。

## 6. 演进路线（建议）

1. ~~**字段反哺**~~（✅ 已完成，v2.2.0）：腹痛 / 费力 / 未排净 / 颜色日记已并入噗噗日记 DB v4（手写 ALTER TABLE 迁移 + 备份格式 v4 双向兼容），新字段收进记录页折叠区以保持快流程，统计环比与腹痛预警洞察同步上线。
2. **数据层演进评估**：噗噗日记若数据量突破万条，启动全量加载将成为可感知瓶颈，可参考 `shunshun` 分支改造为 Room + 区间查询。（v4.0.0 已将打卡数据入 DB v5 并把统计/洞察抽为纯函数层，主体记录仍走全量内存——个人量级无压力）
3. **测试补齐**：便便晴天补数据层与备份格式测试；噗噗日记侧 v4.0.0 已把备份到期判断、CSV、洞察构建抽为纯函数并新增测试（12→26 用例）。
4. **shunshun 分支 CI 触发名**：其 workflow 沿袭原仓库的 `master` 分支名配置，在本仓库的 `shunshun` 分支上不会自动触发，如需启用可将触发分支改为 `shunshun`（属对该分支的改动，暂按"迁移不动分文"原则保留原样）。
5. **平台升级**：本机 Android SDK 仅有 android-34 平台，AGP/targetSdk 升级（35+）需在有网络/平台包的环境执行，暂未纳入 v4.0.0。
