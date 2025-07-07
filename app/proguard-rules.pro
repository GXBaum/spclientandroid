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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



# Keep data classes used by Moshi for serialization.
# Replace 'de.rafaelbeckmann.hvkclient.data.model.**' with the actual package
# where your data model classes are located.
-keep class de.rafaelbeckmann.hvkclient.data.model.** { *; }
-keepnames class de.rafaelbeckmann.hvkclient.data.model.**

# Keep any class that is annotated with @JsonClass and its constructor.
# This is for Moshi's reflection-based adapter.
-keep @com.squareup.moshi.JsonClass class * {
    <init>(...);
}