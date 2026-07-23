package io.github.twyora.douyinenhancer.hook.feed

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedDownloadModuleHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    override fun onHook() {
        packageInstance.lppDownloadModule.selfClass?.resolveMethod(
            packageInstance.lppDownloadModule.getVisibility()
        )?.hook {
            after {
                val visibility = result as? Int ?: return@after
                if (visibility == View.VISIBLE) {
                    return@after
                }

                // TODO: Kinda tired of staring at this thing, and honestly
                // the author doesn't really need to download restricted content that often,
                // so let's just pick some low-hanging fruit first —
                // this one can wait till next time, hehe (^///^)
                YLog.warn(
                    "$TAG: feed download button is hidden by the host and the hook logic hasn't been implemented yet — the download button will remain hidden!"
                )
            }
        }
    }
}
