package io.github.twyora.douyinenhancer.hook.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.RelativeDateTimeFormatter
import android.os.strictmode.UntaggedSocketViolation
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.io.FileInputStream
import java.nio.file.Files
import kotlin.io.path.Path

@HookOnMainProcess
object FeedMultiImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName
    private val ring = CircularFifoQueue<String>(5)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_MULTI_IMAGE_REMOVE_WATERMARK, false)) {
            return
        }

        installInjectPlayUrlIntoImageDownloadHook()
        installDisableSaveImageToVideoLocalWaterMaskHook()

        val packageInstance = DouyinPackage.instance
        packageInstance.imageResourceRxDownloadListener.selfClass?.resolveMethod(
            packageInstance.imageResourceRxDownloadListener.onSuccessed()
        )?.hook {
            before {
                val dlInfo = args[0] ?: return@before

                val filePath = dlInfo.invokeMethod<String>(
                    packageInstance.downloadInfo.getTargetFilePath()
                )
                if (filePath == null) {
                    YLog.error("$TAG: target file path is null")
                } else {
                    YLog.debug("$TAG: file path: $filePath")
                }
            }
        }

        //LX/19xB;->LIZIZ(LX/19u9;)Z
        "X.19xB".toClass().resolve().firstMethodOrNull {
            name = "LIZIZ"
        }?.hook {
            before {
                val _19u9 = args[0] ?: return@before
                val imgFilePath = _19u9.getField<String>(
                    Field(name = "LIZJ")
                )
                if (imgFilePath == null) {
                    YLog.error("$TAG: imgFilePath is null")
                    return@before
                } else {
                    YLog.debug("$TAG: imgFilePath: $imgFilePath")
                }

                val imgBytes = FileInputStream(imgFilePath).use {
                    it.readBytes()
                }
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888

                val bitmap = "com.bytedance.fresco.heif.HeifDecoder".toClass().getStaticField<Any>(
                    Field(name = "sBitmapFactory")
                )?.invokeMethod<Bitmap>(
                    Method(name = "decodeByteArray", parameters = null),
                    imgBytes,
                    0,
                    imgBytes.size,
                    options
                )
                if (bitmap == null) {
                    YLog.error("$TAG: bitmap is null")
                    return@before
                } else {
                    YLog.info("$TAG: bitmap nooooooooooot null!")
                }
                bitmap.recycle()
            }
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

                val awemeId = aweme.invokeMethod<String>(
                    packageInstance.aweme.getAid()
                )
                if (ring.contains(awemeId)) {
                    return@before
                } else if (awemeId != null) {
                    ring.add(awemeId)
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
                        return@before
                    }

                    val urlList = imageStruct.getField<List<*>>(
                        packageInstance.imageUrlStruct.urlList()
                    )
                    if (urlList.isNullOrEmpty()) {
                        YLog.error("$TAG: image has no play URL, skipping watermark-free injection")
                        return@forEach
                    } else {
                        YLog.debug("$TAG: image.urlList[0]: ${urlList.first()}")
                    }

                    imageStruct.setField(
                        packageInstance.imageUrlStruct.downloadUrlList(),
                        urlList
                    )
                    /*
                    val downloadUrlList = imageStruct.getField<List<String>>(
                        Field(name = "downloadUrlList")
                    )
                    val maskUrlList = imageStruct.getField<List<String>>(
                        Field(name = "maskUrlList")
                    )
                    val urlList = imageStruct.getField<List<String>>(
                        Field(name = "urlList")
                    )
                    val watermarkFreeDownloadUrlList = imageStruct.getField<List<String>>(
                        Field(name = "watermarkFreeDownloadUrlList")
                    )
                    val imageExtra = imageStruct.getField<Any>(
                        Field(name = "imageExtra")
                    )
                    val backupUrlList = imageExtra?.getField<List<String>>(
                        Field(name = "backupUrlList")
                    )

                    YLog.debug(
                        "$TAG: urlList: ${
                            urlList?.firstOrNull()
                        }, downloadUrlList: ${
                            downloadUrlList?.firstOrNull()
                        }, maskUrlList: ${
                            maskUrlList?.firstOrNull()
                        }, watermarkFreeDownloadUrlList: ${
                            watermarkFreeDownloadUrlList?.firstOrNull()
                        }, backupUrlList: ${
                            backupUrlList?.firstOrNull()
                        }"
                    )
                     */

                    /*
                    if (imageStruct == null) {
                        return@forEach
                    }

                     */
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
