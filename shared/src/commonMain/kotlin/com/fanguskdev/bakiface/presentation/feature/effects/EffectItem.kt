package com.fanguskdev.bakiface.presentation.feature.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectHint
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.presentation.resources.Res
import com.fanguskdev.bakiface.presentation.resources.effect_baki_alias
import com.fanguskdev.bakiface.presentation.resources.effect_baki_description
import com.fanguskdev.bakiface.presentation.resources.effect_doppo_alias
import com.fanguskdev.bakiface.presentation.resources.effect_doppo_description
import com.fanguskdev.bakiface.presentation.resources.effect_hanayama_alias
import com.fanguskdev.bakiface.presentation.resources.effect_hanayama_description
import com.fanguskdev.bakiface.presentation.resources.effect_jack_alias
import com.fanguskdev.bakiface.presentation.resources.effect_jack_description
import com.fanguskdev.bakiface.presentation.resources.effect_kaku_alias
import com.fanguskdev.bakiface.presentation.resources.effect_kaku_description
import com.fanguskdev.bakiface.presentation.resources.effect_oliva_alias
import com.fanguskdev.bakiface.presentation.resources.effect_oliva_description
import com.fanguskdev.bakiface.presentation.resources.effect_original_alias
import com.fanguskdev.bakiface.presentation.resources.effect_original_name
import com.fanguskdev.bakiface.presentation.resources.effect_pickle_alias
import com.fanguskdev.bakiface.presentation.resources.effect_pickle_description
import com.fanguskdev.bakiface.presentation.resources.effect_retsu_alias
import com.fanguskdev.bakiface.presentation.resources.effect_retsu_description
import com.fanguskdev.bakiface.presentation.resources.effect_yujiro_alias
import com.fanguskdev.bakiface.presentation.resources.effect_yujiro_description
import com.fanguskdev.bakiface.render.toColor
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * One entry of the carousel, as the screens show it: a character of the catalogue or, with no [id], the face as it is
 * ([Original]). The name is a proper noun and comes from the catalogue; the alias and the description are translated.
 */
@Immutable
data class EffectItem(
    val id: EffectId?,
    val name: String,
    val kanji: String,
    val accent: Argb,
    val hint: EffectHint?,
) {
    companion object {
        /** The face without any effect, always first in the carousel. 素 is "bare", as in 素顔, the unmade face. */
        val Original = EffectItem(id = null, name = "", kanji = "素", accent = Argb(0xFF8A8782L), hint = null)
    }
}

/** The carousel entry of a character of the catalogue. */
fun Effect.toItem(): EffectItem = EffectItem(id = id, name = name, kanji = kanji, accent = accent, hint = hint)

/** The colour of the character, for the seal and the paper. */
val EffectItem.accentColor: Color get() = accent.toColor()

private val texts: Map<String, Pair<StringResource, StringResource>> = mapOf(
    "baki" to (Res.string.effect_baki_alias to Res.string.effect_baki_description),
    "yujiro" to (Res.string.effect_yujiro_alias to Res.string.effect_yujiro_description),
    "jack" to (Res.string.effect_jack_alias to Res.string.effect_jack_description),
    "hanayama" to (Res.string.effect_hanayama_alias to Res.string.effect_hanayama_description),
    "oliva" to (Res.string.effect_oliva_alias to Res.string.effect_oliva_description),
    "doppo" to (Res.string.effect_doppo_alias to Res.string.effect_doppo_description),
    "retsu" to (Res.string.effect_retsu_alias to Res.string.effect_retsu_description),
    "kaku" to (Res.string.effect_kaku_alias to Res.string.effect_kaku_description),
    "pickle" to (Res.string.effect_pickle_alias to Res.string.effect_pickle_description),
)

/** The name to show: the character's, or "your face" for the [EffectItem.Original]. */
@Composable
fun EffectItem.displayName(): String = if (id == null) stringResource(Res.string.effect_original_name) else name

/** The character's nickname in the person's language; `null` for a character without translated texts. */
@Composable
fun EffectItem.alias(): String? = when (id) {
    null -> stringResource(Res.string.effect_original_alias)
    else -> texts[id.value]?.first?.let { stringResource(it) }
}

/** One line about what the effect does to the face; `null` for the original face or an untranslated character. */
@Composable
fun EffectItem.description(): String? = id?.let { texts[it.value]?.second }?.let { stringResource(it) }
