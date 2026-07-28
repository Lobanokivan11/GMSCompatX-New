# disable obfuscation
-dontobfuscate

# keep our module entrypoint
-keep public class net.sb418.android.gmscompatx.XposedModule { public *; }

# keep our non-namespaced public classes + members
-keep public class android.app.compat.gms.** { public *; }
-keep public class com.android.internal.gmscompat.** { public *; }

# guava: ignore missing annotation classes (not used at runtime)
-dontwarn com.google.j2objc.annotations.**
