// Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/MainActivity.kt)

@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.widget.Toast
import com.highcapable.yukihookapi.YukiHookAPI
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(
            android.R.id.content,
            PrefsFragment()
        ).commit()
    }

    class PrefsFragment :
        PreferenceFragment(),
        Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.main_activity)

            findPreference("open_module_settings")?.onPreferenceClickListener = this
            (findPreference("hide_launcher_icon") as? SwitchPreference)?.let {
                val aliasName = ComponentName(activity, MainActivity::class.java.name + "Alias")
                val isHidden = activity.packageManager.getComponentEnabledSetting(aliasName) ==
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                it.isChecked = isHidden
            }
            findPreference("hide_launcher_icon")?.onPreferenceChangeListener = this
            findPreference("version")?.summary = BuildConfig.VERSION_NAME
            findPreference("build_time")?.summary =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(BuildConfig.BUILD_TIMESTAMP)
            if (YukiHookAPI.Status.isModuleActive) {
                val activationStatus = findPreference("activation_status")
                activationStatus?.title = context.getString(R.string.pref_about_activation_status_enabled_title)
                activationStatus?.summary = context.getString(R.string.pref_about_activation_status_activated_summary)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean = when (preference.key) {
            "hide_launcher_icon" -> {
                val shouldHide = newValue as Boolean
                val status = if (shouldHide) {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                }

                val aliasName = ComponentName(activity, MainActivity::class.java.name + "Alias")
                val packageManager = activity.packageManager
                if (packageManager.getComponentEnabledSetting(aliasName) != status) {
                    packageManager.setComponentEnabledSetting(
                        aliasName,
                        status,
                        PackageManager.DONT_KILL_APP
                    )
                }

                true
            }

            else -> false
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference): Boolean {
            return when (preference.key) {
                "open_module_settings" -> {
                    if (!YukiHookAPI.Status.isModuleActive) {
                        Toast.makeText(
                            activity,
                            activity.getString(
                                R.string.pref_about_activation_status_deactivated_summary
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return true
                    }

                    activity.packageManager.getLaunchIntentForPackage(
                        "com.ss.android.ugc.aweme"
                    )?.run {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("douyinenhancer_start_settings", true)
                        activity.startActivity(this)
                    }
                    true
                }

                else -> false
            }
        }
    }
}
