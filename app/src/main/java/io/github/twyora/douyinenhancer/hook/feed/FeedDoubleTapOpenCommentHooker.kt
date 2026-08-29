package io.github.twyora.douyinenhancer.hook.feed

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.MotionEventClass
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess

@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private var lastClickTime = 0L

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap to open comment panel is disabled, skipping hook")
            }
            return
        }

        // 直接 Hook 抖音手势布局 LongPressLayout 的 onTouchEvent
        "com.ss.android.ugc.aweme.feed.ui.LongPressLayout".toClass().method {
            name = "onTouchEvent"
            param(MotionEventClass)
        }.hook {
            after {
                val event = args[0] as? MotionEvent ?: return@after
                val view = instance as? View ?: return@after

                // 监听手势抬起动作 (ACTION_UP) 作为双击判定点
                if (event.action == MotionEvent.ACTION_UP) {
                    val currentTime = SystemClock.uptimeMillis()
                    // 两次点击间隔在 300ms 以内判定为双击
                    if (currentTime - lastClickTime < 300) {
                        if (verbose) {
                            YLog.debug("$TAG: detected double tap gesture on LongPressLayout, searching comment view...")
                        }

                        // 找到当前视频卡片的根节点 View
                        val holderRootView = findHolderRootView(view) ?: view.rootView
                        if (performOpenComment(holderRootView)) {
                            // 触发成功后重置时间，防止重复触发
                            lastClickTime = 0L
                        }
                    } else {
                        lastClickTime = currentTime
                    }
                }
            }
        }.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook LongPressLayout", throwable)
            }
        }
    }

    private fun findHolderRootView(view: View): View? {
        var current: View? = view
        while (current != null) {
            if (current.javaClass.name.contains("VideoViewHolderRootView")) {
                return current
            }
            current = current.parent as? View
        }
        return null
    }

    private fun performOpenComment(parent: View): Boolean {
        val commentRegex = Regex("评论(.*?)，按钮")
        var targetView: View? = null

        fun search(v: View) {
            if (targetView != null) return

            val contentDesc = v.contentDescription?.toString() ?: ""
            val text = (v as? TextView)?.text?.toString() ?: ""

            if (commentRegex.containsMatchIn(contentDesc) || commentRegex.containsMatchIn(text)) {
                targetView = v
                return
            }

            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    search(v.getChildAt(i))
                }
            }
        }

        search(parent)

        val target = targetView ?: run {
            YLog.error("$TAG: comment view with matching contentDescription not found")
            return false
        }

        // 优先通过常规 View 监听器触发点击
        return try {
            val listenerField = View::class.java.getDeclaredField("mListenerInfo").apply { isAccessible = true }
            val listenerInfo = listenerField.get(target)
            val onClickListenerField = listenerInfo?.javaClass?.getDeclaredField("mOnClickListener")?.apply { isAccessible = true }
            val onClickListener = onClickListenerField?.get(listenerInfo) as? View.OnClickListener

            if (onClickListener != null) {
                onClickListener.onClick(target)
                if (verbose) YLog.debug("$TAG: successfully triggered OnClickListener directly")
                true
            } else {
                // 回退方案：通过 performClick 触发
                target.performClick()
            }
        } catch (e: Throwable) {
            target.performClick()
        }
    }
}
