package io.github.twyora.douyinenhancer.hook.feed

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
object FeedLoopHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    // what a magic number, hope it can remain stable on other versions
    private const val EVENT_PLAY_COMPLETED = 7

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_BLOCK_AUTO_REPLAY, false)) {
            if (verbose) {
                YLog.debug("$TAG: block auto replay is disabled, skipping hook")
            }
            return
        }

        // BaseListFragmentPanel has no member function onPlayCompleted,
        // but onVideoPlayerEvent will be called when video playback completes with a specific video event code.
        // I have no other good idea, pause the video manually with a magic number checking
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.onVideoPlayerEvent()
        )?.hook {
            after {
                val status = args[0] ?: run {
                    YLog.error("$TAG: video player event is null")
                    return@after
                }
                val code = status.getField<Int>(
                    packageInstance.videoPlayerEvent.code()
                ) ?: run {
                    YLog.error("$TAG: video player event code is null")
                    return@after
                }

                if (code != EVENT_PLAY_COMPLETED) {
                    return@after
                } else if (verbose) {
                    YLog.debug("$TAG: pause when feed playback completes")
                }
                instance.invokeMethod<Any>(
                    packageInstance.baseListFragmentPanel.pauseCurrentPlayerWithListener()
                )
                instance.invokeMethod<Any>(
                    packageInstance.baseListFragmentPanel.showIvWhenPause()
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to intercept feed auto-loop on playback completion", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook feed auto-loop on playback completion", throwable)
            }
        }
    }
}