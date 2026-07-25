package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField

@HookOnMainProcess
object FeedDownloadModuleHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        packageInstance.absPermissionChecker.selfClass?.resolveMethod(
            packageInstance.absPermissionChecker.getActionCheckResult()
        )?.hook {
            after {
                val actionCheckResult = result ?: run {
                    YLog.warn("$TAG: getActionCheckResult of AbsPermissionChecker returned null, cannot override action status")
                    return@after
                }
                val actionStatus = actionCheckResult.getField<Any>(
                    packageInstance.actionCheckResult.actionStatus()
                ) ?: run {
                    YLog.warn("$TAG: actionStatus field of ActionCheckResult returned null, cannot read action status")
                    return@after
                }

                val normalStatus = packageInstance.actionStatus.selfClass?.invokeStaticMethod<Any>(
                    packageInstance.actionStatus.valueOf(),
                    packageInstance.actionStatus.normal().name
                )

                if (verbose) {
                    YLog.debug("$TAG: permission check actionStatus is $actionStatus")
                }

                if (actionStatus != normalStatus) {
                    YLog.info("$TAG: actionStatus is $actionStatus, override to $normalStatus")
                    actionCheckResult.setField(
                        packageInstance.actionCheckResult.actionStatus(),
                        normalStatus
                    )
                }
            }
        }

        packageInstance.galleryShareHelper.selfClass?.resolveMethod(
            packageInstance.galleryShareHelper.startDownload()
        )?.hook {
            before {
                val aweme = args[0] ?: return@before
                val downloadStatus = aweme.invokeMethod<Int>(
                    packageInstance.aweme.getDownloadStatus()
                )

                if (verbose) {
                    YLog.debug("$TAG: aweme downloadStatus is $downloadStatus")
                }

                if (downloadStatus != 0) {
                    YLog.info("$TAG: overriding download status of aweme from $downloadStatus to 0")
                    aweme.getField<Any>(
                        packageInstance.aweme.status()
                    )?.setField(
                        packageInstance.awemeStatus.downloadStatus(),
                        0
                    )
                }
            }
        }

        packageInstance.sharePrivacyVideoApi.selfClass?.resolveMethod(
            packageInstance.sharePrivacyVideoApi.getDownloadStatus()
        )?.hook {
            before {
                val response = packageInstance.sharePrivacyVideoApi.privacyVideoResponse.selfClass?.createInstance() ?: run {
                    YLog.error("$TAG: failed to create privacyVideoResponse, cannot set allowed download status")
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
                    YLog.error("$TAG: rxObservable.just() returned null, cannot wrap response into observable")
                    return@before
                }
                result = observable
            }
        }
    }
}
