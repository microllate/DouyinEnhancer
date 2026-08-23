package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedResumeHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    private const val EVENT_SURFACE_AVAILABLE = 0

    override fun onHook() {
        // I have another choice: intercept FeedPanelProxy.handleTextureAvailable,
        // (the name "FeedPanelProxy" is given by me; it is obfuscated by the host and owned by BaseListFragmentPanel.basePanelProxy)
        // instead of checking videoType using a magic number inside BaseListFragmentPanel.handleVideoEvent.
        // The reason I ultimately chose the magic number checking strategy is that I cannot confirm whether FeedPanelProxy
        // will be removed or obfuscated into another name. If this functionality breaks,
        // maintaining extra hook points becomes an additional maintenance burden
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleVideoEvent()
        )?.hook {
            before {
                val videoType = args[0]?.getField<Int>(
                    packageInstance.videoEvent.videoType()
                ) ?: run {
                    YLog.error("$TAG: videoEvent is null")
                    return@before
                }

                if (videoType != EVENT_SURFACE_AVAILABLE) {
                    return@before
                } else if (verbose) {
                    YLog.debug("$TAG: intercepting resume of video playback on surface created")
                }
                resultNull()
            }
        }
    }
}