# 便便晴天 release 混淆补充规则
#
# 说明：项目使用的 Room / Compose / DataStore 等 AndroidX 库均自带 consumer
# ProGuard 规则（随 AAR 分发），通常无需在此额外配置。此文件保留用于放置
# 针对 app 自身代码的必要 keep 规则；如 R8 构建报缺类错误，可按报错信息
# 在此补充 -dontwarn / -keep 规则。
