package io.github.twyora.douyinenhancer.hook.feed

import android.os.SystemClock
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

    private var lastClickTime = 0L

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(FeedKey.FEED_DOUBLE_TAP_OPEN_COMMENT, false)) {
            if (verbose) {
                YLog.debug("$TAG: double-tap to open comment panel is disabled, skipping hook")
            }
            return
        }

        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleDoubleClick()
        )?.hook {
            after {
                val currentTime = SystemClock.uptimeMillis()
                if (currentTime - lastClickTime < 500) {
                    return@after
                }
                lastClickTime = currentTime

                val instanceView = findViewFromInstance(instance) ?: return@after
                val holderRootView = findHolderRootView(instanceView) ?: instanceView.rootView

                if (verbose) {
                    YLog.debug("$TAG: double-tap triggered, searching for comment button...")
                }

                performOpenComment(holderRootView)
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to conduct double-tap hook", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook double-tap handle", throwable)
            }
        }
    }

    private fun findViewFromInstance(panelInstance: Any): View? {
        panelInstance.javaClass.declaredFields.forEach { field ->
            if (View::class.java.isAssignableFrom(field.type)) {
                runCatching {
                    field.isAccessible = true
                    val view = field.get(panelInstance) as? View
                    if (view != null) return view
                }
            }
        }
        return null
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
