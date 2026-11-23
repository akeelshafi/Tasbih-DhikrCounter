# ---- Tasbih release (R8) rules ----

# Readable crash reports in Play Console (upload the mapping file that the AAB already contains)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization — the type-safe navigation routes (TodayRoute, ProgressRoute, SettingsRoute) are @Serializable
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.tasbih.app.**$$serializer { *; }
-keepclassmembers class com.tasbih.app.** { *** Companion; }
-keepclasseswithmembers class com.tasbih.app.** { kotlinx.serialization.KSerializer serializer(...); }
-keep class com.tasbih.app.ui.TodayRoute { *; }
-keep class com.tasbih.app.ui.ProgressRoute { *; }
-keep class com.tasbih.app.ui.SettingsRoute { *; }

# Room — the entity and generated database/DAO implementations
-keep class com.tasbih.app.data.DailyDhikr { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
