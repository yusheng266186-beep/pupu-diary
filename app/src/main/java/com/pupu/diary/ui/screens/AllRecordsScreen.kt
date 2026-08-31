package com.pupu.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.data.Bristol
import com.pupu.diary.data.Moods
import com.pupu.diary.data.PoopRecord
import com.pupu.diary.data.extraInfo
import com.pupu.diary.ui.AppState
import com.pupu.diary.ui.components.BackHeader
import com.pupu.diary.ui.components.BristolIcon
import com.pupu.diary.ui.components.EmptyState
import com.pupu.diary.ui.components.SoftCard
import com.pupu.diary.ui.theme.LocalAppPalette
import com.pupu.diary.util.formatFullCn
import com.pupu.diary.util.toLocalDate

/** 全部记录时间线（v4.0.0）：按月分组、按形状分组筛选，最近记录不再只有 8 条 */
@Composable
fun AllRecordsScreen(state: AppState, onBack: () -> Unit, onEdit: (PoopRecord) -> Unit) {
    val palette = LocalAppPalette.current
    // -1 全部 / 0..3 Bristol 分组（偏硬/理想/偏软/水样）
    var filter by rememberSaveable { mutableIntStateOf(-1) }
    val shown = remember(state.records, filter) {
        if (filter < 0) state.records.toList()
        else state.records.filter { Bristol.groupOf(it.bristol) == filter }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        BackHeader("全部记录", onBack)
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(-1 to "全部") + Bristol.groupNames.mapIndexed { i, n -> i to n }
                .forEach { (g, name) ->
                    FilterChip(name, g == filter) { filter = g }
                }
        }
        Spacer(Modifier.height(6.dp))
        if (shown.isEmpty()) {
            EmptyState(
                title = if (state.records.isEmpty()) "还没有记录" else "这个筛选下没有记录",
                subtitle = if (state.records.isEmpty()) "去记第一条吧，噗噗在等你～" else "换个筛选看看，或回到「全部」。",
                characterSize = 52.dp
            )
            return@Column
        }
        // 拍平成 月份头 + 记录行
        val lines = remember(shown) {
            val out = ArrayList<Pair<String, PoopRecord?>>()
            var lastKey = ""
            shown.forEach { r ->
                val d = r.time.toLocalDate()
                val k = "${d.year} 年 ${d.monthValue} 月"
                if (k != lastKey) {
                    out.add(k to null)
                    lastKey = k
                }
                out.add(k to r)
            }
            out
        }
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
        ) {
            items(lines) { line ->
                val r = line.second
                if (r == null) {
                    Text(
                        line.first,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryDeep,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                } else {
                    TimelineRow(
                        r = r,
                        onEdit = { onEdit(r) },
                        onDelete = { state.removeWithUndo(r) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) palette.primarySoft else palette.card)
            .then(
                if (selected) Modifier.border(1.5.dp, palette.primary, RoundedCornerShape(50))
                else Modifier.border(1.dp, palette.cardAlt, RoundedCornerShape(50))
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) palette.primaryDeep else palette.textSub
        )
    }
}

@Composable
private fun TimelineRow(r: PoopRecord, onEdit: () -> Unit, onDelete: () -> Unit) {
    val palette = LocalAppPalette.current
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onEdit
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                BristolIcon(type = r.bristol, size = 26.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    formatFullCn(r.time),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textMain
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append("${r.bristol}型 ${Bristol.label(r.bristol)} · 心情${Moods.labels[r.mood.coerceIn(0, 4)]} · 顺滑 ${r.smoothness}%")
                        if (r.durationMin > 0) append(" · ${r.durationMin} 分钟")
                        val extra = r.extraInfo()
                        if (extra.isNotEmpty()) append(" · $extra")
                    },
                    fontSize = 11.sp,
                    color = palette.textSub
                )
                if (r.note.isNotBlank()) {
                    Text(
                        "「${r.note}」",
                        fontSize = 11.sp,
                        color = palette.primaryDeep,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                Modifier
                    .size(44.dp)
                    .semantics { contentDescription = "删除这条记录" }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
                    drawLine(
                        palette.textSub,
                        androidx.compose.ui.geometry.Offset(0f, 0f),
                        androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = size.width * 0.16f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        palette.textSub,
                        androidx.compose.ui.geometry.Offset(size.width, 0f),
                        androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = size.width * 0.16f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}
