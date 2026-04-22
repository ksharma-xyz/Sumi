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
