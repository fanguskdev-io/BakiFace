package com.fanguskdev.bakiface.data.catalog

/**
 * The catalogue of characters (ADR 0005), as data: each one a list of local changes to the person's own face
 * (`warp`), what it adds on the skin (`marks`) and its colour (`grade`). Distances are in units of the distance between
 * the eyes; on a paired feature `dx` is outwards, on the middle line it is to the right of the picture; `dy` is down
 * the face. The order is the order of the carousel.
 *
 * Every shape here is original, drawn from the features of the person in front of the camera: no artwork of the manga
 * or the anime is copied (R-01 of `docs/harness/riesgos.md`).
 */
internal const val EFFECT_CATALOG_JSON: String = """
{
  "version": 1,
  "effects": [
    {
      "id": "baki",
      "name": "Baki Hanma",
      "kanji": "刃",
      "accent": "#D9480F",
      "grade": { "contrast": 1.12, "saturation": 1.05, "warmth": 0.04 },
      "warp": [
        { "op": "move", "feature": "brow_inner", "radius": 0.32, "dx": -0.05, "dy": 0.10 },
        { "op": "move", "feature": "brow_outer", "radius": 0.32, "dy": -0.03 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 1.04, "sy": 0.78 },
        { "op": "move", "feature": "cheekbone", "radius": 0.42, "dx": 0.04, "dy": -0.04 },
        { "op": "move", "feature": "cheek", "radius": 0.36, "dx": -0.04 },
        { "op": "move", "feature": "gonion", "radius": 0.55, "dx": 0.12, "dy": 0.04, "oy": 0.06 },
        { "op": "scale", "feature": "chin", "radius": 0.50, "sx": 1.15 },
        { "op": "scale", "feature": "mouth", "radius": 0.60, "sx": 1.04, "sy": 0.90 },
        { "op": "move", "feature": "neck", "radius": 0.90, "dx": 0.16 }
      ],
      "marks": [
        { "type": "scar", "width": 0.045, "path": [
          { "feature": "cheekbone", "side": "left", "dx": 0.12, "dy": -0.14 },
          { "feature": "cheekbone", "side": "left", "dx": -0.02, "dy": 0.02 },
          { "feature": "cheek", "side": "left", "dx": -0.10, "dy": -0.18 } ] },
        { "type": "scar", "width": 0.038, "path": [
          { "feature": "brow_mid", "side": "right", "dx": -0.04, "dy": -0.14 },
          { "feature": "brow_mid", "side": "right", "dx": 0.04, "dy": 0.06 } ] },
        { "type": "scar", "width": 0.032, "path": [
          { "feature": "chin", "dx": 0.08, "dy": -0.10 },
          { "feature": "chin", "dx": 0.24, "dy": -0.02 } ] },
        { "type": "vein", "width": 0.034, "path": [
          { "feature": "temple", "side": "right", "dx": -0.05, "dy": 0.05 },
          { "feature": "temple", "side": "right", "dx": -0.10, "dy": -0.15 },
          { "feature": "temple", "side": "right", "dx": -0.06, "dy": -0.32 } ] },
        { "type": "wrinkle", "depth": 0.8, "mirrored": true, "path": [
          { "feature": "brow_inner", "side": "left", "dx": 0.03 },
          { "feature": "brow_inner", "side": "left", "dx": 0.05, "dy": -0.18 } ] },
        { "type": "shade", "at": { "feature": "eye_outer", "side": "right", "dx": 0.04, "dy": 0.10 },
          "rx": 0.20, "ry": 0.14, "color": "#4B2C5E", "alpha": 0.42, "blend": "multiply" },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.04, "dy": -0.10 },
          "rx": 0.20, "ry": 0.34, "color": "#4A2A20", "alpha": 0.22, "blend": "multiply" },
        { "type": "shade", "at": { "feature": "forehead", "dx": 0.15, "dy": 0.05 },
          "rx": 0.14, "ry": 0.08, "color": "#FFFFFF", "alpha": 0.2, "blend": "screen" },
        { "type": "shade", "at": { "feature": "nose_tip", "dy": -0.12 },
          "rx": 0.05, "ry": 0.12, "color": "#FFFFFF", "alpha": 0.18, "blend": "screen" }
      ]
    },
    {
      "id": "yujiro",
      "name": "Yujiro Hanma",
      "kanji": "鬼",
      "accent": "#B3122E",
      "hint": "open_mouth",
      "grade": { "contrast": 1.16, "saturation": 0.85, "warmth": 0.06, "tint": "#7A0A12", "tintAmount": 0.08 },
      "warp": [
        { "op": "move", "feature": "brow_inner", "radius": 0.32, "dx": -0.06, "dy": 0.12 },
        { "op": "move", "feature": "brow_outer", "radius": 0.34, "dx": 0.02, "dy": -0.08 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 1.06, "sy": 0.70 },
        { "op": "move", "feature": "eye_outer", "radius": 0.22, "dy": -0.05 },
        { "op": "move", "feature": "cheekbone", "radius": 0.45, "dx": 0.06, "dy": -0.05 },
        { "op": "move", "feature": "jaw", "radius": 0.55, "dx": 0.05 },
        { "op": "move", "feature": "gonion", "radius": 0.55, "dx": 0.17, "dy": 0.06, "oy": 0.08 },
        { "op": "move", "feature": "cheek", "radius": 0.38, "dx": -0.05, "dy": 0.02 },
        { "op": "scale", "feature": "chin", "radius": 0.60, "sx": 1.25, "sy": 1.06 },
        { "op": "move", "feature": "chin", "radius": 0.70, "dy": 0.06 },
        { "op": "scale", "feature": "nose_base", "radius": 0.40, "sx": 1.20 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.12, "sy": 0.96, "boost": 0.5 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.30, "dx": 0.05, "dy": -0.08, "boost": 1.0 },
        { "op": "move", "feature": "neck", "radius": 0.95, "dx": 0.20 }
      ],
      "marks": [
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
        { "type": "vein", "width": 0.024, "boost": 1.2, "path": [
          { "feature": "forehead", "side": "left", "dx": -0.14, "dy": 0.10 },
          { "feature": "forehead", "side": "left", "dx": -0.04, "dy": -0.04 },
          { "feature": "forehead", "side": "left", "dx": -0.07, "dy": -0.22 } ] },
        { "type": "vein", "width": 0.030, "boost": 1.0, "path": [
          { "feature": "brow_inner", "side": "right", "dx": -0.04, "dy": -0.16 },
          { "feature": "forehead", "side": "right", "dx": 0.18, "dy": 0.02 },
          { "feature": "forehead", "side": "right", "dx": 0.22, "dy": -0.20 } ] },
        { "type": "wrinkle", "depth": 0.9, "mirrored": true, "path": [
          { "feature": "brow_inner", "side": "left", "dx": 0.04 },
          { "feature": "brow_inner", "side": "left", "dx": 0.07, "dy": -0.22 } ] },
        { "type": "wrinkle", "depth": 0.85, "mirrored": true, "path": [
          { "feature": "nose_base", "side": "left", "dx": -0.24, "dy": -0.06 },
          { "feature": "nose_base", "side": "left", "dx": -0.38, "dy": 0.18 },
          { "feature": "mouth_corner", "side": "left", "dx": -0.06, "dy": 0.02 } ] },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.04, "dy": -0.12 },
          "rx": 0.22, "ry": 0.38, "color": "#4A2A20", "alpha": 0.28, "blend": "multiply" },
        { "type": "shade", "mirrored": true, "at": { "feature": "eye", "side": "left", "dy": -0.04 },
          "rx": 0.30, "ry": 0.16, "color": "#3A1E1E", "alpha": 0.22, "blend": "multiply" },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheekbone", "side": "left", "dx": -0.05, "dy": -0.05 },
          "rx": 0.20, "ry": 0.10, "color": "#FFFFFF", "alpha": 0.10, "blend": "screen" }
      ]
    },
    {
      "id": "jack",
      "name": "Jack Hanma",
      "kanji": "牙",
      "accent": "#C9A227",
      "hint": "open_mouth",
      "grade": { "contrast": 1.12, "saturation": 0.92, "warmth": 0.04 },
      "warp": [
        { "op": "move", "feature": "chin", "radius": 1.15, "dy": 0.36 },
        { "op": "move", "feature": "gonion", "radius": 0.85, "dx": 0.15, "dy": 0.12 },
        { "op": "move", "feature": "jaw", "radius": 0.75, "dx": 0.10, "dy": 0.10 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.20, "sy": 1.08, "boost": 0.6 },
        { "op": "move", "feature": "mouth", "radius": 0.90, "dy": 0.10 },
        { "op": "scale", "feature": "eye", "radius": 0.38, "sx": 0.90, "sy": 0.80 },
        { "op": "move", "feature": "brow_mid", "radius": 0.42, "dy": 0.07 },
        { "op": "scale", "feature": "nose_base", "radius": 0.45, "sx": 1.15, "sy": 1.10 },
        { "op": "move", "feature": "neck", "radius": 1.00, "dx": 0.24 }
      ],
      "marks": [
        { "type": "scar", "width": 0.034, "stitches": true, "path": [
          { "feature": "forehead", "dx": -0.42, "dy": -0.12 },
          { "feature": "brow_inner", "side": "left", "dx": 0.02, "dy": -0.08 },
          { "feature": "nose_tip", "dx": 0.06, "dy": -0.32 },
          { "feature": "cheek", "side": "right", "dx": -0.12, "dy": -0.05 } ] },
        { "type": "scar", "width": 0.030, "stitches": true, "path": [
          { "feature": "lower_lip", "dx": 0.12, "dy": 0.10 },
          { "feature": "chin", "dx": 0.10, "dy": -0.04 } ] },
        { "type": "vein", "width": 0.034, "mirrored": true, "path": [
          { "feature": "forehead", "side": "left", "dx": -0.25, "dy": 0.20 },
          { "feature": "forehead", "side": "left", "dx": -0.15, "dy": -0.05 },
          { "feature": "forehead", "side": "left", "dx": -0.20, "dy": -0.25 } ] },
        { "type": "shade", "mirrored": true, "at": { "feature": "gonion", "side": "left", "dx": 0.15 },
          "rx": 0.30, "ry": 0.35, "color": "#3A2A22", "alpha": 0.20, "blend": "multiply" }
      ]
    },
    {
      "id": "hanayama",
      "name": "Kaoru Hanayama",
      "kanji": "侠",
      "accent": "#5C677D",
      "grade": { "contrast": 1.12, "saturation": 0.88, "warmth": 0.02 },
      "warp": [
        { "op": "move", "feature": "gonion", "radius": 0.85, "dx": 0.18, "dy": 0.04 },
        { "op": "move", "feature": "cheek", "radius": 0.80, "dx": 0.10 },
        { "op": "scale", "feature": "chin", "radius": 0.62, "sx": 1.30 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sy": 0.78 },
        { "op": "scale", "feature": "nose_base", "radius": 0.42, "sx": 1.22, "sy": 0.95 },
        { "op": "move", "feature": "brow_mid", "radius": 0.40, "dy": 0.04 },
        { "op": "move", "feature": "neck", "radius": 1.00, "dx": 0.22 }
      ],
      "marks": [
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
    },
    {
      "id": "oliva",
      "name": "Biscuit Oliva",
      "kanji": "鎖",
      "accent": "#2B59C3",
      "grade": { "contrast": 1.06, "warmth": 0.05 },
      "warp": [
        { "op": "move", "feature": "cheek", "radius": 0.90, "dx": 0.15 },
        { "op": "move", "feature": "gonion", "radius": 0.90, "dx": 0.20, "dy": 0.05 },
        { "op": "move", "feature": "jaw", "radius": 0.80, "dx": 0.12 },
        { "op": "move", "feature": "neck", "radius": 1.15, "dx": 0.30 },
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 0.90, "sy": 0.80 },
        { "op": "scale", "feature": "mouth", "radius": 0.95, "sx": 1.16 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.30, "dx": 0.04, "dy": -0.08 },
        { "op": "scale", "feature": "cheekbone", "radius": 0.45, "sx": 1.08, "sy": 1.08 }
      ],
      "marks": [
        { "type": "shade", "at": { "feature": "upper_lip", "dy": -0.12 },
          "rx": 0.40, "ry": 0.10, "color": "#2A1E18", "alpha": 0.25, "blend": "multiply" },
        { "type": "hair", "mirrored": true, "count": 115, "length": 0.16, "angle": 24, "spread": 26,
          "color": "#2A1C14", "width": 0.011,
          "from": { "feature": "upper_lip", "side": "left", "dx": -0.02, "dy": -0.13 },
          "to": { "feature": "mouth_corner", "side": "left", "dx": -0.04, "dy": -0.12 } },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.06, "dy": -0.10 },
          "rx": 0.22, "ry": 0.16, "color": "#FFFFFF", "alpha": 0.10, "blend": "screen" }
      ]
    },
    {
      "id": "doppo",
      "name": "Doppo Orochi",
      "kanji": "虎",
      "accent": "#D97706",
      "grade": { "contrast": 1.10, "saturation": 0.95, "warmth": 0.03 },
      "warp": [
        { "op": "move", "feature": "gonion", "radius": 0.80, "dx": 0.14, "dy": 0.03 },
        { "op": "scale", "feature": "chin", "radius": 0.60, "sx": 1.20 },
        { "op": "scale", "feature": "nose_base", "radius": 0.45, "sx": 1.25, "sy": 1.05 },
        { "op": "move", "feature": "brow_inner", "radius": 0.32, "dx": -0.04, "dy": 0.08 },
        { "op": "scale", "feature": "eye", "sides": "left", "radius": 0.42, "sy": 0.32 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.30, "dx": 0.03, "dy": -0.04 },
        { "op": "move", "feature": "neck", "radius": 0.95, "dx": 0.18 }
      ],
      "marks": [
        { "type": "scar", "width": 0.05, "path": [
          { "feature": "brow_mid", "side": "left", "dx": 0.02, "dy": -0.30 },
          { "feature": "eye", "side": "left", "dx": 0.02, "dy": -0.02 },
          { "feature": "cheekbone", "side": "left", "dx": 0.08, "dy": 0.16 } ] },
        { "type": "shade", "at": { "feature": "eye", "side": "left", "dy": 0.02 },
          "rx": 0.26, "ry": 0.16, "color": "#3B2420", "alpha": 0.30, "blend": "multiply" },
        { "type": "wrinkle", "depth": 0.7, "mirrored": true, "path": [
          { "feature": "brow_inner", "side": "left", "dx": 0.04 },
          { "feature": "brow_inner", "side": "left", "dx": 0.06, "dy": -0.20 } ] }
      ]
    },
    {
      "id": "retsu",
      "name": "Retsu Kaioh",
      "kanji": "烈",
      "accent": "#2D6A4F",
      "grade": { "contrast": 1.14, "saturation": 0.92, "warmth": -0.03 },
      "warp": [
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 1.10, "sy": 0.70 },
        { "op": "move", "feature": "eye_outer", "radius": 0.25, "dx": 0.04, "dy": -0.08 },
        { "op": "move", "feature": "brow_outer", "radius": 0.35, "dx": 0.03, "dy": -0.10 },
        { "op": "move", "feature": "brow_inner", "radius": 0.30, "dx": -0.03, "dy": 0.06 },
        { "op": "move", "feature": "cheekbone", "radius": 0.45, "dx": 0.06, "dy": -0.07 },
        { "op": "move", "feature": "cheek", "radius": 0.40, "dx": -0.07 },
        { "op": "move", "feature": "gonion", "radius": 0.70, "dx": -0.10 },
        { "op": "scale", "feature": "chin", "radius": 0.55, "sx": 0.80, "sy": 1.12 },
        { "op": "scale", "feature": "nose_base", "radius": 0.40, "sx": 0.88 },
        { "op": "scale", "feature": "mouth", "radius": 0.55, "sx": 0.94, "sy": 0.88 }
      ],
      "marks": [
        { "type": "shade", "mirrored": true, "at": { "feature": "cheek", "side": "left", "dx": 0.04, "dy": -0.12 },
          "rx": 0.22, "ry": 0.40, "color": "#3A2620", "alpha": 0.34, "blend": "multiply" },
        { "type": "shade", "mirrored": true, "at": { "feature": "cheekbone", "side": "left", "dx": -0.04, "dy": -0.04 },
          "rx": 0.20, "ry": 0.09, "color": "#FFFFFF", "alpha": 0.16, "blend": "screen" },
        { "type": "shade", "mirrored": true, "at": { "feature": "eye", "side": "left", "dy": -0.06 },
          "rx": 0.30, "ry": 0.13, "color": "#2B1D18", "alpha": 0.24, "blend": "multiply" },
        { "type": "wrinkle", "depth": 0.7, "mirrored": true, "path": [
          { "feature": "brow_inner", "side": "left", "dx": 0.03 },
          { "feature": "brow_inner", "side": "left", "dx": 0.05, "dy": -0.18 } ] },
        { "type": "wrinkle", "depth": 0.6, "mirrored": true, "path": [
          { "feature": "nose_base", "side": "left", "dx": -0.22, "dy": -0.04 },
          { "feature": "mouth_corner", "side": "left", "dx": -0.05, "dy": -0.02 } ] }
      ]
    },
    {
      "id": "kaku",
      "name": "Kaku Kaioh",
      "kanji": "皇",
      "accent": "#A68A64",
      "grade": { "contrast": 1.05, "saturation": 0.45, "warmth": 0.10 },
      "warp": [
        { "op": "scale", "feature": "eye", "radius": 0.40, "sx": 0.95, "sy": 0.60 },
        { "op": "move", "feature": "eye_outer", "radius": 0.25, "dy": 0.04 },
        { "op": "move", "feature": "brow_outer", "radius": 0.35, "dy": 0.07 },
        { "op": "move", "feature": "cheek", "radius": 0.60, "dy": 0.08 },
        { "op": "move", "feature": "mouth_corner", "radius": 0.30, "dy": 0.06 },
        { "op": "scale", "feature": "mouth", "radius": 0.55, "sx": 0.95, "sy": 0.80 },
        { "op": "move", "feature": "temple", "radius": 0.60, "dx": -0.06 },
        { "op": "move", "feature": "nose_tip", "radius": 0.45, "dy": 0.06 },
        { "op": "move", "feature": "jaw", "radius": 0.60, "dx": -0.05 }
      ],
      "marks": [
        { "type": "wrinkle", "depth": 0.7, "path": [
          { "feature": "forehead", "dx": -0.42, "dy": 0.20 }, { "feature": "forehead", "dy": 0.17 }, { "feature": "forehead", "dx": 0.42, "dy": 0.20 } ] },
        { "type": "wrinkle", "depth": 0.7, "path": [
          { "feature": "forehead", "dx": -0.44, "dy": 0.08 }, { "feature": "forehead", "dy": 0.05 }, { "feature": "forehead", "dx": 0.44, "dy": 0.08 } ] },
        { "type": "wrinkle", "depth": 0.6, "path": [
          { "feature": "forehead", "dx": -0.42, "dy": -0.04 }, { "feature": "forehead", "dy": -0.07 }, { "feature": "forehead", "dx": 0.42, "dy": -0.04 } ] },
        { "type": "wrinkle", "depth": 0.5, "path": [
          { "feature": "forehead", "dx": -0.38, "dy": -0.16 }, { "feature": "forehead", "dy": -0.19 }, { "feature": "forehead", "dx": 0.38, "dy": -0.16 } ] },
        { "type": "wrinkle", "depth": 0.7, "mirrored": true, "path": [
          { "feature": "eye_outer", "side": "left", "dx": -0.04, "dy": -0.02 }, { "feature": "eye_outer", "side": "left", "dx": -0.18, "dy": -0.08 } ] },
        { "type": "wrinkle", "depth": 0.7, "mirrored": true, "path": [
          { "feature": "eye_outer", "side": "left", "dx": -0.04, "dy": 0.02 }, { "feature": "eye_outer", "side": "left", "dx": -0.19, "dy": 0.03 } ] },
        { "type": "wrinkle", "depth": 0.6, "mirrored": true, "path": [
          { "feature": "eye_outer", "side": "left", "dx": -0.04, "dy": 0.05 }, { "feature": "eye_outer", "side": "left", "dx": -0.16, "dy": 0.14 } ] },
        { "type": "wrinkle", "depth": 0.6, "mirrored": true, "path": [
          { "feature": "eye", "side": "left", "dx": -0.18, "dy": 0.14 }, { "feature": "eye", "side": "left", "dy": 0.20 },
          { "feature": "eye", "side": "left", "dx": 0.15, "dy": 0.13 } ] },
        { "type": "wrinkle", "depth": 1.0, "mirrored": true, "path": [
          { "feature": "nose_base", "side": "left", "dx": -0.24, "dy": -0.06 }, { "feature": "nose_base", "side": "left", "dx": -0.38, "dy": 0.18 },
          { "feature": "mouth_corner", "side": "left", "dx": -0.08, "dy": 0.08 } ] },
        { "type": "wrinkle", "depth": 0.7, "mirrored": true, "path": [
          { "feature": "mouth_corner", "side": "left", "dx": -0.02, "dy": 0.06 }, { "feature": "mouth_corner", "side": "left", "dx": 0.02, "dy": 0.32 } ] },
        { "type": "wrinkle", "depth": 0.6, "mirrored": true, "path": [
          { "feature": "brow_inner", "side": "left", "dx": 0.03 }, { "feature": "brow_inner", "side": "left", "dx": 0.06, "dy": -0.20 } ] },
        { "type": "hair", "mirrored": true, "count": 60, "length": 0.24, "angle": 75, "spread": 25,
          "color": "#F4F1EA", "width": 0.010,
          "from": { "feature": "brow_inner", "side": "left" }, "to": { "feature": "brow_outer", "side": "left" } },
        { "type": "hair", "mirrored": true, "count": 45, "length": 0.22, "angle": 12, "spread": 18,
          "color": "#EFEDE6", "width": 0.010,
          "from": { "feature": "upper_lip", "side": "left", "dy": -0.12 },
          "to": { "feature": "mouth_corner", "side": "left", "dx": 0.02, "dy": -0.10 } }
      ]
    },
    {
      "id": "pickle",
      "name": "Pickle",
      "kanji": "原",
      "accent": "#7A5C3E",
      "grade": { "contrast": 1.10, "saturation": 0.82, "warmth": 0.10 },
      "warp": [
        { "op": "move", "feature": "brow_mid", "radius": 0.45, "dy": 0.12 },
        { "op": "move", "feature": "brow_inner", "radius": 0.30, "dy": 0.05 },
        { "op": "scale", "feature": "forehead", "radius": 1.00, "sx": 1.02, "sy": 0.82 },
        { "op": "scale", "feature": "eye", "radius": 0.38, "sx": 0.86, "sy": 0.74 },
        { "op": "scale", "feature": "nose_base", "radius": 0.55, "sx": 1.40, "sy": 1.15 },
        { "op": "move", "feature": "nose_tip", "radius": 0.40, "dy": 0.05 },
        { "op": "move", "feature": "gonion", "radius": 0.85, "dx": 0.20, "dy": 0.08 },
        { "op": "scale", "feature": "chin", "radius": 0.70, "sx": 1.35, "sy": 1.10 },
        { "op": "move", "feature": "chin", "radius": 0.85, "dy": 0.12 },
        { "op": "scale", "feature": "mouth", "radius": 0.90, "sx": 1.18, "sy": 1.08 },
        { "op": "move", "feature": "neck", "radius": 1.00, "dx": 0.26 }
      ],
      "marks": [
        { "type": "shade", "mirrored": true, "at": { "feature": "eye", "side": "left", "dy": -0.06 },
          "rx": 0.36, "ry": 0.17, "color": "#2B1D14", "alpha": 0.38, "blend": "multiply" },
        { "type": "hair", "mirrored": true, "count": 95, "length": 0.11, "angle": 112, "spread": 40,
          "color": "#2A1D15", "width": 0.012,
          "from": { "feature": "brow_inner", "side": "left" }, "to": { "feature": "brow_outer", "side": "left" } },
        { "type": "wrinkle", "depth": 0.5, "path": [
          { "feature": "forehead", "dx": -0.35, "dy": 0.15 }, { "feature": "forehead", "dy": 0.12 }, { "feature": "forehead", "dx": 0.35, "dy": 0.15 } ] },
        { "type": "shade", "at": { "feature": "nose_base", "dy": -0.02 },
          "rx": 0.32, "ry": 0.12, "color": "#3A2418", "alpha": 0.18, "blend": "multiply" }
      ]
    }
  ]
}
"""
