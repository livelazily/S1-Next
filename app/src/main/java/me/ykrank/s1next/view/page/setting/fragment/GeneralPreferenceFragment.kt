package me.ykrank.s1next.view.page.setting.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import com.github.ykrank.androidautodispose.AndroidRxDispose
import com.github.ykrank.androidlifecycle.event.FragmentEvent
import com.github.ykrank.androidtools.util.L
import com.github.ykrank.androidtools.util.ResourceUtil
import com.github.ykrank.androidtools.util.RxJavaUtil
import com.github.ykrank.androidtools.widget.RxBus
import com.github.ykrank.androidtools.widget.track.DataTrackAgent
import com.github.ykrank.androidtools.widget.track.event.ThemeChangeTrackEvent
import io.reactivex.Single
import io.reactivex.functions.Consumer
import me.ykrank.s1next.App
import me.ykrank.s1next.R
import me.ykrank.s1next.data.pref.GeneralPreferencesManager
import me.ykrank.s1next.data.pref.ThemeManager
import me.ykrank.s1next.util.AppDeviceUtil
import me.ykrank.s1next.util.AppUpdate
import me.ykrank.s1next.util.BuglyUtils
import me.ykrank.s1next.view.event.FontSizeChangeEvent
import me.ykrank.s1next.view.event.ThemeChangeEvent
import me.ykrank.s1next.view.page.setting.SettingsActivity
import me.ykrank.s1next.widget.span.HtmlCompat
import me.ykrank.s1next.widget.span.HtmlCompat.FROM_HTML_MODE_LEGACY
import javax.inject.Inject

/**
 * An Activity includes general settings that allow users
 * to modify general features and behaviors such as theme
 * and font size.
 */
class GeneralPreferenceFragment : BasePreferenceFragment(), Preference.OnPreferenceClickListener {

    @Inject
    internal lateinit var mRxBus: RxBus

    @Inject
    internal lateinit var mGeneralPreferencesManager: GeneralPreferencesManager

    @Inject
    internal lateinit var mThemeManager: ThemeManager

    @Inject
    internal lateinit var trackAgent: DataTrackAgent

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        App.appComponent.inject(this)
        addPreferencesFromResource(R.xml.preference_general)

        findPreference<Preference>(getString(R.string.pref_key_downloads))?.onPreferenceClickListener = this
        findPreference<Preference>(getString(R.string.pref_key_blacklists))?.onPreferenceClickListener = this
        findPreference<Preference>(getString(R.string.pref_key_post_read))?.onPreferenceClickListener = this
        findPreference<Preference>(getString(R.string.pref_key_backup))?.onPreferenceClickListener = this
        findPreference<Preference>(getString(R.string.pref_key_network))?.onPreferenceClickListener = this

        if (BuglyUtils.isPlay) {
            findPreference<Preference>(getString(R.string.pref_key_check_update))?.isVisible = false
        } else {
            findPreference<Preference>(getString(R.string.pref_key_check_update))?.onPreferenceClickListener = this
        }

        Single.fromCallable { HtmlCompat.fromHtml(AppDeviceUtil.getSignature(requireContext()), FROM_HTML_MODE_LEGACY) }
            .compose(RxJavaUtil.computationSingleTransformer())
            .to(AndroidRxDispose.withSingle(this, FragmentEvent.DESTROY))
            .subscribe(Consumer { findPreference<Preference>(getString(R.string.pref_key_signature))?.summary = it })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (!isAdded) {
            return
        }
        if (key == getString(R.string.pref_key_theme) || key == getString(R.string.pref_key_dark_theme)) {
            trackAgent.post(ThemeChangeTrackEvent(false))
            mThemeManager.invalidateTheme()
            mRxBus.post(ThemeChangeEvent())
        } else if (key == getString(R.string.pref_key_font_size)) {
            L.l("Setting")
            // change scaling factor for fonts
            ResourceUtil.setScaledDensity(
                activity,
                mGeneralPreferencesManager.fontScale
            )
            mRxBus.post(FontSizeChangeEvent())
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val key = preference.key ?: return false
        when (key) {
            getString(R.string.pref_key_downloads) -> {
                SettingsActivity.startDownloadSettingsActivity(preference.context)
                return true
            }
            getString(R.string.pref_key_blacklists) -> {
                SettingsActivity.startBlackListSettingsActivity(preference.context)
                return true
            }
            getString(R.string.pref_key_post_read) -> {
                SettingsActivity.startReadProgressSettingsActivity(preference.context)
                return true
            }
            getString(R.string.pref_key_backup) -> {
                SettingsActivity.startBackupSettingsActivity(preference.context)
                return true
            }
            getString(R.string.pref_key_network) -> {
                SettingsActivity.startNetworkSettingsActivity(preference.context)
                return true
            }
            getString(R.string.pref_key_check_update) -> {
                AppUpdate.checkUpdate(preference.context)
                return true
            }
            else -> return false
        }

    }
}
