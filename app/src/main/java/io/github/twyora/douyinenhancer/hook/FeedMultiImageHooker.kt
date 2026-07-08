package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod
import io.github.twyora.douyinenhancer.hook.utils.setField

object FeedMultiImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(name = mainProcessName) {
            installInjectPlayUrlIntoImageDownloadHook()
            installDisableSaveImageToVideoLocalWaterMaskHook()
        }
    }

    private fun installInjectPlayUrlIntoImageDownloadHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val packageInstance = DouyinPackage.instance

        return packageInstance.downloadAction.selfClass?.resolveMethod(
            packageInstance.downloadAction.startDownload()
        )?.hook {
            before {
                val aweme = instance.getField<Any>(
                    packageInstance.downloadAction.aweme()
                ) ?: return@before
                if (aweme.invokeMethod<Boolean>(
                        packageInstance.aweme.isMultiImage()
                    ) == false
                ) {
                    return@before
                }

                val awemeImages = aweme.getField<List<*>>(
                    packageInstance.aweme.images()
                )
                if (awemeImages.isNullOrEmpty()) {
                    YLog.error("$TAG: aweme.images is null or empty")
                    return@before
                }

                awemeImages.forEach { imageStruct ->
                    if (imageStruct == null) {
                        return@forEach
                    }

                    val urlList = imageStruct.getField<List<*>>(
                        packageInstance.imageUrlStruct.urlList()
                    )
                    if (urlList.isNullOrEmpty()) {
                        YLog.error("$TAG: image has no play URL, skipping watermark-free injection")
                        return@forEach
                    }

                    imageStruct.setField(
                        packageInstance.imageUrlStruct.downloadUrlList(),
                        urlList
                    )
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to inject play URL into image download", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, multi-image watermark-free download unavailable")
            }
        }
    }

    private fun installDisableSaveImageToVideoLocalWaterMaskHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val packageInstance = DouyinPackage.instance

        return packageInstance.abTestServiceImpl.selfClass?.resolveMethod(
            packageInstance.abTestServiceImpl.enableSaveImageToVideoLocalWaterMask()
        )?.hook {
            before {
                YLog.debug("$TAG: disabling local watermark for save-image-to-video")
                resultFalse()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook ABTestServiceImpl.enableSaveImageToVideoLocalWaterMask", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, unable to remove watermark from save-image-to-video")
            }
        }
    }
}