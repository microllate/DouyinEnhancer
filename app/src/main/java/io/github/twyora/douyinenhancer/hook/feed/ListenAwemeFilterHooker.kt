package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object ListenAwemeFilterHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.BYPASS_LISTEN_AWEME_RESTRICTION, false)) {
            if (verbose) {
                YLog.debug("$TAG: bypass listen aweme restriction disabled, skip listen aweme hooks")
            }
            return
        }
        installBypassListenAwemeFilterHook()
    }

    private fun installBypassListenAwemeFilterHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.listenAwemeFilter.selfClass?.resolveMethod(
            packageInstance.listenAwemeFilter.accept()
        )?.hook {
            after {
                if (result == true) {
                    return@after
                }

                if (verbose) {
                    val aweme = args[0]
                    val awemeId = aweme?.getField<String>(
                        packageInstance.aweme.aid()
                    )
                    YLog.debug("$TAG: bypassing listen aweme filter for aweme id: $awemeId")
                }
                resultTrue()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to bypass listen aweme filter, some aweme may be filtered out", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, listen aweme filter cannot be bypassed, some aweme may be filtered out")
            }
        }
}
