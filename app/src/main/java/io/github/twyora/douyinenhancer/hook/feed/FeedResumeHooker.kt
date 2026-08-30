package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedResumeHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_BLOCK_RESUME_PLAYBACK, false)) {
            if (verbose) {
                YLog.debug("$TAG: block resume playback is disabled, skipping hook")
            }
            return
        }

        // I have another choice: intercept FeedPanelProxy.handleTextureAvailable,
        // (the name "FeedPanelProxy" is given by me; it is obfuscated by the host and owned by BaseListFragmentPanel.basePanelProxy)
        // instead of checking videoType inside BaseListFragmentPanel.handleVideoEvent.
        // The reason I ultimately chose this strategy is that I cannot confirm whether FeedPanelProxy
        // will be removed or obfuscated into another name. If this functionality breaks,
        // maintaining extra hook points becomes an additional maintenance burden
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleVideoEvent()
        )?.hook {
            after {
                val videoType = args[0]?.getField<Int>(
                    packageInstance.videoEvent.videoType()
                ) ?: run {
                    YLog.error("$TAG: video type is null")
                    return@after
                }

                if (videoType != DouyinPackage.VideoEventModule.EVENT_TEXTURE_AVAILABLE) {
                    return@after
                } else if (verbose) {
                    YLog.debug("$TAG: pause playback on resume")
                }
                instance.invokeMethodOnly(
                    packageInstance.baseListFragmentPanel.pauseCurrentPlayerWithListener()
                )
                instance.invokeMethodOnly(
                    packageInstance.baseListFragmentPanel.showIvWhenPause()
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to intercept resume of video playback on surface created", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for intercepting resume of video playback on surface created", throwable)
            }
        }
    }
}
