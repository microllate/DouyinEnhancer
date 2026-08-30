package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.FeedKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedDoubleTapOpenCommentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

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
            before {
                // 1. 获取当前正在播放/展示的视频 Aweme 对象
                val aweme = instance.invokeMethod<Any>(
                    packageInstance.baseListFragmentPanel.getCurrentAweme()
                ) ?: run {
                    YLog.error("$TAG: unable to get current aweme")
                    return@before
                }

                // 2. 构建打开评论区的 VideoEvent 事件
                val openCommentPanelEvent = packageInstance.videoEvent.selfClass?.createInstance(
                    DouyinPackage.VideoEventModule.EVENT_OPEN_COMMENT_PANEL,
                    aweme
                ) ?: run {
                    YLog.error("$TAG: unable to build open-comment-panel event")
                    return@before
                }

                if (verbose) {
                    val awemeId = aweme.getField<String>(
                        packageInstance.aweme.aid()
                    )
                    YLog.debug("$TAG: dispatching open-comment-panel event for current aweme, aid: $awemeId")
                }

                // 3. 分发事件打开评论区
                instance.invokeMethodOnly(
                    packageInstance.baseListFragmentPanel.handleVideoEvent(),
                    openCommentPanelEvent
                )

                // 4. 关键：拦截原生的双击点赞逻辑
                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to dispatch open-comment-panel event for current aweme", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook double-tap handle for opening comment panel", throwable)
            }
        }
    }
}
