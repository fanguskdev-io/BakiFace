package com.fanguskdev.bakiface.presentation

import com.fanguskdev.bakiface.domain.camera.CameraFrame
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectHint
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.WarpOp
import com.fanguskdev.bakiface.testing.FakePicture
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.collections.immutable.persistentListOf

val Ogre = Effect(
    id = EffectId("yujiro"),
    name = "Yujiro Hanma",
    kanji = "鬼",
    accent = Argb(0xFFB3122EL),
    warp = persistentListOf(WarpOp.Move(Feature.Jaw, radius = 0.6f, dx = 0.15f)),
    hint = EffectHint.OpenMouth,
)

val Champion = Effect(
    id = EffectId("baki"),
    name = "Baki Hanma",
    kanji = "刃",
    accent = Argb(0xFFD9480FL),
    warp = persistentListOf(WarpOp.Scale(Feature.Eye, radius = 0.4f, sy = 0.8f)),
)

/** A 720p frame of the front camera, with a face unless told otherwise. */
fun cameraFrame(timestamp: Long = 0L, withFace: Boolean = true) = CameraFrame(
    picture = FakePicture(720, 1280),
    landmarks = if (withFace) syntheticFace() else null,
    mirrored = true,
    timestampNanos = timestamp,
)
