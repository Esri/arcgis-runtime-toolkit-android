# We need to make sure these classes are available in the main DEX file for API 19
-keep @org.junit.runner.RunWith public class *
-keep class android.support.test.internal** { *; }
-keep class org.junit.** { *; }
-keep public class com.esri.arcgisruntime.toolkit.** { *; }
