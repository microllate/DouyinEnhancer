package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.content.Intent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.SettingsDialog
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object SettingsEntryIntentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        packageInstance.mainActivity.selfClass?.resolveMethod(
            packageInstance.mainActivity.onResume()
        )?.hook {
            before {
                val activity = instance as? Activity ?: return@before

                val shouldStartSettings = activity.intent?.getBooleanExtra(
                    "douyinenhancer_start_settings",
                    false
                )
                if (shouldStartSettings == true) {
                    if (verbose) {
                        YLog.debug("$TAG: start-settings flag detected in onResume intent, showing settings dialog")
                    }
                    activity.intent?.removeExtra("douyinenhancer_start_settings")
                    SettingsDialog.show(activity)
                    removeSelf {
                        if (verbose) {
                            YLog.debug("$TAG: settings dialog shown, unregistering onResume hook to prevent re-show")
                        }
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to show settings dialog on resume", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for showing settings dialog on resume", throwable)
            }
        }

        packageInstance.mainActivity.selfClass?.resolveMethod(
            packageInstance.mainActivity.onNewIntent()
        )?.hook {
            before {
                val activity = instance as? Activity ?: return@before
                val intent = args[0] as? Intent ?: return@before

                val shouldStartSettings = intent.getBooleanExtra(
                    "douyinenhancer_start_settings",
                    false
                )
                if (shouldStartSettings) {
                    if (verbose) {
                        YLog.debug("$TAG: start-settings flag detected in onNewIntent, showing settings dialog")
                    }
                    intent.removeExtra("douyinenhancer_start_settings")
                    SettingsDialog.show(activity)
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to show settings dialog on new intent", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for showing settings dialog on new intent", throwable)
            }
        }
    }
}
