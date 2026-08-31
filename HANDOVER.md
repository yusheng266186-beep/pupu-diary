# 噗噗日记 · 交接上手清单（HANDOVER）

> 读者：接手本项目的下一个 AI / 开发者。
> 目标：按顺序读完本文档，可以在 30 分钟内完成环境搭建、跑通构建与测试、理解项目结构，并独立完成一次完整的版本发布。
> 本文只陈述事实与操作步骤，不包含任何改进方向——发现问题请自行评估。

---

## 1. 这个仓库是什么

**单仓库（monorepo）双分支结构**，两个独立的 Android App 各占一条分支，互不影响：

| 分支 | 项目 | 当前版本 | 标签空间 | 状态 |
|---|---|---|---|---|
| `main` | **噗噗日记 Pupu Diary**（本清单的主角） | v3.0.0（versionCode 14） | `v*` | 活跃开发 |
| `shunshun` | **便便晴天** | v1.11.0（versionCode 12） | `shunshun-v*` | 独立完整项目，保留原仓库全部 16 个提交历史 |

- 两分支是**不相关历史**（由 main 上一个 `-s ours` 策略的 merge 提交联结），**不要在两分支之间互相提 PR**，会产生全量伪冲突。
- Releases 页面混放两个系列的版本：噗噗日记看 `v*`，便便晴天看 `shunshun-v*`。
- 切换项目：`git checkout main` / `git checkout shunshun`，两条分支都是根目录级完整 Android 工程。

**文档索引（按阅读顺序）：**

| 文档 | 内容 | 什么时候读 |
|---|---|---|
| `README.md`（main） | 合集总览：分支模型、两款 App 介绍、功能对比、构建方法 | 第一时间 |
| `HANDOVER.md`（本文） | 环境搭建、代码地图、发布流程、踩坑实录 | 第二步 |
| `ARCHITECTURE.md`（main） | 两套架构对照、数据流图、构建与签名约定 | 需要深入时 |
| `DESIGN.md`（main） | 噗噗日记的完整设计规格（色板令牌/动效参数/验收标准）+ **逐版本更新日志** | 改 UI 或查"某版本改了什么"时 |
| `README.md`（shunshun 分支） | 便便晴天自己的说明 | 只在需要了解便便晴天时 |

**技术栈速览（噗噗日记）：** Kotlin 2.0.21 · Jetpack Compose（BOM 2024.09）· Material3（自定义主题映射）· 裸 SQLiteOpenHelper（无 Room）· SharedPreferences · AlarmManager 提醒 · minSdk 26 / targetSdk 34 · applicationId `com.pupu.diary` · 数据库名 `pupu.db`（当前 version 4）· 偏好文件 `pupu_prefs`。除 Compose BOM 与基础 androidx 外**零第三方依赖**。

---

## 2. 本机环境（Windows 11 + Git Bash）—— 照做即可

以下路径都是这台机器的实际情况，接手后先原样使用：

| 项 | 值 | 说明 |
|---|---|---|
| JDK | `D:/Android/jbr` | **不在 PATH 上**，每个新 shell 都要先 export（见下） |
| Android SDK | `D:/Android/Sdk` | 通过项目根目录 `local.properties` 的 `sdk.dir` 生效 |
| Gradle | wrapper 8.9 | 已缓存在 `~/.gradle/wrapper/dists`，离线也能跑 |
| GitHub CLI | `gh` 已登录 `yusheng266186-beep` | 需要操作 GitHub 时用 `gh`，token 用 `$(gh auth token)` 内联，**永远不要打印或写进文件** |
| 构建工具 | `D:/Android/Sdk/build-tools/37.0.0/apksigner.bat` | 验签用（34.0.0 / 36.1.0 也在） |

**每个新 shell 的开场动作（必须）：**

```bash
export JAVA_HOME="D:/Android/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
cd "D:/ppt-master/shit app"    # 注意路径含空格，必须带引号
```

**首次跑通验证（全绿即环境 OK）：**

```bash
./gradlew testDebugUnitTest assembleDebug assembleRelease
# 期望：BUILD SUCCESSFUL，12 个单测全过
# 产物：app/build/outputs/apk/{debug,release}/
```

---

## 3. 关键配置文件（改前必读）

| 文件 | 作用 | 红线 |
|---|---|---|
| `local.properties`（不入库） | `sdk.dir` + 签名四键 `pupu.storeFile / storePassword / keyAlias / keyPassword` | **绝不能提交入库**（.gitignore 已覆盖）；密钥库文件在 `app/pupu-release.jks`（同样不入库） |
| `gradle.properties`（入库） | JVM 参数 / 并行 / AndroidX 开关 | **保持可移植**：不要写入代理、本机 JDK 路径（`org.gradle.java.home`）等机器特定值，机器级配置放全局 `~/.gradle/gradle.properties` |
| `gradle/wrapper/gradle-wrapper.properties` | 指向官方 services.gradle.org 的 gradle-8.9 | 不要改回镜像地址或降版本 |
| `app/pupu-release.jks`（不入库） | 签名密钥库（CN=Pupu Diary，2048 位 RSA） | 丢失则无法再以同签名发版；交接换机时需用户手动拷贝 `local.properties` + 此文件 |
| `.gitignore` | 已覆盖 `local.properties` / `*.jks` / `*.apk` / `dist/` / `build/` 等 | 不要把 dist/ 或 jks 加回版本控制 |

签名行为：`local.properties` 四键齐全 → release 产出签名包；缺任一键 → release 产出**未签名包**（debug 构建始终不受影响）。CI 正是利用这一点只构建 debug。

---

## 4. 代码地图（main 分支，`app/src/main/java/com/pupu/diary/`）

```
MainActivity.kt            # 入口：CrashLog.install → enableEdgeToEdge → AppState 单例 → AppRoot
                           # onResume 按隐私模式增减 FLAG_SECURE；onNewIntent 接收通知导航
├── data/
│   ├── PoopRecord.kt      # 记录模型（id/time/bristol/mood/smoothness/note/durationMin/
│   │                      #   colorType/abdominalPain/straining/incomplete）+ Bristol 分型 + PoopColors 色板
│   ├── RecordDb.kt        # SQLiteOpenHelper，DB_VERSION=4；onUpgrade 增量 ALTER TABLE；全量读 all()
│   └── Prefs.kt           # SharedPreferences 封装：主题/夜间/隐私/双提醒/打卡/昵称/userId/自动备份
├── ui/
│   ├── AppState.kt        # ★ 应用级状态单例：全量记录内存列表 + 全部偏好状态 + 所有写操作入口
│   │                      #   （addRecord/updateRecord/deleteRecord/removeWithUndo/undoLastDelete/
│   │                      #    importFromJson/setTheme/updateDarkMode/双提醒 reArmReminders…）
│   ├── Root.kt            # AppRoot：夜间模式解析 → AnimatedAppPalette(450ms 全色过渡) → IntroScreen 开屏
│   │                      #   MainScaffold：Tab 切换动画、贴士层、编辑层、BackHandler 逐级返回、UndoBar 挂载
│   ├── theme/Palette.kt   # AppPalette 13 色令牌 ×5 浅色主题 + DARK_PALETTES ×5 深色 + toColorScheme 映射
│   ├── screens/
│   │   ├── HomeScreen     # 时段问候（带昵称）、治愈场景 Canvas（太阳/月牙/云/草丘/植物）、贴士徽章
│   │   ├── RecordScreen   # 记录表单：形状/心情/顺滑度主表单 + 折叠区（颜色日记/三状态/时长/备注）
│   │   │                  #   支持编辑模式（edit 参数，从统计页最近记录或当日明细进入）
│   │   ├── StatsScreen    # 日/周/月/年四段 + 四形态范围日历 + 三指标 + 环比(DeltaLine) + 洞察 + 最近记录
│   │   ├── TipsScreen     # 打卡（水/运动/纤维）+ Bristol 速查 + 颜色速查 + 6 张知识卡
│   │   └── ProfileScreen  # 头像卡（点按设昵称）+ 六行设置 + 底部面板（提醒×2/隐私+夜间/备份+CSV/主题）
│   └── components/        # Common(SoftCard/PupuDialog/PrimaryButton/UndoBar…)、PoopCharacter(呼吸眨眼)、
│                          #   BristolIcons、MoodFace、Charts(柱状/环形)、BottomBar、Confetti
└── util/
    ├── Backup.kt          # JSON 备份 version 4（含新字段，旧格式可读），导出到下载目录 + 系统分享 + 每周静默备份
    ├── Csv.kt             # 医生查看版 CSV（十列，UTF-8 BOM，Excel 直接打开）
    ├── Reminders.kt       # AlarmManager 每日提醒（CODE_RECORD=1001/CODE_WATER=1002），触发后自续期，通知带 nav 参数
    ├── BootReceiver.kt    # BOOT_COMPLETED + MY_PACKAGE_REPLACED 时重挂两个闹钟
    ├── CrashLog.kt        # 未捕获异常追加到应用私有目录 files/logs/crash_log.txt（约 60KB 环形），再交回系统
    └── Dates.kt           # 日期工具（纯 JVM，有单测）
```

**三条核心数据流：**

1. **记录**：RecordScreen → `AppState.addRecord/updateRecord` → `scope.launch(IO)` 写 SQLite → 同步刷新内存 `records`（mutableStateListOf）→ 各页自动重组。全量内存加载，统计在内存算。
2. **统计**：`inRange = records.filter{}` + `dataStamp`（条数×31+逐条 hashCode）作为 `remember` 缓存键——**任何字段编辑都会改变哈希**，图表/洞察自动重算。
3. **提醒**：Profile 设定 → `Reminders.schedule(code, minOfDay)` 一次性闹钟 → 触发时在 `ReminderReceiver` 里**自动续期明天** → 通知带 `nav=record/home` 参数 → MainActivity `navTarget` 直达对应页。

---

## 5. 动手范例：给记录增加一个新字段要动哪些文件

这是一条已经走过两遍（v2.2.0 加颜色+三状态、v3.0.0 未加新字段但复用链路）的完整链路，按顺序改即可：

1. `data/PoopRecord.kt` — 加字段（给默认值，保持旧构造兼容）+ 若有新枚举/色板在同文件补 object
2. `data/RecordDb.kt` — `DB_VERSION` +1，`onUpgrade` 补 `if (oldVersion < N)` 增量 ALTER TABLE，`onCreate` 的建表语句**同步补列**，`all()` 读列，`toValues()` 写列
3. `ui/AppState.kt` — `addRecord` 增参并透传（`updateRecord` 走整对象 copy 无需改）
4. `ui/screens/RecordScreen.kt` — 状态变量 + 折叠区 UI + 保存时两处调用（edit.copy / addRecord）
5. `util/Backup.kt` — JSON `version` +1，`toJsonBytes` 编码，`parse` 用 `optXxx` 带默认值解码（保证旧备份可读）
6. 展示端按需：`StatsScreen`（明细/最近记录的 extraInfo）、`Csv.kt`（导出列）
7. 版本与文档：`app/build.gradle.kts` 的 versionCode/versionName、`DESIGN.md` 更新日志、`README.md` 版本历史表

> 原则：`parse`/`all`/构造处**永远给默认值**，保证老备份、老数据库、老调用方三向兼容。

---

## 6. 发布一个版本的完整流程（checklist）

```bash
# 0. 开场：export JAVA_HOME / PATH，cd 项目目录（见第 2 节）

# 1. 升版本：app/build.gradle.kts → versionCode +1，versionName 按 semver
# 2. 文档：DESIGN.md「10. 更新日志」加小节；README.md 版本历史表加行；有功能变化顺带更新功能表
# 3. 构建与测试（必须全绿再继续）
./gradlew testDebugUnitTest assembleDebug assembleRelease

# 4. 验签（确认是 CN=Pupu Diary）
"D:/Android/Sdk/build-tools/37.0.0/apksigner.bat" verify --print-certs app/build/outputs/apk/release/app-release.apk

# 5. 产物改名入库到本地 dist/（dist/ 已 gitignore，不进 git）
cp app/build/outputs/apk/release/app-release.apk dist/pupu-diary-vX.Y.Z-release.apk
cp app/build/outputs/apk/debug/app-debug.apk     dist/pupu-diary-vX.Y.Z-debug.apk

# 6. 提交并推送（本地没有 remote，用内联 token URL）
git add -A && git commit -m "feat: vX.Y.Z —— 一句话标题"
git push "https://x-access-token:$(gh auth token)@github.com/yusheng266186-beep/pupu-diary.git" main

# 7. 创建 Release（必须带 --repo，原因见踩坑 #8）
gh release create vX.Y.Z dist/pupu-diary-vX.Y.Z-release.apk dist/pupu-diary-vX.Y.Z-debug.apk \
  --repo yusheng266186-beep/pupu-diary --title "vX.Y.Z · 标题" --notes-file notes.md

# 8. 盯 CI（main 的 push 会自动触发：单测 + debug 构建 + artifact 上传）
gh run list --repo yusheng266186-beep/pupu-diary --limit 3
# 9. 确认 GitHub Releases 页 Latest 标记停在最新主线版本上（若被其他 release 抢走：
#    gh release edit vX.Y.Z --repo ... --latest）
```

---

## 7. 踩坑实录（前手真实踩过，按"现象 → 原因 → 处理"记录）

1. **`java: command not found` / `keytool: MISSING`**
   → 本机 JDK 只装在 `D:/Android/jbr`，Git Bash 的 PATH 里没有。
   → 每次 shell 先 `export JAVA_HOME="D:/Android/jbr"; export PATH="$JAVA_HOME/bin:$PATH"`。

2. **构建莫名失败 / CI 必挂，报代理或找不到 JDK**
   → 当年仓库的 `gradle.properties` 里写死了本机代理（127.0.0.1:7897）和 `org.gradle.java.home` 本机路径。
   → 仓库文件必须保持可移植，机器特定值放全局 `~/.gradle/gradle.properties`。现仓库已清理，**别再加回去**。

3. **wrapper 指向腾讯镜像的 gradle-8.7，但本地缓存是 8.9**
   → 现象是下载/校验失败。已改为官方 8.9 URL。若再遇到，先核对 `gradle-wrapper.properties` 与 `~/.gradle/wrapper/dists` 里实际缓存是否一致。

4. **Kotlin 报 `Platform declaration clash: setDarkMode(I)V`（或 setUserName）**
   → Compose 的 `var darkMode by mutableIntStateOf(...)` 属性会自动生成 JVM setter，与手写 `fun setDarkMode(...)` 签名冲突。
   → 手写函数改名（本项目惯例是 `updateDarkMode` / `updateUserName`），并同步改调用点。

5. **在文件里插入新 @Composable 函数后报 `This annotation is not repeatable` + 后一个函数丢失 @Composable**
   → 以 `private fun TipCard(...)` 为锚点插入时，原函数头上的 `@Composable` 注解留在了插入内容上方，造成重复注解 + 后函数裸奔。
   → 插入函数时锚点要**包含注解行**（`@Composable\nprivate fun ...`），或插完后 grep 检查每个函数的注解。

6. **`Unresolved reference 'choices'`**
   → 引用了不存在的属性名（实际是 `PoopColors.labels`/`PoopColors.colors`）。
   → 引用前先 grep 目标 object 的真实成员名，别凭记忆写。

7. **单测 `PoopColorsTest` 断言失败（颜色兜底）**
   → `PoopColors.color(越界)` 兜底色是便便棕 `0xFFBE8E60`，不是色板第一项。写断言前看实现。

8. **`gh release create` 报 `no git remotes found`**
   → 本地仓库**没有配置 git remote**（历史原因，推送都用内联 URL）。
   → 所有 `gh` 命令显式带 `--repo yusheng266186-beep/pupu-diary`。

9. **标签自动跟随导致的标签污染**
   → `git fetch` 便便晴天时（refspec 指定 `refs/tags/*:refs/tags/shunshun-*`），git 的 tag auto-follow 仍会把**未加前缀**的 v1.x 标签抓进来，与噗噗日记自己的 `v1.2.0~v1.11.0` 同名冲突。
   → 当时逐个 `git tag -d` 清理。教训：**永远不要 `git push --tags`**；需要推标签时用显式 refspec（`refs/tags/shunshun-*:refs/tags/shunshun-*`）。

10. **GitHub Release 资产名不支持中文**
    → 上传时服务端会剥除文件名中的非 ASCII 字符（`便便晴天-x.apk` 会变 `-x.apk`），PATCH 改名也无效，属平台行为。
    → 资产命名用 ASCII（现有惯例：`pupu-diary-vX.Y.Z-*.apk`）。

11. **单元测试里用 org.json / Android 类直接抛异常**
    → JVM 单测跑在 android.jar stub 上，框架类方法默认抛 "not mocked"。
    → 只测纯 Kotlin 逻辑。现有可测对象：`Dates`、`Bristol`、`PoopColors`；`Backup`（org.json）与任何 Context 相关逻辑**不要**写 JVM 单测。

12. **改了记录字段但图表没刷新（历史 bug，机制已知）**
    → 图表/洞察用 `dataStamp` 作 remember 缓存键。旧实现只算"条数+时间总和"，改备注/状态不刷新。
    → 现实现为逐条 `hashCode()`（data class 全字段覆盖）。新增字段若不进 data class 而是旁路状态，记得手动并入 stamp。

13. **数据库迁移只改了 onUpgrade 忘了 onCreate（或反之）**
    → 全新安装走 `onCreate`，老用户走 `onUpgrade`，两处建表/加列必须同步。DB_VERSION 与迁移分支号严格对应。

14. **误跑 `deploy_github.py` 的风险**
    → 这是早期的"一键发布"脚本：行为是 `git commit --amend` + **force push** + 按 hardcoded 列表重建全部 Release。它不适用于日常发布（日常发布走第 6 节流程），**不要随意运行**。

---

## 8. 环境与平台事实（不是问题，是事实——调试前先知道）

- **CI 无签名密钥**（设计如此）：workflow `.github/workflows/build-apk.yml`（ubuntu-latest，temurin 17，Gradle 8.9）只跑 `testDebugUnitTest` + `assembleDebug` 并上传 artifact；签名 release 一律本地构建后走 Release 页发布。手动触发用 workflow_dispatch。
- **shunshun 分支自带另一套 workflow**（windows-latest，build-apk.yml），其触发分支名沿袭原仓库配置（`master`），在该仓库的 `shunshun` 分支上不会自动触发。
- **崩溃排查入口**：`CrashLog.kt` 把未捕获异常写入应用私有目录 `files/logs/crash_log.txt`（新条目在文件头，约 60KB 环形覆盖）。设备上用 Android Studio 的 Device Explorer 或 `adb shell run-as com.pupu.diary cat files/logs/crash_log.txt` 查看（debug 包可 run-as）。
- **通知/提醒调试**：`Reminders.schedule` 是"到点一次性闹钟 + 触发后自续期明天"，`adb shell dumpsys alarm | grep pupu` 可看已注册闹钟；通知点击经 `nav` extra 直达（记录提醒→记录页，喝水提醒→首页）。
- **R8**：release 开 minify + shrinkResources；`app/build/outputs/mapping/release/mapping.txt` 只存在于本地构建机，反解混淆堆栈需要它。
- **备份格式**：JSON 顶层 `{app:"pupu-diary", version:4, records:[...]}`，`parse` 对缺字段给默认值——v1~v4 的历史备份文件都能被当前版本导入。
- **夜间模式**：`Prefs.darkMode` 0=跟随系统/1=常开/2=常关；Root 里 `isSystemInDarkTheme()` 参与解析；深浅两套色板在 `PALETTES`/`DARK_PALETTES` 同索引对应；首页场景的云/花/月亮按 `palette.isDark` 分支绘制。
- **本地工作副本**：`D:\ppt-master\shunshun-android`（便便晴天完整本地克隆）、`D:\ppt-master\shunshun-android-backup.bundle`（其 git bundle 备份）、`D:\ppt-master\shunshun-release-assets\`（其全部历史 Release APK，含 `releases.json` 元数据）。这些是仓库合并迁移时的保全产物，确认无用前不要删。
- **git 操作注意**：本地仓库无 remote，push/fetch 用内联 token URL；`main` 与 `shunshun` 两分支历史独立，rebase/cherry-pick 跨分支没有意义。

---

## 9. 交接边界与用户约定

- **数据面**：无后端、无账号、无联网权限；用户数据全部在设备本地（`pupu.db` + `pupu_prefs`），换机依赖 App 内导出/导入。
- **签名交接**：签名能力 = 本机 `local.properties`（四个 `pupu.*` 键）+ `app/pupu-release.jks`。这两样**不在仓库里**，接手开发机时需要用户手动迁移；口令不要写进任何文档、日志或对话。
- **GitHub 操作**：`gh` 已登录；涉及写操作（push/release/repo edit）直接执行即可，但**任何输出都不要包含 token**；历史上所有 push 命令都用 `sed 's/x-access-token:[^@]*/***CRED***/g'` 脱敏回显，保持这个习惯。
- **文档同步责任**：版本号、DESIGN.md 更新日志、README 版本表三处必须同步更新，这是本仓库的既定惯例。

## 10. 上手自检清单（做完即视为交接完成）

- [ ] 新 shell 里 export JAVA_HOME 后 `./gradlew testDebugUnitTest assembleDebug` 全绿
- [ ] 说出 `AppState` 的职责与三条数据流（记录/统计/提醒）
- [ ] 说出"加一个记录字段"要动的 7 个文件（第 5 节）
- [ ] 不查资料写出发布 checklist 的 9 步（第 6 节）
- [ ] 能解释为什么 CI 只出 debug 包、为什么 `gh release create` 要带 `--repo`
- [ ] 读过 DESIGN.md 的 v3.0.0 与 v2.2.0 两节更新日志
