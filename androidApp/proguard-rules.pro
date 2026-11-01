# R8 rules of the app (ADR 0006). The libraries bring their own consumer rules; this file carries only what they miss.

# Keep the file and line of every frame, so a crash in the release reads with the mapping file of its build.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ML Kit starts its components by reflection: the manifest names each registrar (a `com.google.firebase.components:<class>`
# meta-data: FaceRegistrar, VisionCommonRegistrar, CommonComponentRegistrar) and the library makes it with its default
# constructor. Nobody calls that constructor in code, so R8 in full mode removes it, ML Kit cannot start and the release
# crashes as soon as the camera opens, while the debug build, not minified, works. Seen on 2026-11-01; the same rule
# fixed the same failure in MyLetters 2.0.
-keep class * implements com.google.firebase.components.ComponentRegistrar { void <init>(); }
