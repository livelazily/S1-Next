# 将.class信息中的类名重新定义为"SourceFile"字符串
-renamesourcefileattribute SourceFile
# 并保留源文件名为"Proguard"字符串，而非原始的类名 并保留行号 // blog from sodino.com
-keepattributes SourceFile,LineNumberTable

# Keep fragment name
-keepnames class me.ykrank.s1next.** implements androidx.fragment.app.Fragment
-keepnames class me.ykrank.s1next.** implements android.app.Fragment

# Jackson Model
-keep public class me.ykrank.s1next.data.api.model.** { *; }
-keep public class me.ykrank.s1next.data.api.app.model.** { *; }
-keep public class me.ykrank.s1next.data.cache.** { *; }
-keep public class me.ykrank.s1next.data.db.dbmodel.ReadProgress { *; }
-keep public class me.ykrank.s1next.widget.uploadimg.model.** { *; }
# GreenDao model
-keep public class me.ykrank.s1next.data.db.dbmodel.** { *; }

# OkDownload
# okhttp https://github.com/square/okhttp/#proguard
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# okdownload:okhttp
-keepnames class com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection

# okdownload:sqlite
-keep class com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite {
        public com.liulishuo.okdownload.core.breakpoint.DownloadStore createRemitSelf();
        public com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite(android.content.Context);
}