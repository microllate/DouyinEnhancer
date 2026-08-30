package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.VerifyDialog
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object VerifyDialogHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (BuildConfig.DEBUG) {
            if (verbose) {
                YLog.debug("$TAG: debug build, skipping verification check")
            }
            return
        }

        packageInstance.mainActivity.selfClass?.resolveMethod(
            packageInstance.mainActivity.onResume()
        )?.hook {
            after {
                val activity = instance as? Activity ?: run {
                    YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                    return@after
                }

                val lastVerifiedVersion = FastKVConfigManager.module.getInt(
                    ModuleKey.LAST_VERIFIED_VERSION,
                    0
                )
                if (BuildConfig.VERSION_CODE != lastVerifiedVersion) {
                    VerifyDialog.show(activity)
                }
                removeSelf {
                    if (verbose) {
                        YLog.debug("$TAG: verification check hook removed to prevent duplicate check")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to show verify dialog on resume", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for showing verify dialog on resume", throwable)
            }
        }
    }
}
