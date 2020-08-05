# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build_lib.gradleadle.
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

-optimizationpasses 7
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontoptimize
-verbose
-ignorewarnings
-dontskipnonpubliclibraryclassmembers
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保留java与js交互注解
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*

-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
   static final long serialVersionUID;
   private static final java.io.ObjectStreamField[] serialPersistentFields;
   !static !transient <fields>;
   private void writeObject(java.io.ObjectOutputStream);
   private void readObject(java.io.ObjectInputStream);
   java.lang.Object writeReplace();
   java.lang.Object readResolve();
}

-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R$* {
 *;
}

-keep public class * extends android.app.Activity{
	public <fields>;
	public <methods>;
}
-keep public class * extends android.app.Application{
	public <fields>;
	public <methods>;
}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembers class * {
	public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
	public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclasseswithmembernames class *{
	native <methods>;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembers class * {
    ... *JNI*(...);
}

-keepclasseswithmembernames class * {
	... *JRI*(...);
}

-keep class **JNI* {*;}


-keepattributes InnerClasses,Signature,SourceFile,Exceptions,LineNumberTable,*Annotation*
-keep class  * extends cn.uc.gamesdk.even.SDKEventReceiver {*;}
-keep class  * extends cn.uc.** {*;}
-keep class  * extends cn.gundam.sdk.shell.**{*;}
-keep class  * extends cn.gundam.sdk.shell.even.SDKEventReceiver{*;}
-keep class  * extends com.alipay.** {*;}
-keep class  * extends com.ta.** {*;}
-keep class  * extends com.ut.** {*;}
-keep class  * extends org.json.** {*;}
-keep class  * extends com.baidu.fy.cps.** {*;}
-keep class  * extends com.baidu.bottom.** {*;}
-keep class  * extends com.baidu.mtjstatsdk.** {*;}

-keep class android.** { *; }

-keep class Decoder.** {*;}

-keep class cn.waves.rvds.** {*;}

#-keep class com.tencent.bugly.** {*;}
#-keep class com.asus.** {*;}
#-keep class com.bun.** {*;}
#-keep class com.heytap.** {*;}
#-keep class com.huawei.** {*;}
#-keep class com.meizu.** {*;}
#-keep class com.samsung.** {*;}
#-keep class com.zui.** {*;}
