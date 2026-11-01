package com.fanguskdev.bakiface.domain.effect

import com.fanguskdev.bakiface.domain.common.AppResult
import kotlinx.collections.immutable.ImmutableList

/** Why the catalogue of effects could not be read. */
sealed interface CatalogError {
    /** The catalogue is malformed; [reason] says where, for the log. */
    data class Invalid(val reason: String) : CatalogError
}

/** The characters the app knows, in the order the carousel shows them. */
interface EffectRepository {
    suspend fun effects(): AppResult<ImmutableList<Effect>, CatalogError>
}
