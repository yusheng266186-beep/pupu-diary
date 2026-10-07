# 便便晴天 · 便便生活记录

一款面向中国大陆用户的本地生活记录 Android App，品牌主题是“今天也要轻松一点”，视觉方向为极简、治愈、柔和的手绘插画风。

<!-- project-navigation:start -->
## 项目概览

| 项目 | 说明 |
| --- | --- |
| 分类 | 生活记录 |
| 平台 | Android |
| 当前定位 | 独立 App · shunshun 分支 |

在手机本地记录身体状态、查看趋势并导出备份的两款 Android 应用。

[版本与下载](https://github.com/yusheng266186-beep/pupu-diary/releases) · [使用与开发](#构建) · [项目总导航](https://github.com/yusheng266186-beep/yusheng266186-beep)

本分支承载便便晴天；[默认分支 main](https://github.com/yusheng266186-beep/pupu-diary/tree/main) 承载噗噗日记。下载本 App 时选择 `shunshun-v*` 发布标签。

**阅读导航：** [构建](#构建) · [版本历史](#版本历史)

<!-- project-navigation:end -->

## 已实现

- 首页治愈场景与轻量动画
- 便便晴天品牌角色与原创 7 种手绘便便图案
- 7 种便便类型、心情、顺畅度记录
- Room 本地数据持久化
- 历史记录时间线、节奏趋势线与类型环形图
- 本地备份导出、恢复和清除
- 历史 Debug APK 可用于对应版本的安装验证；当前签名与下载说明见「APK」
- 页面已按首页、记录、统计、我的拆分，统一使用手绘 Canvas 资产与自定义交互组件
- 记录支持时长、腹部状态、费力、未排净和备注；历史记录支持日期筛选；统计区间按真实数据聚合
- 针对实机截图优化底部线性图标、心情表情语义、首页植物层次和统计标签排版
- 历史记录支持详情、编辑和删除；提醒支持通知权限、时间设置、每日调度与重启恢复；数据层引入 Repository
- 记录支持可选颜色日记，旧版本数据通过 Room 迁移无感保留；历史详情、备份恢复会同步保留颜色信息
- 首页增加连续记录徽章与每日小贴士；“我的”支持本地昵称编辑
- 全局替换为无矩形水波纹的手绘交互；删除、清除、编辑与提醒时间统一使用圆角手绘弹窗，提醒时间使用自绘 24 小时轮盘
- 首页便便角色支持轻点回应与随机鼓励气泡；心情表情、便便颜色选项和提醒图标进一步统一细化
- 首页星光采用分散上浮的 Canvas 动画；历史日期带支持横向滑动并保留更早记录；生活趋势按顺畅度、心情和记录频率与上一周期对比
- 统一固定设置行栅格，优化页面切换与高刷新率偏好，减少多余布局动画以提升滑动跟手度
- 当前保留的构建工作流仍监听 `master`；本项目位于 `shunshun`，该分支推送不会自动触发这份旧工作流，可按实际需要从 Actions 手动运行。

## 构建

使用 Android Studio 打开项目，或在项目根目录执行：

```powershell
./gradlew.bat assembleDebug
```

生成文件：`app/build/outputs/apk/debug/app-debug.apk`

如需构建正式签名版，执行：

```powershell
./gradlew.bat assembleRelease
```

生成文件：`app/build/outputs/apk/release/app-release.apk`（R8 混淆 + 资源收缩）

release 签名密钥与密码存放在本地 `local.properties`（键名：`shunshun.storeFile`、`shunshun.storePassword`、`shunshun.keyAlias`、`shunshun.keyPassword`），不随仓库分发；缺少这些配置时仍可正常构建 Debug APK，但 `assembleRelease` 会因缺少签名配置而无法签名。

最低支持 Android 8.0（API 26）。

## APK

APK 不再提交进仓库。各版本安装包请到 [GitHub Releases](https://github.com/yusheng266186-beep/pupu-diary/releases) 页面下载；已有 Debug APK 构建产物可在 [GitHub Actions](https://github.com/yusheng266186-beep/pupu-diary/actions) 的构建产物中获取。

**签名变化**：自 v1.11.0 起提供 R8 混淆 + 资源收缩的正式签名 Release APK，使用全新正式密钥签名，与 v1.10.0 及更早版本的 Debug 签名不同，无法覆盖安装——已装旧版的用户请先卸载旧版再安装新版；如有需要保留的数据，请先在 App 内使用备份导出功能，重装后恢复。

本应用仅用于个人生活记录，不构成医疗诊断或治疗建议。

## 版本历史

| 版本 | 亮点 |
| --- | --- |
| v1.11.0 | 首个 R8 混淆 + 正式签名 Release 包；Room 注解处理迁移 KSP；仓库瘦身，APK 全部移至 Releases |
| v1.10.0 | 首页星光上浮动画；历史日期带横滑并保留更早记录；生活趋势环比上一周期 |
| v1.9.0 | 手绘交互统一：无矩形水波纹、圆角手绘弹窗、自绘 24 小时提醒轮盘 |
| v1.8.0 | 连续记录徽章与每日小贴士；昵称编辑；颜色日记经 Room 迁移无感保留 |
| v1.7.0 | 记录详情、编辑、删除；通知提醒（权限、每日调度、重启恢复）；引入 Repository |
| v1.6.0 | 实机截图保真调优：底部线性图标、心情表情语义、图标缩放 |
| v1.5.0 | 记录新增时长、腹部状态等维度；历史日期筛选；统计按真实数据聚合 |
| v1.4.0 | 页面按首页 / 记录 / 统计 / 我的拆分，统一手绘 Canvas 资产与交互组件 |
| v1.3.0 | 原创手绘便便图标体系与类型化视觉样式 |
| v1.2.0 | 细化全新手绘治愈风视觉语言 |
| v1.1.0 | 整体重构为手绘治愈风体验 |
| v1.0.0 | 首个版本：便便记录、Room 持久化、历史时间线与类型环形图 |
