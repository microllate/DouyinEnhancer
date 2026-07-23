package io.github.twyora.douyinenhancer.hook.comment

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object CommentImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.PURIFY_COMMENT_IMAGE, false)) {
            if (verbose) {
                YLog.debug("$TAG: purify comment image disabled, skip hook")
            }
            return
        }

        packageInstance.commentImageStruct.selfClass?.resolveMethod(
            packageInstance.commentImageStruct.getDownloadUrl()
        )?.hook {
            before {
                val originUrl = instance.getField<Any?>(
                    packageInstance.commentImageStruct.originUrl()
                )
                if (originUrl != null) {
                    if (verbose) {
                        YLog.debug("$TAG: origin url present, override download url with origin url: $originUrl")
                    }
                    result = originUrl
                }
            }
        } ?: run {
            YLog.warn(
                "$TAG: target method not found, watermark-free comment image download is not active"
            )
        }
    }
}
