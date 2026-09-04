@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.ContextThemeWrapper
import androidx.core.content.edit
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.MiscKey
import io.github.twyora.douyinenhancer.config.key.PlaybackComponentBlockKey
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.setField

class PlaybackComponentBlockDialog(context: Context) : AlertDialog.Builder(ContextThemeWrapper(context, R.style.MainTheme)) {
    class PrefsFragment : PreferenceFragment() {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val prefs = FastKVConfigManager.settings
            preferenceManager.setField(Field("mSharedPreferences"), prefs)
            preferenceManager.setField(Field("mEditor"), null)
            addPreferencesFromResource(R.xml.pref_playback_component_block)

            if (!prefs.getBoolean(MiscKey.ENABLE_HIDDEN_FEATURES, false)) {
                HIDDEN_KEYS.forEach { key ->
                    findPreference(key)?.let {
                        preferenceScreen?.removePreference(it)
                    }
                }
            }
        }
    }

    init {
        val activity = context as Activity

        val prefsFragment = PrefsFragment()
        activity.fragmentManager.beginTransaction().add(prefsFragment, "PlaybackComponentBlock").commit()
        activity.fragmentManager.executePendingTransactions()

        setView(prefsFragment.view)
        setTitle(R.string.playback_component_block_dialog_title)
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok) { _, _ ->
            val prefs = FastKVConfigManager.settings
            if (!prefs.getBoolean(MiscKey.ENABLE_HIDDEN_FEATURES, false)) {
                HIDDEN_KEYS.forEach { key ->
                    prefs.edit(true) {
                        putBoolean(key, false)
                    }
                }
            }
        }
        setOnDismissListener {
            activity.fragmentManager.beginTransaction().remove(prefsFragment).commitAllowingStateLoss()
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        private val HIDDEN_KEYS = listOf(
            PlaybackComponentBlockKey.BUTTON_UNFOLLOW_FAMILIAR_REC,
            PlaybackComponentBlockKey.NEARBY_HOT_COMMENT,
            PlaybackComponentBlockKey.ECOM_STORE,
            PlaybackComponentBlockKey.ECOM_TAG_FRIEND,
            PlaybackComponentBlockKey.JX_PICK,
            PlaybackComponentBlockKey.FLOW,
            PlaybackComponentBlockKey.JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG,
            PlaybackComponentBlockKey.SOCIAL_NEW_COMMENT_GUIDE_BUBBLE,
            PlaybackComponentBlockKey.AI_CO_CREATORS_THREE,
            PlaybackComponentBlockKey.AIGC_COCREATE_STATUS_TITLE
        )

        fun show(context: Context) {
            runCatching {
                (context as? Activity)?.injectModuleAppResources()
                PlaybackComponentBlockDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: failed to show playback component block dialog", it)
            }
        }
    }
}
