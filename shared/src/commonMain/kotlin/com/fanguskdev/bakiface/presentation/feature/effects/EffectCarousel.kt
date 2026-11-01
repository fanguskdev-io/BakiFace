package com.fanguskdev.bakiface.presentation.feature.effects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fanguskdev.bakiface.presentation.designsystem.KanjiSeal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

private val SealSize = 56.dp
private val SealGap = 18.dp
private val CarouselHeight = 96.dp

/**
 * The row of seals of the camera and the editor, like the effects of Instagram's camera: it snaps a seal to the middle,
 * and the seal in the middle is the chosen one. A tap on another seal brings it to the middle. [center] is drawn over
 * the middle (the shutter on the camera, a ring on the editor). Stateless: [selectedIndex] comes from the state and
 * every choice goes out through [onSelected].
 */
@Composable
fun EffectCarousel(
    effects: ImmutableList<EffectItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    center: @Composable BoxScope.() -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    val currentOnSelected by rememberUpdatedState(onSelected)
    val currentSelected by rememberUpdatedState(selectedIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(listState, SnapPosition.Center)
    val scope = rememberCoroutineScope()
    val centered by remember { derivedStateOf { listState.layoutInfo.centeredIndex() } }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to listState.layoutInfo.centeredIndex() }
            .filter { (scrolling, _) -> !scrolling }
            .map { (_, index) -> index }
            .distinctUntilChanged()
            .collect { index -> if (index != null && index != currentSelected) currentOnSelected(index) }
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            centered
        }.distinctUntilChanged().collect { if (listState.isScrollInProgress) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    }
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && centered != selectedIndex) listState.animateScrollToItem(selectedIndex)
    }

    BoxWithConstraints(modifier.fillMaxWidth().height(CarouselHeight)) {
        val sidePadding: Dp = ((maxWidth - SealSize) / 2).coerceAtLeast(0.dp)
        LazyRow(
            state = listState,
            flingBehavior = fling,
            contentPadding = PaddingValues(horizontal = sidePadding),
            horizontalArrangement = Arrangement.spacedBy(SealGap),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center),
        ) {
            itemsIndexed(effects, key = { _, item -> item.id?.value ?: "original" }) { index, item ->
                val label = item.displayName()
                KanjiSeal(
                    kanji = item.kanji,
                    accent = item.accentColor,
                    size = SealSize,
                    dark = dark,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.RadioButton) { scope.launch { listState.animateScrollToItem(index) } }
                        .semantics {
                            contentDescription = label
                            selected = index == selectedIndex
                        },
                )
            }
        }
        Box(Modifier.align(Alignment.Center), content = center)
    }
}

/** The index of the item whose middle is nearest the middle of the row. */
private fun LazyListLayoutInfo.centeredIndex(): Int? {
    val middle = (viewportStartOffset + viewportEndOffset) / 2
    return visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - middle) }?.index
}
