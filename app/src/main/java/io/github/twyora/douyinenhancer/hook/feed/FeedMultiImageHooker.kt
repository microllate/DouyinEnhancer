package io.github.twyora.douyinenhancer.hook.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.FileTypeDetector
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField
import java.io.File
import java.io.FileInputStream
import org.apache.commons.collections4.queue.CircularFifoQueue

@HookOnMainProcess
object FeedMultiImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val ring = CircularFifoQueue<String>(5)

    // TODO: FastKVConfigManager.global.getBoolean("verbose", true)
    private val verbose = false

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_MULTI_IMAGE_REMOVE_WATERMARK, false)) {
            return
        }

        installInjectPlayUrlIntoImageDownloadHook()
        installConvertVvicImageToPngHook()
        installDisableSaveImageToVideoLocalWaterMaskHook()

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
    }

    private fun installInjectPlayUrlIntoImageDownloadHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
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
                        return@forEach
                    }

                    val urlList = imageStruct.getField<List<*>>(
                        packageInstance.imageUrlStruct.urlList()
                    )
                    if (urlList.isNullOrEmpty()) {
                        YLog.error("$TAG: image has no play URL, skipping watermark-free injection")
                        return@forEach
                    } else {
                        if (verbose) {
                            YLog.debug("$TAG: image.urlList[0]: ${urlList.first()}")
                        }
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

    private fun installDisableSaveImageToVideoLocalWaterMaskHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.abTestServiceImpl.selfClass?.resolveMethod(
            packageInstance.abTestServiceImpl.enableSaveImageToVideoLocalWaterMask()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: disabling local watermark for save-image-to-video")
                }
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

    private fun installConvertVvicImageToPngHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downLoadExecutor.selfClass?.resolveMethod(
            packageInstance.downLoadExecutor.execute()
        )?.hook {
            before {
                val downloadTask = args[0] ?: return@before

                val imageFilePath = downloadTask.getField<String>(
                    packageInstance.downLoadTask.targetFilePath()
                ) ?: run {
                    YLog.error("$TAG: failed to get image file path from download task")
                    return@before
                }

                val imageFile = File(imageFilePath)
                if (!imageFile.exists()) {
                    YLog.error("$TAG: image file does not exist")
                    return@before
                }
                if (verbose) {
                    YLog.debug("$TAG: image file absolute path: ${imageFile.absolutePath}")
                }

                val fvvicInfo = FileTypeDetector.detect(imageFile)
                if (fvvicInfo.mimeType != "image/vvic") {
                    if (verbose) {
                        YLog.debug("$TAG: image is not in vvic format (got ${fvvicInfo.mimeType}), skipping")
                    }
                    return@before
                }

                // convert to bitmap
                val imageBytes = FileInputStream(imageFile).use {
                    it.readBytes()
                }
                val bitmap = packageInstance.heifDecoder.selfClass?.getStaticField<Any>(
                    packageInstance.heifDecoder.sBitmapFactory()
                )?.invokeMethod<Bitmap>(
                    packageInstance.heifBitmapFactoryImpl.decodeByteArray(),
                    imageBytes,
                    0,
                    imageBytes.size,
                    BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                ) ?: run {
                    YLog.error("$TAG: failed to decode image to bitmap")
                    return@before
                }

                // convert to png and overwrite the original image file
                val pngFile = File(imageFilePath).run {
                    resolveSibling("$nameWithoutExtension.png")
                }
                try {
                    runCatching {
                        pngFile.outputStream().use { out ->
                            require(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                                "failed to compress image to png"
                            }
                        }
                        imageFile.writeBytes(pngFile.readBytes())
                    }.onSuccess {
                        if (verbose) {
                            YLog.debug("$TAG: saved png image to ${imageFile.absolutePath}")
                        }
                    }.onFailure {
                        YLog.error("$TAG: failed to save png image", it)
                    }.also {
                        pngFile.delete()
                    }
                } finally {
                    bitmap.recycle()
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert vvic image to png", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, vvic image to png conversion unavailable")
            }
        }
    }
}
