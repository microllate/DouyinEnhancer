package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedDoubleTapDiggHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: disabling double-tap digg")
                }
                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: error while disabling double-tap digg", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, double-tap action cannot be intercepted, double-tap digg may stay enabled")
            }
        }
    }
}