package com.fanguskdev.bakiface.data.catalog

/**
 * The catalogue of characters (ADR 0005), as data: each one a list of local changes to the person's own face
 * (`warp`), what it adds on the skin (`marks`) and its colour (`grade`). Distances are in units of the distance between
 * the eyes; on a paired feature `dx` is outwards, on the middle line it is to the right of the picture; `dy` is down
 * the face. The order is the order of the carousel, the one the owner gave (ADR 0011): Yujiro, Baki, Jack, Oliva and
 * Hanayama. The look is the one of the «Ya me vi Baki» trend: every muscle of the face carved in folds (ADR 0011).
 *
 * Every shape here is original, drawn from the features of the person in front of the camera: no artwork of the manga
 * or the anime is copied (R-01 of `docs/harness/riesgos.md`).
 */
internal const val EFFECT_CATALOG_JSON: String = """
{
  "version": 1,
  "effects": [
    {
      "id": "yujiro",
      "name": "Yujiro Hanma",
      "kanji": "鬼",
      "accent": "#B3122E",
      "hint": "open_mouth",
      "grade": { "contrast": 1.28, "saturation": 0.82, "warmth": 0.10, "tint": "#8A2412", "tintAmount": 0.10 },
      "warp": [
        { "op": "move", "feature": "brow_inner", "radius": 0.34, "dx": -0.06, "dy": 0.13 },
        { "op": "move", "feature": "brow_outer", "radius": 0.36, "dx": 0.03, "dy": -0.12 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 1.06, "sy": 0.82 },
        { "op": "move", "feature": "cheekbone", "radius": 0.50, "dx": 0.07, "dy": -0.08 },
        { "op": "move", "feature": "cheek", "radius": 0.50, "dx": 0.04, "dy": -0.10, "boost": 0.4 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.62, "dx": 0.13, "dy": -0.14, "boost": 0.9 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.24, "sy": 1.02, "boost": 0.5 },
        { "op": "move", "feature": "jaw", "radius": 0.60, "dx": 0.08 },
        { "op": "move", "feature": "gonion", "radius": 0.60, "dx": 0.18, "dy": 0.06, "oy": 0.08 },
        { "op": "scale", "feature": "chin", "radius": 0.62, "sx": 1.28, "sy": 1.06 },
        { "op": "move", "feature": "chin", "radius": 0.72, "dy": 0.07 },
        { "op": "scale", "feature": "nose_base", "radius": 0.42, "sx": 1.24, "sy": 1.06 },
        { "op": "move", "feature": "neck", "radius": 1.00, "dx": 0.26 }
      ],
      "marks": [
        { "type": "folds", "region": "forehead", "count": 6, "depth": 0.95 },
        { "type": "folds", "region": "frown", "count": 3, "depth": 1.0 },
        { "type": "folds", "region": "crows_feet", "count": 4, "depth": 0.9 },
        { "type": "folds", "region": "under_eyes", "count": 2, "depth": 0.85 },
        { "type": "folds", "region": "nose_bridge", "count": 2, "depth": 0.8 },
        { "type": "folds", "region": "smile", "count": 3, "depth": 1.0, "boost": 0.4 },
        { "type": "folds", "region": "cheeks", "count": 2, "depth": 0.8, "boost": 0.3 },
        { "type": "folds", "region": "mouth", "count": 2, "depth": 0.8 },
        { "type": "folds", "region": "chin", "count": 3, "depth": 0.8 },
        { "type": "folds", "region": "neck", "count": 3, "depth": 0.9 },
        { "type": "blank_eyes", "color": "#F2EDE4", "alpha": 0.92 },
        { "type": "ink_brows", "width": 0.13, "color": "#140D0A", "alpha": 0.88 },
        { "type": "vein", "width": 0.040, "mirrored": true, "boost": 0.8, "path": [
          { "feature": "temple", "side": "left", "dx": 0.04, "dy": 0.12 },
          { "feature": "temple", "side": "left", "dx": 0.10, "dy": -0.06 },
          { "feature": "temple", "side": "left", "dx": 0.20, "dy": -0.20 },
          { "feature": "temple", "side": "left", "dx": 0.26, "dy": -0.38 } ] },
        { "type": "vein", "width": 0.034, "boost": 1.0, "path": [
          { "feature": "brow_inner", "side": "left", "dx": 0.02, "dy": -0.10 },
          { "feature": "forehead", "side": "left", "dx": -0.14, "dy": 0.10 },
          { "feature": "forehead", "side": "left", "dx": -0.26, "dy": -0.06 },
          { "feature": "forehead", "side": "left", "dx": -0.34, "dy": -0.26 } ] },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.06, "dy": 0.02 },
          "rx": 0.24, "ry": 0.36, "color": "#3E2018", "alpha": 0.30, "blend": "multiply" },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheekbone", "side": "left", "dx": -0.06, "dy": -0.04 },
          "rx": 0.20, "ry": 0.10, "color": "#FFFFFF", "alpha": 0.14, "blend": "screen" }
      ]
    },
    {
      "id": "baki",
      "name": "Baki Hanma",
      "kanji": "刃",
      "accent": "#D9480F",
      "hint": "open_mouth",
      "grade": { "contrast": 1.26, "saturation": 0.86, "warmth": 0.12, "tint": "#9A5A2A", "tintAmount": 0.10 },
      "warp": [
        { "op": "move", "feature": "brow_inner", "radius": 0.32, "dx": -0.04, "dy": 0.06 },
        { "op": "move", "feature": "brow_outer", "radius": 0.34, "dy": -0.06 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 1.04, "sy": 0.72 },
        { "op": "move", "feature": "cheekbone", "radius": 0.48, "dx": 0.05, "dy": -0.08 },
        { "op": "move", "feature": "cheek", "radius": 0.50, "dx": 0.03, "dy": -0.10, "boost": 0.4 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.62, "dx": 0.14, "dy": -0.13, "boost": 0.9 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.26, "sy": 1.04, "boost": 0.5 },
        { "op": "move", "feature": "gonion", "radius": 0.58, "dx": 0.12, "dy": 0.04, "oy": 0.06 },
        { "op": "scale", "feature": "chin", "radius": 0.52, "sx": 1.16 },
        { "op": "move", "feature": "neck", "radius": 0.95, "dx": 0.20 }
      ],
      "marks": [
        { "type": "folds", "region": "forehead", "count": 5, "depth": 0.9 },
        { "type": "folds", "region": "frown", "count": 3, "depth": 0.85 },
        { "type": "folds", "region": "crows_feet", "count": 5, "depth": 1.0 },
        { "type": "folds", "region": "under_eyes", "count": 2, "depth": 0.9 },
        { "type": "folds", "region": "nose_bridge", "count": 2, "depth": 0.7 },
        { "type": "folds", "region": "smile", "count": 3, "depth": 1.0, "boost": 0.4 },
        { "type": "folds", "region": "cheeks", "count": 2, "depth": 0.85, "boost": 0.3 },
        { "type": "folds", "region": "mouth", "count": 2, "depth": 0.75 },
        { "type": "folds", "region": "chin", "count": 2, "depth": 0.7 },
        { "type": "folds", "region": "neck", "count": 3, "depth": 0.85 },
        { "type": "blank_eyes", "color": "#F4F0E8", "alpha": 0.9 },
        { "type": "ink_brows", "width": 0.10, "color": "#1A110C", "alpha": 0.8 },
        { "type": "scar", "width": 0.040, "path": [
          { "feature": "cheekbone", "side": "left", "dx": 0.12, "dy": -0.14 },
          { "feature": "cheekbone", "side": "left", "dx": -0.02, "dy": 0.02 },
          { "feature": "cheek", "side": "left", "dx": -0.10, "dy": -0.18 } ] },
        { "type": "scar", "width": 0.034, "path": [
          { "feature": "brow_mid", "side": "right", "dx": -0.04, "dy": -0.14 },
          { "feature": "brow_mid", "side": "right", "dx": 0.04, "dy": 0.06 } ] },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.06, "dy": 0.02 },
          "rx": 0.22, "ry": 0.34, "color": "#3E2418", "alpha": 0.26, "blend": "multiply" },
        { "type": "shade", "at": { "feature": "nose_tip", "dy": -0.12 },
          "rx": 0.05, "ry": 0.12, "color": "#FFFFFF", "alpha": 0.18, "blend": "screen" }
      ]
    },
    {
      "id": "jack",
      "name": "Jack Hanma",
      "kanji": "牙",
      "accent": "#C9A227",
      "hint": "open_mouth",
      "grade": { "contrast": 1.22, "saturation": 0.84, "warmth": 0.08, "tint": "#7A5A2A", "tintAmount": 0.06 },
      "warp": [
        { "op": "move", "feature": "chin", "radius": 1.15, "dy": 0.36 },
        { "op": "move", "feature": "gonion", "radius": 0.85, "dx": 0.16, "dy": 0.12 },
        { "op": "move", "feature": "jaw", "radius": 0.75, "dx": 0.11, "dy": 0.10 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.24, "sy": 1.08, "boost": 0.6 },
        { "op": "move", "feature": "mouth", "radius": 0.90, "dy": 0.10 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.55, "dx": 0.08, "dy": -0.06, "boost": 0.8 },
        { "op": "scale", "feature": "eye", "radius": 0.38, "sx": 0.88, "sy": 0.76 },
        { "op": "move", "feature": "brow_mid", "radius": 0.42, "dy": 0.08 },
        { "op": "scale", "feature": "nose_base", "radius": 0.45, "sx": 1.16, "sy": 1.10 },
        { "op": "move", "feature": "neck", "radius": 1.00, "dx": 0.26 }
      ],
      "marks": [
        { "type": "folds", "region": "forehead", "count": 4, "depth": 0.8 },
        { "type": "folds", "region": "frown", "count": 2, "depth": 0.9 },
        { "type": "folds", "region": "under_eyes", "count": 2, "depth": 0.7 },
        { "type": "folds", "region": "smile", "count": 3, "depth": 0.9, "boost": 0.4 },
        { "type": "folds", "region": "cheeks", "count": 3, "depth": 0.8 },
        { "type": "folds", "region": "mouth", "count": 3, "depth": 0.85 },
        { "type": "folds", "region": "chin", "count": 3, "depth": 0.9 },
        { "type": "folds", "region": "neck", "count": 4, "depth": 0.95 },
        { "type": "ink_brows", "width": 0.11, "color": "#1C130E", "alpha": 0.8 },
        { "type": "scar", "width": 0.034, "stitches": true, "path": [
          { "feature": "forehead", "dx": -0.42, "dy": -0.12 },
          { "feature": "brow_inner", "side": "left", "dx": 0.02, "dy": -0.08 },
          { "feature": "nose_tip", "dx": 0.06, "dy": -0.32 },
          { "feature": "cheek", "side": "right", "dx": -0.12, "dy": -0.05 } ] },
        { "type": "scar", "width": 0.030, "stitches": true, "path": [
          { "feature": "lower_lip", "dx": 0.12, "dy": 0.10 },
          { "feature": "chin", "dx": 0.10, "dy": -0.04 } ] },
        { "type": "shade", "mirrored": true, "at": { "feature": "gonion", "side": "left", "dx": 0.15 },
          "rx": 0.30, "ry": 0.35, "color": "#3A2A22", "alpha": 0.24, "blend": "multiply" }
      ]
    },
    {
      "id": "oliva",
      "name": "Biscuit Oliva",
      "kanji": "鎖",
      "accent": "#2B59C3",
      "grade": { "contrast": 1.18, "saturation": 0.9, "warmth": 0.10, "tint": "#8A5A30", "tintAmount": 0.06 },
      "warp": [
        { "op": "move", "feature": "cheek", "radius": 0.90, "dx": 0.16, "dy": -0.04 },
        { "op": "move", "feature": "gonion", "radius": 0.90, "dx": 0.22, "dy": 0.05 },
        { "op": "move", "feature": "jaw", "radius": 0.80, "dx": 0.13 },
        { "op": "move", "feature": "neck", "radius": 1.15, "dx": 0.34 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 0.90, "sy": 0.74 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.22 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.55, "dx": 0.10, "dy": -0.11, "boost": 0.6 },
        { "op": "scale", "feature": "cheekbone", "radius": 0.45, "sx": 1.10, "sy": 1.08 }
      ],
      "marks": [
        { "type": "folds", "region": "forehead", "count": 3, "depth": 0.7 },
        { "type": "folds", "region": "crows_feet", "count": 4, "depth": 0.9 },
        { "type": "folds", "region": "under_eyes", "count": 2, "depth": 0.7 },
        { "type": "folds", "region": "smile", "count": 3, "depth": 0.95 },
        { "type": "folds", "region": "cheeks", "count": 3, "depth": 0.8 },
        { "type": "folds", "region": "neck", "count": 5, "depth": 1.0 },
        { "type": "shade", "at": { "feature": "upper_lip", "dy": -0.12 },
          "rx": 0.40, "ry": 0.10, "color": "#2A1E18", "alpha": 0.25, "blend": "multiply" },
        { "type": "hair", "mirrored": true, "count": 115, "length": 0.16, "angle": 24, "spread": 26,
          "color": "#2A1C14", "width": 0.011,
          "from": { "feature": "upper_lip", "side": "left", "dx": -0.02, "dy": -0.13 },
          "to": { "feature": "mouth_corner", "side": "left", "dx": -0.04, "dy": -0.12 } },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.06, "dy": -0.10 },
          "rx": 0.22, "ry": 0.16, "color": "#FFFFFF", "alpha": 0.12, "blend": "screen" }
      ]
    },
    {
      "id": "hanayama",
      "name": "Kaoru Hanayama",
      "kanji": "侠",
      "accent": "#5C677D",
      "grade": { "contrast": 1.2, "saturation": 0.8, "warmth": 0.02, "tint": "#39465A", "tintAmount": 0.06 },
      "warp": [
        { "op": "move", "feature": "gonion", "radius": 0.85, "dx": 0.18, "dy": 0.04 },
        { "op": "move", "feature": "cheek", "radius": 0.80, "dx": 0.10 },
        { "op": "scale", "feature": "chin", "radius": 0.62, "sx": 1.30 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sy": 0.78 },
        { "op": "scale", "feature": "nose_base", "radius": 0.42, "sx": 1.22, "sy": 0.95 },
        { "op": "move", "feature": "brow_mid", "radius": 0.40, "dy": 0.05 },
        { "op": "move", "feature": "neck", "radius": 1.00, "dx": 0.24 }
      ],
      "marks": [
        { "type": "folds", "region": "forehead", "count": 3, "depth": 0.7 },
        { "type": "folds", "region": "frown", "count": 2, "depth": 0.75 },
        { "type": "folds", "region": "under_eyes", "count": 2, "depth": 0.7 },
        { "type": "folds", "region": "smile", "count": 2, "depth": 0.8 },
        { "type": "folds", "region": "cheeks", "count": 2, "depth": 0.7 },
        { "type": "folds", "region": "neck", "count": 3, "depth": 0.8 },
        { "type": "ink_brows", "width": 0.09, "color": "#1E1612", "alpha": 0.7 },
        { "type": "scar", "width": 0.048, "path": [
          { "feature": "forehead", "dx": -0.50, "dy": -0.22 },
          { "feature": "forehead", "dx": -0.10, "dy": -0.02 },
          { "feature": "forehead", "dx": 0.32, "dy": 0.14 } ] },
        { "type": "scar", "width": 0.044, "path": [
          { "feature": "brow_mid", "side": "left", "dx": 0.04, "dy": -0.24 },
          { "feature": "brow_mid", "side": "left", "dx": 0.07, "dy": 0.02 } ] },
        { "type": "scar", "width": 0.042, "path": [
          { "feature": "eye", "side": "left", "dx": 0.02, "dy": 0.16 },
          { "feature": "cheekbone", "side": "left", "dx": 0.06, "dy": 0.12 },
          { "feature": "cheek", "side": "left", "dx": 0.02, "dy": 0.08 } ] },
        { "type": "scar", "width": 0.05, "path": [
          { "feature": "cheekbone", "side": "right", "dx": -0.12, "dy": -0.08 },
          { "feature": "cheek", "side": "right", "dy": 0.05 },
          { "feature": "jaw", "side": "right", "dx": -0.12, "dy": 0.05 } ] },
        { "type": "scar", "width": 0.036, "path": [
          { "feature": "nose_tip", "dx": -0.14, "dy": -0.38 },
          { "feature": "nose_tip", "dx": 0.14, "dy": -0.42 } ] },
        { "type": "scar", "width": 0.038, "path": [
          { "feature": "chin", "dx": -0.20, "dy": -0.12 },
          { "feature": "chin", "dx": 0.05, "dy": -0.02 } ] },
        { "type": "scar", "width": 0.032, "path": [
          { "feature": "upper_lip", "dx": 0.10, "dy": -0.10 },
          { "feature": "upper_lip", "dx": 0.16, "dy": 0.06 } ] }
      ]
    }
  ]
}
"""
