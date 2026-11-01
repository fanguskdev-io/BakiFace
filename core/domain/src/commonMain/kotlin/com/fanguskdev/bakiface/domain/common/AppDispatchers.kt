package com.fanguskdev.bakiface.domain.common

import kotlinx.coroutines.CoroutineDispatcher

/**
 * The dispatchers the app works on, injected (ADR 0003) so that a test can run everything on its own scheduler.
 * [default] is for computing (the warp of every frame), [io] for files and the photo library.
 */
data class AppDispatchers(val default: CoroutineDispatcher, val io: CoroutineDispatcher)
