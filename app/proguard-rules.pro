# Qlic-specific keep rules are added only when a dependency requires them.
-keepattributes Signature,*Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
