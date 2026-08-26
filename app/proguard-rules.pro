# 噗噗日记 R8 规则
# Compose / AndroidX / kotlinx-coroutines 自带 consumer 规则，通常无需额外 keep。
# 组件（Activity / Receiver）由 Manifest 自动保留。

# 保留备份 JSON 解析使用的 org.json 由平台提供，无需处理。
