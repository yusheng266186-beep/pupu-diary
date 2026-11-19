# Pupu Diary and Shunshun · Local Android Journals

[中文说明](README.md) · [APK releases](https://github.com/yusheng266186-beep/pupu-diary/releases) · [Architecture](ARCHITECTURE.md)

One repository hosts two distinct Android apps for locally recording bowel health, seeing trends, and backing up records. Each branch contains a complete app with its own release tags.

| App | Branch | Release tags | Focus |
| --- | --- | --- | --- |
| Pupu Diary | [main](https://github.com/yusheng266186-beep/pupu-diary/tree/main) | <code>v*</code> | Hand-drawn Compose illustrations, a simple logging flow, themes, trends, and JSON backups. |
| Shunshun | [shunshun](https://github.com/yusheng266186-beep/pupu-diary/tree/shunshun) | <code>shunshun-v*</code> | Independently maintained logging and trends with its own design and release history. |

Check the appropriate tag prefix when downloading an APK. Records stay on the device and the apps work offline. Treat JSON exports as personal records. Use the matching app's backup and restore workflow. The journals are for personal tracking and communication with a qualified professional when appropriate.

## Build

Both projects use JDK 17 and Gradle 8.9. Open the selected branch in Android Studio. On Windows run <code>gradlew.bat assembleDebug</code>. On Linux or macOS, <code>main</code> includes <code>./gradlew assembleDebug</code>. For <code>shunshun</code>, use Android Studio or a matching local Gradle 8.9 installation. The branch build and APK signing histories are independent.

Original app source and documentation on both branches use the [MIT License](LICENSE). Gradle and third-party dependencies retain their own licenses.
