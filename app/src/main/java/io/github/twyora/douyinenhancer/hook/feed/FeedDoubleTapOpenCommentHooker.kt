package io.github.twyora.douyinenhancer.hook.feed

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod

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

                // 从 BaseListFragmentPanel 中寻找当前视频的 View
                val rootView = findVideoViewHolderRootView(panel)

                if (rootView == null) {
                    if (verbose) {
                        YLog.debug(
                            "$TAG: VideoViewHolderRootView not found"
                        )
                    }
                    return@after
                }

                if (verbose) {
                    YLog.debug(
                        "$TAG: VideoViewHolderRootView found: $rootView"
                    )
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

    /**
     * 从 BaseListFragmentPanel 的字段中寻找 View，
     * 再递归寻找 VideoViewHolderRootView。
     */
    private fun findVideoViewHolderRootView(parent: Any): View? {

        var currentClass: Class<*>? = parent.javaClass

        while (currentClass != null) {

            val fields = currentClass.declaredFields

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
                    // 某些字段可能无法访问，直接跳过
                }
            }

            currentClass = currentClass.superclass
        }

        return null
    }

    /**
     * 在 View 树中寻找 VideoViewHolderRootView。
     */
    private fun findVideoViewHolderRootView(view: View): View? {

        if (view.javaClass.name ==
            "com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView"
        ) {
            return view
        }

        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                val child = view.getChildAt(i)

                val result = findVideoViewHolderRootView(child)

                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    /**
     * 寻找评论按钮并点击。
     */
    private fun clickCommentView(parent: View) {

        if (findAndClickCommentView(parent)) {

            if (verbose) {
                YLog.debug(
                    "$TAG: comment view clicked successfully"
                )
            }

        } else {

            if (verbose) {
                YLog.debug(
                    "$TAG: comment view not found"
                )
            }
        }
    }

    /**
     * 递归遍历 View。
     */
    private fun findAndClickCommentView(view: View): Boolean {

        val contentDescription =
            view.contentDescription?.toString() ?: ""

        val text =
            if (view is TextView) {
                view.text?.toString() ?: ""
            } else {
                ""
            }

        /*
         * 抖音评论按钮常见：
         *
         * contentDescription = "评论xxx，按钮"
         *
         * 或者 TextView = "评论"
         */
        val isComment =
            contentDescription.contains("评论") ||
            text.contains("评论")

        if (isComment && view.isShown) {

            if (verbose) {
                YLog.debug(
                    "$TAG: comment candidate found, " +
                        "class=${view.javaClass.name}, " +
                        "text=$text, " +
                        "contentDescription=$contentDescription, " +
                        "clickable=${view.isClickable}"
                )
            }

            /*
             * 优先使用 View 自己的点击事件。
             *
             * 即使 isClickable=false，
             * 某些抖音 View 仍可能通过父布局处理点击，
             * 所以这里不立即排除。
             */
            try {

                if (view.performClick()) {

                    if (verbose) {
                        YLog.debug(
                            "$TAG: performClick() succeeded"
                        )
                    }

                    return true
                }

            } catch (e: Throwable) {

                if (verbose) {
                    YLog.error(
                        "$TAG: performClick() failed",
                        e
                    )
                }
            }
        }

        /*
         * 当前 View 没找到，
         * 继续搜索子 View。
         */
        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                val child = view.getChildAt(i)

                if (findAndClickCommentView(child)) {
                    return true
                }
            }
        }

        return false
    }
}
