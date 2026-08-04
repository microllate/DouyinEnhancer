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
import io.github.twyora.douyinenhancer.utils.setField

@HookOnMainProcess
object ListenAwemeFeedHooker : YukiBaseHooker() {
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
        installForceListenAwemeFeedItemListStatusOkHook()
    }

    private fun installBypassListenAwemeFilterHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.listenAwemeFilter.selfClass?.resolveMethod(
            packageInstance.listenAwemeFilter.accept()
        )?.hook {
            before {
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

    private fun installForceListenAwemeFeedItemListStatusOkHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.feedItemList.selfClass?.resolveMethod(
            packageInstance.feedItemList.getStatusCodeP()
        )?.hook {
            before {
                val statusCode = instance.getField<Int>(
                    packageInstance.feedItemList.statusCode()
                )
                if (statusCode == 0) {
                    // I don't know why statusCode being 0 represents an invalid status
                    if (verbose) {
                        YLog.debug("$TAG: feed item list status invalid ($statusCode), marking valid to let listen aweme feed load")
                    }
                    instance.setField(
                        packageInstance.feedItemList.statusCode(),
                        1
                    )
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to fix feed item list status, listen aweme feed may fail to load", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, feed item list status cannot be fixed, listen aweme feed may fail to load")
            }
        }
}
