package io.github.twyora.douyinenhancer.hook.feed

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
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

        "com.ss.android.ugc.aweme.feed.ui.LongPressLayout".toClass().method {
            name = "onTouchEvent"
            param(MotionEventClass)
        }.hook {
            after {
                val event = args[0] as? MotionEvent ?: return@after
                val view = instance as? View ?: return@after

                if (event.action == MotionEvent.ACTION_UP) {
                    val currentTime = SystemClock.uptimeMillis()
                    if (currentTime - lastClickTime < 300) {
                        if (verbose) {
                            YLog.debug("$TAG: detected double tap gesture on LongPressLayout, searching comment view...")
                        }

                        val holderRootView = findHolderRootView(view) ?: view.rootView
                        if (performOpenComment(holderRootView)) {
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
                target.performClick()
            }
        } catch (e: Throwable) {
            target.performClick()
        }
    }
}
