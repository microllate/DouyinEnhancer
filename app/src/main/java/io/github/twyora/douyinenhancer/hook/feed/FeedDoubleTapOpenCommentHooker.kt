
package io.github.twyora.douyinenhancer.hook.feed

import android.view.View
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod
import com.freegang.extension.forEachChild

@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {

    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(
            ModuleKey.DISABLE_VERBOSE_LOGS,
            false
        )

    override fun onHook() {

        if (!FastKVConfigManager.settings.getBoolean(
                FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT,
                false
            )
        ) {
            if (verbose) {
                YLog.debug("$TAG: double-tap open comment is disabled")
            }
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.hook {

            after {

                val panel = instance ?: return@after

                // 找当前 BaseListFragmentPanel 下的 VideoViewHolderRootView
                val rootView = findVideoViewHolderRootView(panel)

                if (rootView == null) {
                    if (verbose) {
                        YLog.debug("$TAG: VideoViewHolderRootView not found")
                    }
                    return@after
                }

                clickCommentView(rootView)
            }

        }?.result {

            onConductFailure { _, throwable ->
                YLog.error(
                    "$TAG: failed to open comment after double-tap",
                    throwable
                )
            }

            onHookingFailure { throwable ->
                YLog.error(
                    "$TAG: failed to hook handleDoubleClick",
                    throwable
                )
            }
        }
    }

    private fun findVideoViewHolderRootView(parent: Any): View? {

        // BaseListFragmentPanel 本身可能不是 View，
        // 尝试从字段中寻找 View
        val fields = parent.javaClass.declaredFields

        for (field in fields) {
            try {
                field.isAccessible = true
                val value = field.get(parent)

                if (value is View) {
                    val result = findVideoViewHolderRootView(value)
                    if (result != null) {
                        return result
                    }
                }
            } catch (_: Throwable) {
            }
        }

        return null
    }

    private fun findVideoViewHolderRootView(view: View): View? {

        if (view.javaClass.name ==
            "com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView"
        ) {
            return view
        }

        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val result = findVideoViewHolderRootView(view.getChildAt(i))
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    private fun clickCommentView(parent: View) {

        parent.forEachChild {

            val content = "${it.contentDescription}"
            val text = if (it is TextView) "${it.text}" else ""

            val isComment =
                content.contains("评论") ||
                text.contains("评论")

            if (!isComment) {
                return@forEachChild
            }

            if (!it.isClickable) {
                return@forEachChild
            }

            if (verbose) {
                YLog.debug(
                    "$TAG: found comment view: $it"
                )
            }

            it.performClick()

            return@forEachChild
        }
    }
}

