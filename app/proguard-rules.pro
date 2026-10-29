# Proguard rules for MediRemind
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
