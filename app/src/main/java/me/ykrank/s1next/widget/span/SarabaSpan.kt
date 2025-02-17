package me.ykrank.s1next.widget.span

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import com.github.ykrank.androidtools.util.L
import me.ykrank.s1next.view.page.post.postlist.PostListGatewayActivity
import me.ykrank.s1next.widget.span.PostMovementMethod.URLSpanClick

/**
 * <xmp>
 * 符合intent-filter的<a></a> tag。
</xmp> *
 * <br></br>
 * <xmp>
 * eg:[http://bbs.saraba1st.com/2b/forum-75-1.html](http://bbs.saraba1st.com/2b/forum-75-1.html)
</xmp> *
 * <br></br>
 * Created by ykrank on 2016/10/16 0016.
 */
open class SarabaSpan : URLSpanClick {
    override fun isMatch(uri: Uri): Boolean {
        if ("http".equals(uri.scheme, ignoreCase = true) ||
            "https".equals(uri.scheme, ignoreCase = true)
        ) {
            var path = uri.encodedPath
            if (path == null) {
                path = "/"
            }
            val host = uri.host ?: ""
            for (filter in HOST_FILTERS) {
                val hostFilter = filter.key
                val pathPattern = filter.value

                if ((host == hostFilter || host.endsWith(".${hostFilter}"))
                    && path.matches(pathPattern)
                ) {
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(uri: Uri, view: View) {
        goSaraba(view.context, uri)
    }

    companion object {
        /**
         * intent-filter.从AndroidManifest中提取
         */
        private val HOST_FILTERS: Map<String, Regex> by lazy {
            buildMap {
                put("saraba1st.com", ".*".toRegex())
                put("stage1.cc", ".*".toRegex())
                put("stage1st.com", ".*".toRegex())
            }
        }

        // 对Saraba链接进行独立处理，调用Saraba客户端
        @JvmStatic
        protected fun goSaraba(context: Context, uri: Uri?) {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setClass(context, PostListGatewayActivity::class.java)
            try {
                val pm = context.packageManager
                val ai = intent.resolveActivityInfo(pm, PackageManager.MATCH_DEFAULT_ONLY)
                if (ai == null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } else {
                    context.startActivity(intent)
                }
            } catch (e: Throwable) {
                L.report("SarabaURLSpan startActivity error for intent, $intent", e)
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }
}
