# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Kotlin*

# Koin
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
}

# Room / WorkManager (transitive via play-services-ads).
# Room instantiates its generated *_Impl database via reflection; R8 full mode
# strips the no-arg constructor, so WorkManager's WorkDatabase fails to build
# and the androidx.startup initializer crashes the app at launch:
#   "Failed to create an instance of androidx.work.impl.WorkDatabase"
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-dontwarn androidx.work.**
