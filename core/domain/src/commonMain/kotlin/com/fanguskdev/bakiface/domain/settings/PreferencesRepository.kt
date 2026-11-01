package com.fanguskdev.bakiface.domain.settings

import com.fanguskdev.bakiface.domain.effect.EffectId
import kotlinx.coroutines.flow.Flow

/** What the app remembers between launches, on the phone only (ADR 0007). */
interface PreferencesRepository {
    /** The effect chosen last; `null` is the face as it is. */
    val selectedEffectId: Flow<EffectId?>

    suspend fun setSelectedEffectId(id: EffectId?)
}
