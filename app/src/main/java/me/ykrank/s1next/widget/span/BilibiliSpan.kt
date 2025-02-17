package me.ykrank.s1next.widget.span

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import com.github.ykrank.androidtools.util.L
import me.ykrank.s1next.widget.span.PostMovementMethod.URLSpanClick

/**
 * <xmp>
 * 符合intent-filter的<a></a> tag。或者<bilibili></bilibili> tag
</xmp> *
 * <br></br>
 * <xmp>
 * eg:[http://www.bilibili.com/video/av6591319/](http://www.bilibili.com/video/av6591319/)
</xmp> *
 * <br></br>
 * <xmp>
 * or <bilibili>http://www.bilibili.com/video/av6591319/</bilibili>
</xmp> *
 * <br></br>
 * Created by ykrank on 2016/10/16 0016.
 */
class BilibiliSpan : URLSpanClick {
    override fun isMatch(uri: Uri): Boolean {
        if ("http".equals(uri.scheme, ignoreCase = true) ||
            "https".equals(uri.scheme, ignoreCase = true)
        ) {
            var path = uri.encodedPath
            if (path == null) {
                path = "/"
            }
            var host = uri.host ?: ""
            if (host.startsWith("www.")) {
                host = host.replaceFirst("www.", "")
            }
            for (filter in HOST_FILTERS) {
                val hostFilter = filter.key
                val pathPattern = filter.value

                if (hostFilter.equals(host, ignoreCase = true)
                    && path.matches(pathPattern.toRegex())
                ) {
                    return true
                }
            }
        }
        return false
    }

    override fun onClick(uri: Uri, view: View) {
        goBilibili(view.context, uri)
    }

    private class BilibiliHref
    private class BilibiliURLSpan(val uRL: String) : ClickableSpan() {

        override fun onClick(widget: View) {
            goBilibili(widget.context, Uri.parse(uRL))
        }
    }

    companion object {
        /**
         * intent-filter.从官方APP AndroidManifest中提取
         */
        private val HOST_FILTERS: Map<String, String> by lazy {
            buildMap {
                put("bilibili.com", ".*")
                put("bilibili.tv", ".*")
                put("bilibili.cn", ".*")
                put("bangumi.bilibili.com", ".*")

                put("bilibili.kankanews.com", "/video/av.*")
                put("bilibili.smgbb.cn", "/video/av.*")
                put("m.acg.tv", "/video/av.*")
            }
        }

        /**
         * See android.text.HtmlToSpannedConverter#startA(android.text.SpannableStringBuilder, org.xml.sax.Attributes)
         */
        @JvmStatic
        fun startBilibiliSpan(text: SpannableStringBuilder) {
            val len = text.length
            text.setSpan(BilibiliHref(), len, len, Spannable.SPAN_MARK_MARK)
        }

        /**
         * See android.text.HtmlToSpannedConverter#endA(android.text.SpannableStringBuilder)
         */
        @JvmStatic
        fun endBilibiliSpan(text: SpannableStringBuilder) {
            val len = text.length
            val obj: Any? = TagHandler.getLastSpan(text, BilibiliHref::class.java)
            val where = text.getSpanStart(obj)
            text.removeSpan(obj)
            if (where < len && obj != null) {
                val hrefChars = CharArray(len - where)
                text.getChars(where, len, hrefChars, 0)
                val href = String(hrefChars)
                text.setSpan(
                    BilibiliURLSpan(href), where, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // 对Bilibili链接进行独立处理，调用Bilibili客户端
        private fun goBilibili(context: Context, uri: Uri) {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setClassName("tv.danmaku.bili", "tv.danmaku.bili.ui.IntentHandlerActivity")
            try {
                val pm = context.packageManager
                val ai = intent.resolveActivityInfo(pm, PackageManager.MATCH_DEFAULT_ONLY)
                if (ai == null) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                } else {
                    context.startActivity(intent)
                }
            } catch (e: Throwable) {
                L.report("BilibiliURLSpan startActivity error for intent, $intent", e)
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }
}
