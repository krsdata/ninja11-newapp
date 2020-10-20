# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-keep class com.address_package.** { *; }
#
#-keep class com.squareup.okhttp.** { *; }
#-keep interface com.squareup.okhttp.** { *; }
#
#-dontwarn com.squareup.okhttp.**
#-dontwarn okio.**
#
#-keepattributes Signature
#-keepattributes *Annotation*
#-keep class okhttp3.** { *; }
#-keep interface okhttp3.** { *; }
#
#-dontwarn okhttp3.**
#
#-keep class com.google.** { *; }
#-dontwarn com.google.**
#
#-keep class com.crashlytics.** { *; }
#-dontwarn com.crashlytics.**
#
#-keepattributes *Annotation*
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile
#-keep public class * extends java.lang.Exception
#
### razor pay proguard rules start here 06-09-20 ==========================
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
#
#-keepattributes JavascriptInterface
#-keepattributes *Annotation*
#
#-dontwarn com.razorpay.**
#-keep class com.razorpay.** {*;}
#
#-optimizations !method/inlining/*
#
#-keepclasseswithmembers class * {
#  public void onPayment*(...);
#}
### razor pay proguard rules end here 06-09-20 ==========================
#
#
###---------------Begin: proguard configuration common for all Android apps ----------
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontskipnonpubliclibraryclassmembers
#-dontpreverify
#-verbose
#-dump class_files.txt
#-printseeds seeds.txt
#-printusage unused.txt
#-printmapping mapping.txt
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
