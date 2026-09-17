# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\MarianMaier\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Gson rules
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

# Keep data classes that are used for JSON serialization
-keep class com.example.cykluscalk.data.** { *; }
-keep class com.example.cykluscalk.ui.StatsData { *; }
-keep class com.example.cykluscalk.ui.CycleInsights { *; }
-keep class com.example.cykluscalk.ui.BackupData { *; }

# Hilt/Dagger rules (usually handled by the library, but good to have)
-keepattributes *Annotation*
-keepattributes Signature
