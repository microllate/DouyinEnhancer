package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap open-comment disabled, skip hook")
            }
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.hook {
            after {
                val aweme = instance.invokeMethod<Any>(
                    packageInstance.baseListFragmentPanel.getCurrentAweme()
                ) ?: run {
                    YLog.error("$TAG: unable to get current aweme")
                    return@after
                }

                val openCommentPanelEvent = packageInstance.videoEvent.selfClass?.createInstance(
                    7,
                    aweme
                ) ?: run {
                    YLog.error("$TAG: unable to build open-comment-panel event")
                    return@after
                }

                if (verbose) {
                    val awemeId = aweme.getField<String>(
                        packageInstance.aweme.aid()
                    )
                    YLog.debug("$TAG: dispatching open-comment-panel event for current aweme, aid: $awemeId")
                }

                instance.invokeMethod<Any>(
                    packageInstance.baseListFragmentPanel.handleVideoEvent(),
                    openCommentPanelEvent
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: error while dispatching open-comment-panel event for current aweme", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, open-comment-panel event will not be forwarded")
            }
        }
    }
}
