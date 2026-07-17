package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.content.Intent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.SettingsDialog
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object MainActivityHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        val packageInstance = DouyinPackage.instance

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
                    activity.intent?.removeExtra("douyinenhancer_start_settings")
                    SettingsDialog.show(activity)
                    removeSelf()
                }
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
                    intent.removeExtra("douyinenhancer_start_settings")
                    SettingsDialog.show(activity)
                }
            }
        }
    }
}