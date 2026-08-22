package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
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

    private const val PLAY_COMPLETED_EVENT = 7

    override fun onHook() {
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

                if (code != PLAY_COMPLETED_EVENT) {
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