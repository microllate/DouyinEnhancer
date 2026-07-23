package io.github.twyora.douyinenhancer.hook.settings

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.SettingsDialog
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object SettingsHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethod(
            packageInstance.douYinSettingNewVersionActivity.onResume()
        )?.hook {
            after {
                val moduleSettingsTag = "dyenhancer_settings"

                val activity = instance as? Activity ?: return@after

                val settingsScrollView = instance.getField<ViewGroup?>(
                    packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
                ) ?: run {
                    YLog.error("$TAG: settings scroll view field not found, cannot inject module entry")
                    return@after
                }

                if (settingsScrollView.findViewWithTag<View>(moduleSettingsTag) != null) {
                    if (verbose) {
                        YLog.debug("$TAG: module settings entry already injected, skipping")
                    }
                    return@after
                }

                val moduleSettingsCommonItemView =
                    packageInstance.commonItemView.selfClass?.getConstructor(Context::class.java)
                        ?.newInstance(instance) as? ViewGroup
                if (moduleSettingsCommonItemView == null) {
                    YLog.error("$TAG: failed to create module settings entry")
                    return@after
                }

                moduleSettingsCommonItemView.tag = moduleSettingsTag
                moduleSettingsCommonItemView.id = View.generateViewId()

                // Ensure the host app can find the module settings icon
                activity.injectModuleAppResources()
                moduleSettingsCommonItemView.invokeMethod<Unit>(
                    packageInstance.commonItemView.setLeftTextAndIcon(),
                    moduleAppResources.getString(R.string.app_name),
                    R.drawable.ic_module_settings
                )
                moduleSettingsCommonItemView.invokeMethod<Unit>(
                    packageInstance.commonItemView.setRightUIMode(),
                    0 // arrow mode for the right UI element
                )
                moduleSettingsCommonItemView.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                moduleSettingsCommonItemView.setOnClickListener {
                    SettingsDialog.show(activity)
                }

                // Prefer inserting above the logout button; fallback to direct insert
                val targetParent = settingsScrollView
                    .findViewWithTag<View?>("logout")
                    ?.parent as? ViewGroup
                    ?: (settingsScrollView.getChildAt(0) as? ViewGroup)
                    ?: run {
                        YLog.error("$TAG: unable to find a suitable parent for module settings entry")
                        return@after
                    }

                if (verbose) {
                    YLog.debug("$TAG: module settings entry prepared, adding view to settings")
                }

                targetParent.addView(
                    moduleSettingsCommonItemView,
                    0
                )
            }
        }
    }
}
