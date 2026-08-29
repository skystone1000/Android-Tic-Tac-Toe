# R8 keep rules for XOXO.
#
# Applied on top of proguard-android-optimize.txt (see app/build.gradle). Compose, Coroutines,
# Lifecycle, Navigation, DataStore and Room all ship their own consumer rules, so only the things
# R8 cannot infer from the bytecode are listed here.
#
# See docs/RELEASE-CHECKLIST.md §1.

# --- Crash symbolication -------------------------------------------------------------------
# Keep line numbers so Play Console stack traces are readable once mapping.txt is uploaded,
# but still rename the source file so class names are not leaked back through it.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room ----------------------------------------------------------------------------------
# MatchEntity columns and the generated DAO/database implementations are resolved by name at
# runtime, so the whole stats package is kept rather than relying on inference.
-keep class com.skystone1000.xoxo.data.stats.** { *; }

# --- Kotlin enums ---------------------------------------------------------------------------
# Difficulty and GameMode are persisted by name in DataStore and passed as Navigation route
# arguments, so valueOf() must still resolve after obfuscation. values()/valueOf() are also
# generated reflectively and are otherwise invisible to R8.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
