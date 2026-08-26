package com.pupu.diary.data

import androidx.compose.ui.graphics.Color

data class PoopRecord(
    val id: Long = 0,
    val time: Long,
    val bristol: Int,   // 1..7
    val mood: Int,      // 0..4
    val smoothness: Int, // 0..100
    val note: String = "",
    val durationMin: Int = 0 // 排便时长（分钟），0 = 未记录
)

object Bristol {
    val labels = listOf("硬块状", "干硬块状", "略块状", "泥块状", "软块状", "糊泥状", "水样状")

    fun label(type: Int) = labels[type - 1]

    fun hint(type: Int) = when (type) {
        1, 2 -> "有点干哦，记得多喝水 💧"
        3, 4 -> "非常棒的状态，继续保持！"
        5 -> "还不错，注意膳食纤维摄入"
        else -> "肚子可能不太舒服，注意保暖饮食 🍲"
    }

    /** 分布分组：0 偏硬 / 1 理想 / 2 偏软 / 3 水样 */
    fun groupOf(type: Int) = when (type) {
        1, 2 -> 0
        3, 4 -> 1
        5, 6 -> 2
        else -> 3
    }

    val groupNames = listOf("1-2 偏硬", "3-4 理想", "5-6 偏软", "7 水样")

    val groupColors = listOf(
        Color(0xFFB98A5E), Color(0xFF8FAE7E), Color(0xFFC4D6AE), Color(0xFF7FA8C9)
    )

    /** 形状分布图的柔和配色 */
    val distColors = listOf(
        Color(0xFFC9925E), Color(0xFFB98A5E), Color(0xFFA58A6F), Color(0xFF8FAE7E),
        Color(0xFF6F9A6B), Color(0xFFE3B04B), Color(0xFF7FA8C9)
    )
}

object Moods {
    val labels = listOf("开心", "平静", "一般", "难受", "痛苦")
}
