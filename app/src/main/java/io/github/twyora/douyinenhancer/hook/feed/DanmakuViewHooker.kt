package io.github.twyora.douyinenhancer.hook.feed

import android.view.View
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object DanmakuViewHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    private var danmakuViewId: Int = View.generateViewId()

    override fun onHook() {
        installAssignDanmakuViewIdHook()
        installAddDanmakuViewIdToCleanModeWhiteListHook()
        installBlockDanmakuViewHidingHook()
    }

    private fun installAssignDanmakuViewIdHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.danmakuView.selfClass?.resolveMethod(
            packageInstance.danmakuView.onAttachedToWindow()
        )?.hook {
            after {
                val danmakuView = instance as? View ?: run {
                    YLog.error("$TAG: danmakuView is null")
                    return@after
                }

                if (danmakuView.id == View.NO_ID) {
                    if (verbose) {
                        YLog.debug("$TAG: danmaku view has no view id, setting to ${danmakuViewId}")
                    }
                    danmakuView.id = danmakuViewId
                } else {
                    if (verbose) {
                        YLog.debug("$TAG: danmaku view id: ${danmakuView.id}")
                    }
//                    danmakuViewId = danmakuView.id
                }
            }
        }
    }

    private fun installAddDanmakuViewIdToCleanModeWhiteListHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.cleanModePresenter.selfClass?.resolveMethod(
            packageInstance.cleanModePresenter.enterCleanMode()
        )?.hook {
            before {
                @Suppress("UNCHECKED_CAST")
                val whiteList = args[4] as? MutableList<Int> ?: run {
                    YLog.error("$TAG: whiteList is null")
                    return@before
                }

                if (verbose) {
                    YLog.debug("$TAG: adding danmakuViewId(${danmakuViewId}) into clean mode white list")
                }

                whiteList.add(danmakuViewId)
            }
        }
    }

    private fun installBlockDanmakuViewHidingHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.cleanModePresenter.selfClass?.resolveMethod(
            packageInstance.cleanModePresenter.setVisibility()
        )?.hook {
            before {
                val view = args[0] as? View ?: run {
                    YLog.error("$TAG: view is null")
                    return@before
                }
                val visibility = args[1] as? Int ?: run {
                    YLog.error("$TAG: visibility is null")
                    return@before
                }
                if (visibility == View.VISIBLE) {
                    return@before
                }

                if (view.findViewById<View?>(danmakuViewId) == null) {
                    return@before
                }
                YLog.info("$TAG: ${view::class.qualifiedName}{id=${view.id.toString(16)}} is a danmaku view holder trying to hide itself; intercept it")

                resultNull()
            }
        }
    }
}