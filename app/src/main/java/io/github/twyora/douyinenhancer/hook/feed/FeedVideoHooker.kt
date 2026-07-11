package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedVideoHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_VIDEO_REMOVE_WATERMARK, false)) {
            return
        }

        val packageInstance = DouyinPackage.instance

        packageInstance.miscDownloadAddrUtil.selfClass?.resolveMethod(
            packageInstance.miscDownloadAddrUtil.getSuffixSceneDownloadAddr()
        )?.hook {
            before {
                val aweme = args[0] ?: return@before

                val playAddr = aweme.invokeMethod<Any>(
                    packageInstance.aweme.getVideo()
                )?.invokeMethod<Any>(
                    packageInstance.video.getPlayAddr()
                )
                if (playAddr != null) {
                    result = playAddr
                }
            }
        }
    }
}
