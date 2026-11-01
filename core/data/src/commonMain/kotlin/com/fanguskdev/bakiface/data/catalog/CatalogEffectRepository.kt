package com.fanguskdev.bakiface.data.catalog

import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The catalogue of characters that ships with the app (ADR 0005), read once from [json] off the main thread and kept.
 * A malformed catalogue is a [CatalogError.Invalid], which the tests catch long before a person could.
 */
class CatalogEffectRepository internal constructor(private val dispatchers: AppDispatchers, private val json: String) : EffectRepository {
    constructor(dispatchers: AppDispatchers) : this(dispatchers, EFFECT_CATALOG_JSON)

    private val mutex = Mutex()
    private var cached: AppResult<ImmutableList<Effect>, CatalogError>? = null

    override suspend fun effects(): AppResult<ImmutableList<Effect>, CatalogError> = mutex.withLock {
        cached ?: withContext(dispatchers.default) { parse() }.also { cached = it }
    }

    private fun parse(): AppResult<ImmutableList<Effect>, CatalogError> = try {
        EffectMapper.toDomain(parser.decodeFromString(CatalogDto.serializer(), json))
    } catch (error: SerializationException) {
        AppResult.Failure(CatalogError.Invalid(error.message.orEmpty()))
    } catch (error: IllegalArgumentException) {
        AppResult.Failure(CatalogError.Invalid(error.message.orEmpty()))
    }

    private companion object {
        val parser = Json { ignoreUnknownKeys = false }
    }
}
