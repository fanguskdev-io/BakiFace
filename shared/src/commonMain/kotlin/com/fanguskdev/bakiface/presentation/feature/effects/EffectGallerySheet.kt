package com.fanguskdev.bakiface.presentation.feature.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.presentation.designsystem.BakiTheme
import com.fanguskdev.bakiface.presentation.designsystem.KanjiSeal
import com.fanguskdev.bakiface.presentation.designsystem.paperTint
import com.fanguskdev.bakiface.presentation.resources.Res
import com.fanguskdev.bakiface.presentation.resources.gallery_title
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/**
 * Every character at once, as the panels of a manga page: each one on the paper of its colour, with its seal, its
 * nickname and what it does to the face. Choosing one closes the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectGallerySheet(
    effects: ImmutableList<EffectItem>,
    selectedId: EffectId?,
    onChoose: (EffectId?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BakiTheme.colors.background,
    ) {
        Text(
            text = stringResource(Res.string.gallery_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = BakiTheme.spacing.l),
        )
        Spacer(Modifier.height(BakiTheme.spacing.m))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(BakiTheme.spacing.l),
            horizontalArrangement = Arrangement.spacedBy(BakiTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(BakiTheme.spacing.m),
        ) {
            items(effects, key = { it.id?.value ?: "original" }) { item ->
                CharacterPanel(item = item, selected = item.id == selectedId, onClick = { onChoose(item.id) })
            }
        }
    }
}

/** One character as a manga panel: thick ink frame, the paper of its colour, a thicker frame when chosen. */
@Composable
fun CharacterPanel(
    item: EffectItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BakiTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(item.accentColor.paperTint(colors.isDark), MaterialTheme.shapes.small)
            .border(
                if (selected) 5.dp else BakiTheme.strokes.panel,
                if (selected) colors.accent else colors.outline,
                MaterialTheme.shapes.small,
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(BakiTheme.spacing.m),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KanjiSeal(kanji = item.kanji, accent = item.accentColor, size = 44.dp, dark = colors.isDark)
            Spacer(Modifier.width(BakiTheme.spacing.s))
            Text(text = item.displayName(), style = MaterialTheme.typography.titleLarge, color = colors.content)
        }
        item.alias()?.let { alias ->
            Spacer(Modifier.height(BakiTheme.spacing.s))
            Text(text = alias, style = MaterialTheme.typography.labelLarge, color = colors.content)
        }
        item.description()?.let { description ->
            Spacer(Modifier.height(BakiTheme.spacing.xs))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = colors.content)
        }
    }
}
