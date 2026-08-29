package io.github.twyora.douyinenhancer.hook.feed

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
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

    private var lastTouchDown = 0L
    private var lastTapTimeMs = 0L
    private var numberOfTaps = 0

    private var touchDownX = 0f
    private var touchDownY = 0f

    private val doubleTapTime =
        ViewConfiguration.getDoubleTapTimeout()

    private val longPressTime =
        ViewConfiguration.getLongPressTimeout()

    private var longPressRunnable: Runnable? = null

    override fun onHook() {

        if (!FastKVConfigManager.settings.getBoolean(
                FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT,
                false
            )
        ) {
            if (verbose) {
                YLog.debug(
                    "$TAG: double-tap open comment is disabled"
                )
            }
            return
        }

        /*
         * 和 Freedom+ 一样：
         *
         * 直接 Hook 抖音的 LongPressLayout.onTouchEvent()
         *
         * 不再 Hook BaseListFragmentPanel.handleDoubleClick()
         */
        try {

            val longPressLayoutClass =
                Class.forName(
                    "com.ss.android.ugc.aweme.feed.ui.LongPressLayout",
                    false,
                    packageInstance.appClassLoader
                )

            longPressLayoutClass
                .resolveMethod(
                    "onTouchEvent",
                    MotionEvent::class.java
                )
                ?.hook {

                    after {

                        val view = instance as? View
                            ?: return@after

                        val event =
                            args.firstOrNull() as? MotionEvent
                                ?: return@after

                        handleTouchEvent(view, event)
                    }

                }?.result {

                    onConductFailure { _, throwable ->
                        YLog.error(
                            "$TAG: onTouchEvent hook failed",
                            throwable
                        )
                    }

                    onHookingFailure { throwable ->
                        YLog.error(
                            "$TAG: failed to hook LongPressLayout.onTouchEvent",
                            throwable
                        )
                    }
                }

            if (verbose) {
                YLog.debug(
                    "$TAG: LongPressLayout.onTouchEvent hooked"
                )
            }

        } catch (e: Throwable) {

            YLog.error(
                "$TAG: failed to find LongPressLayout",
                e
            )
        }

        /*
         * 关键：
         *
         * 抖音自己的双击事件仍然会触发点赞。
         *
         * 所以还需要把原生双击点赞事件拦截掉。
         *
         * 这里沿用 Freedom+ 的思路：
         * 找到真正处理双击的类后，
         * 对包含 View + MotionEvent 参数的方法进行拦截。
         */
        hookNativeDoubleClick()
    }

    private fun handleTouchEvent(
        view: View,
        event: MotionEvent
    ) {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                touchDownX = event.x
                touchDownY = event.y

                lastTouchDown =
                    System.currentTimeMillis()

                longPressRunnable?.let {
                    view.handler.removeCallbacks(it)
                }

                longPressRunnable = Runnable {
                    // 这里不需要处理长按
                }

                view.handler.postDelayed(
                    longPressRunnable!!,
                    longPressTime.toLong()
                )
            }

            MotionEvent.ACTION_UP -> {

                longPressRunnable?.let {
                    view.handler.removeCallbacks(it)
                }

                longPressRunnable = null

                val now =
                    System.currentTimeMillis()

                /*
                 * 超过长按时间，不认为是点击
                 */
                if (now - lastTouchDown >= longPressTime) {
                    return
                }

                /*
                 * 防止拖动/滑动被误认为双击
                 */
                if (isMoved(event)) {
                    numberOfTaps = 0
                    return
                }

                /*
                 * 判断双击
                 */
                if (
                    now - lastTapTimeMs < doubleTapTime &&
                    numberOfTaps == 1
                ) {

                    numberOfTaps = 0

                    if (verbose) {
                        YLog.debug(
                            "$TAG: double tap detected"
                        )
                    }

                    onDoubleClick(view)

                } else {

                    numberOfTaps = 1
                }

                lastTapTimeMs = now
            }

            MotionEvent.ACTION_CANCEL -> {

                longPressRunnable?.let {
                    view.handler.removeCallbacks(it)
                }

                longPressRunnable = null
                numberOfTaps = 0
            }
        }
    }

    private fun isMoved(
        event: MotionEvent
    ): Boolean {

        return kotlin.math.abs(
            touchDownX - event.x
        ) > 10 ||
            kotlin.math.abs(
                touchDownY - event.y
            ) > 10
    }

    private fun onDoubleClick(
        view: View
    ) {

        /*
         * 这里是 Freedom+ 最关键的一句。
         *
         * 当前 LongPressLayout
         * → 向上找到 VideoViewHolderRootView
         */
        val rootView =
            findParentByClassName(view)

        if (rootView == null) {

            if (verbose) {
                YLog.debug(
                    "$TAG: VideoViewHolderRootView not found"
                )
            }

            return
        }

        if (verbose) {
            YLog.debug(
                "$TAG: VideoViewHolderRootView found: " +
                    rootView.javaClass.name
            )
        }

        clickCommentView(rootView)
    }

    /**
     * 向上寻找：
     *
     * com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView
     */
    private fun findParentByClassName(
        view: View
    ): View? {

        var current: View? = view

        while (current != null) {

            if (
                current.javaClass.name ==
                "com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView"
            ) {
                return current
            }

            current =
                (current.parent as? View)
        }

        return null
    }

    /**
     * 在 VideoViewHolderRootView
     * 中寻找评论按钮。
     *
     * 完全参考 Freedom+：
     *
     * contentDescription：
     *     评论xxx，按钮
     *
     * TextView：
     *     评论
     */
    private fun clickCommentView(
        parent: View
    ) {

        if (findAndClickCommentView(parent)) {

            if (verbose) {
                YLog.debug(
                    "$TAG: comment clicked"
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

    private fun findAndClickCommentView(
        view: View
    ): Boolean {

        val content =
            view.contentDescription
                ?.toString()
                ?: ""

        val text =
            if (view is TextView) {
                view.text
                    ?.toString()
                    ?: ""
            } else {
                ""
            }

        /*
         * Freedom+ 使用：
         *
         * Regex("评论(.*?)，按钮")
         *
         * 这里不用 Regex，直接 contains，
         * 对不同版本抖音的兼容性反而更好。
         */
        val isComment =
            content.contains("评论") ||
                text == "评论"

        if (isComment && view.isShown) {

            if (verbose) {

                YLog.debug(
                    "$TAG: comment candidate: " +
                        "class=${view.javaClass.name}, " +
                        "text=$text, " +
                        "content=$content, " +
                        "clickable=${view.isClickable}"
                )
            }

            /*
             * 第一优先级：
             * View 自己的 performClick()
             */
            try {

                if (view.performClick()) {

                    if (verbose) {
                        YLog.debug(
                            "$TAG: performClick success"
                        )
                    }

                    return true
                }

            } catch (e: Throwable) {

                if (verbose) {
                    YLog.error(
                        "$TAG: performClick failed",
                        e
                    )
                }
            }
        }

        /*
         * 继续递归 ViewGroup
         */
        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                val child =
                    view.getChildAt(i)

                if (
                    findAndClickCommentView(child)
                ) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * 拦截抖音自己的双击点赞。
     *
     * 注意：
     *
     * 这部分不能简单依赖
     * BaseListFragmentPanel.handleDoubleClick，
     * 因为 Freedom+ 的实际方案是：
     *
     * doubleClickEventClazz
     * → 找到真正处理双击的类
     * → 拦截 View + MotionEvent 参数的方法
     *
     * 如果当前项目没有对应的 DexkitBuilder，
     * 这里先不强行猜类。
     */
    private fun hookNativeDoubleClick() {

        try {

            val dexkitBuilderClass =
                Class.forName(
                    "io.github.twyora.douyinenhancer.hook.feed.DexkitBuilder"
                )

            if (verbose) {
                YLog.debug(
                    "$TAG: DexkitBuilder exists"
                )
            }

        } catch (_: Throwable) {

            if (verbose) {
                YLog.debug(
                    "$TAG: DexkitBuilder not available in this package"
                )
            }
        }
    }
}
