# Add project specific Proguard rules here.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.example.data.** { *; }
-dontwarn okio.**
-dontwarn com.squareup.okhttp3.**
