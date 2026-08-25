# 顺顺 · 便便生活记录

一款面向中国大陆用户的本地生活记录 Android App，视觉方向为极简、治愈、柔和的插画风。

## 已实现

- 首页治愈场景与轻量动画
- 7 种便便类型、心情、顺畅度记录
- Room 本地数据持久化
- 历史数量与统计概览
- 本地备份导出、恢复和清除
- Debug APK 可直接安装验证

## 构建

使用 Android Studio 打开项目，或在项目根目录执行：

```powershell
./gradlew.bat assembleDebug
```

生成文件：`app/build/outputs/apk/debug/app-debug.apk`

最低支持 Android 8.0（API 26）。

## APK

仓库根目录的 `顺顺-v1.0-debug.apk` 是当前可直接安装的 Debug APK。

本应用仅用于个人生活记录，不构成医疗诊断或治疗建议。
