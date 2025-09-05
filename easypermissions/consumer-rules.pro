#---------------------------------easypermissions----------------------------------
-keeppackagenames com.zorro.easy.permissions.**
-keep interface com.zorro.easy.permissions.intent.MviIntent{*;}
-keep interface com.zorro.easy.permissions.model.MviEffect{*;}
-keep interface com.zorro.easy.permissions.model.MviState{*;}
-keep class com.zorro.easy.permissions.model.PermissionEvent{*;}
-keep class com.zorro.easy.permissions.viewmodel.PermissionViewModel{*;}
-keep class com.zorro.easy.permissions.viewmodel.ViewModelFactory{*;}
-keep class com.zorro.easy.permissions.PermissionGroups{*;}
-keep class com.zorro.easy.permissions.PermissionSupportRegistry{*;}
-keep class com.zorro.easy.permissions.PermissionRequester{*;}
#---------------------------------General----------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#kotilon
-keep class kotlin.coroutines.Continuation
#Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.fragment.app.DialogFragment
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public <init>();
}
-keepclassmembers class * extends androidx.fragment.app.DialogFragment {
    public <init>();
}
-keepclassmembers class ** {
    public static *** Companion;
}
-keepclassmembers class **$Companion {
    *;
}
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class ** {
    public <init>(...);
    public void set*(***);
    public *** get*();
    public *** component*(...);
    public *** copy(...);
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,EnclosingMethod,AnnotationDefault,RuntimeVisibleAnnotations
#----------------------------------------------------------------------------