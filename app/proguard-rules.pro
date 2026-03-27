# Keep Xposed module classes
-keep class io.github.lostmymind.cwm.nightplus.MainHook { *; }
-keep class io.github.lostmymind.cwm.nightplus.MainActivity { *; }
-keep class io.github.lostmymind.cwm.nightplus.MainHook$* { *; }

# Keep libxposed service classes
-keep class io.github.libxposed.service.** { *; }
-keep interface io.github.libxposed.service.** { *; }

# Keep Android components
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Application { *; }