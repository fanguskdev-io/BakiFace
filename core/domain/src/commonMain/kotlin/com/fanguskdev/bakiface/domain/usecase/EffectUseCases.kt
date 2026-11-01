package com.fanguskdev.bakiface.domain.usecase

import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.effect.EffectRepository
import com.fanguskdev.bakiface.domain.settings.PreferencesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

/** The characters of the catalogue, in carousel order. */
class GetEffectsUseCase(private val repository: EffectRepository) {
    suspend operator fun invoke(): AppResult<ImmutableList<Effect>, CatalogError> = repository.effects()
}

/** The effect the person chose last, remembered between launches; `null` is the face as it is. */
class ObserveSelectedEffectUseCase(private val preferences: PreferencesRepository) {
    operator fun invoke(): Flow<EffectId?> = preferences.selectedEffectId
}

/** Remembers the effect the person has just chosen; `null` goes back to the face as it is. */
class SelectEffectUseCase(private val preferences: PreferencesRepository) {
    suspend operator fun invoke(id: EffectId?) = preferences.setSelectedEffectId(id)
}
