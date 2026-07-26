package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.HookTransaction
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField

@HookOnMainProcess
object FeedDownloadHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        val transaction = HookTransaction(TAG)

        transaction.add(::installForceActionStatusNormalHook.name) {
            installForceActionStatusNormalHook()
        }
        transaction.add(::installOverridePrivacyVideoDownloadStatusHook.name) {
            installOverrideAwemeDownloadStatusHook()
        }
        transaction.add(::installOverridePrivacyVideoDownloadStatusHook.name) {
            installOverridePrivacyVideoDownloadStatusHook()
        }

        transaction.commit()
    }

    private fun installForceActionStatusNormalHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.absPermissionChecker.selfClass?.resolveMethod(
            packageInstance.absPermissionChecker.getActionCheckResult()
        )?.hook {
            after {
                val actionCheckResult = result ?: run {
                    YLog.warn("$TAG: action permission check result is null, cannot force action status to allowed, download may be blocked")
                    return@after
                }
                val actionStatus = actionCheckResult.getField<Any>(
                    packageInstance.actionCheckResult.actionStatus()
                ) ?: run {
                    YLog.warn("$TAG: action status is null, cannot decide whether to force it to allowed")
                    return@after
                }

                val normalStatus = packageInstance.actionStatus.selfClass?.invokeStaticMethod<Any>(
                    packageInstance.actionStatus.valueOf(),
                    packageInstance.actionStatus.normal().name
                )

                if (verbose) {
                    YLog.debug("$TAG: action status: $actionStatus (target allowed: $normalStatus)")
                }

                if (actionStatus != normalStatus) {
                    YLog.info("$TAG: forcing action status from $actionStatus to $normalStatus to allow download")
                    actionCheckResult.setField(
                        packageInstance.actionCheckResult.actionStatus(),
                        normalStatus
                    )
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook action permission check, download action may stay blocked", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, action status cannot be forced to allowed, download may be blocked")
            }
        }
    }

    private fun installOverrideAwemeDownloadStatusHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.galleryShareHelper.selfClass?.resolveMethod(
            packageInstance.galleryShareHelper.startDownload()
        )?.hook {
            before {
                val aweme = args[0] ?: return@before
                val downloadStatus = aweme.invokeMethod<Int>(
                    packageInstance.aweme.getDownloadStatus()
                )

                if (verbose) {
                    YLog.debug("$TAG: aweme download status: $downloadStatus")
                }

                if (downloadStatus != 0) {
                    YLog.info("$TAG: resetting aweme download status from $downloadStatus to 0 to allow download")
                    aweme.getField<Any>(
                        packageInstance.aweme.status()
                    )?.setField(
                        packageInstance.awemeStatus.downloadStatus(),
                        0
                    )
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook gallery share download, aweme download status may stay restricted", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, aweme download status cannot be reset, download may be blocked")
            }
        }
    }

    private fun installOverridePrivacyVideoDownloadStatusHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.sharePrivacyVideoApi.selfClass?.resolveMethod(
            packageInstance.sharePrivacyVideoApi.getDownloadStatus()
        )?.hook {
            before {
                val itemId = args[0] as? String
                if (verbose) {
                    YLog.debug("$TAG: privacy video download status query aweme id: $itemId")
                }

                val response = packageInstance.sharePrivacyVideoApi.privacyVideoResponse.selfClass?.createInstance() ?: run {
                    YLog.error("$TAG: failed to build the allowed-download response, privacy video download may stay blocked")
                    return@before
                }
                response.setField(
                    packageInstance.sharePrivacyVideoApi.privacyVideoResponse.msg(),
                    ""
                )
                response.setField(
                    packageInstance.sharePrivacyVideoApi.privacyVideoResponse.status(),
                    0
                )

                val observable = packageInstance.rxObservable.selfClass?.invokeStaticMethod<Any>(
                    packageInstance.rxObservable.just(),
                    response
                ) ?: run {
                    YLog.error("$TAG: failed to return the allowed-download response, privacy video download may stay blocked")
                    return@before
                }
                result = observable
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook privacy video download status, host will not treat it as downloadable", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, privacy video download status cannot be faked, download stays blocked")
            }
        }
    }
}
