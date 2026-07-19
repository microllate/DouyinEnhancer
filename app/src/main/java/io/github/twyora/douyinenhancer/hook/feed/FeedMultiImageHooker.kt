package io.github.twyora.douyinenhancer.hook.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.FileTypeDetector
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

    // TODO: FastKVConfigManager.global.getBoolean("verbose", true)
    private val verbose = false

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_MULTI_IMAGE_REMOVE_WATERMARK, false)) {
            return
        }

        installInjectPlayUrlIntoImageDownloadHook()

        installConvertVvicImageToPngHook()

        installConvertSingleVvicImageToMp4Hook()
        installConvertMultiVvicImagesToMp4Hook()
        installDisableSaveImageToVideoLocalWaterMaskHook()

        installConvertVvicCoverImageToPngHook()
        installDisableVEAddLiveVideoWaterMarkHook()
    }

    private fun installInjectPlayUrlIntoImageDownloadHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downloadAction.selfClass?.resolveMethod(
            packageInstance.downloadAction.startDownload()
        )?.hook {
            val seenAwemeIds = CircularFifoQueue<String>(5)
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

                // skip if this post was already processed
                aweme.invokeMethod<String>(
                    packageInstance.aweme.getAid()
                )?.let {
                    if (seenAwemeIds.contains(it)) {
                        return@before
                    }
                    seenAwemeIds.add(it)
                }

                val awemeImages = aweme.getField<List<*>>(
                    packageInstance.aweme.images()
                ).takeIf {
                    !it.isNullOrEmpty()
                } ?: run {
                    YLog.error("$TAG: aweme.images is null or empty")
                    return@before
                }

                awemeImages.forEach { imageStruct ->
                    if (imageStruct == null) {
                        return@forEach
                    }

                    // use the play URL as the image download URL
                    imageStruct.getField<List<*>>(
                        packageInstance.imageUrlStruct.urlList()
                    ).takeIf {
                        !it.isNullOrEmpty()
                    }?.let {
                        imageStruct.setField(
                            packageInstance.imageUrlStruct.downloadUrlList(),
                            it
                        )
                        if (verbose) {
                            YLog.debug("$TAG: image.urlList[0]: ${it.first()}")
                        }
                    } ?: run {
                        YLog.warn("$TAG: image has no play URL, skipping watermark-free injection")
                    }

                    // also replace the video's download URL when the post has a video
                    imageStruct.getField<Any>(
                        packageInstance.imageUrlStruct.video()
                    )?.let { video ->
                        video.invokeMethod<Any>(
                            packageInstance.video.getPlayAddr()
                        )?.let { playAddr ->
                            video.setField(
                                packageInstance.video.downloadAddr(),
                                playAddr
                            )
                            if (verbose) {
                                YLog.debug(
                                    "$TAG:play URL used as video download address: ${
                                        playAddr.getField<List<String?>>(
                                            packageInstance.urlModel.urlList()
                                        )?.first {
                                            !it.isNullOrBlank()
                                        }
                                    }"
                                )
                            }
                        }?.also {
                            video.setField(
                                packageInstance.video.hasWaterMark(),
                                false
                            )
                            video.setField(
                                packageInstance.video.hasSuffixWaterMark(),
                                false
                            )
                        }
                    }
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

    private fun installConvertVvicCoverImageToPngHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downloadLivePhotoExecutor.selfClass?.resolveMethod(
            packageInstance.downloadLivePhotoExecutor.encodeLivePhoto()
        )?.hook {
            before {
                val downloadTask = args[0] ?: return@before

                val vvicImagePathList = downloadTask.invokeMethod<List<String?>>(
                    packageInstance.downLoadTask.getTargetFilePaths()
                )?.filterNotNull()?.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                }

                if (verbose) {
                    YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
                }

                vvicImagePathList?.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert vvic cover image to png: $it")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert vvic cover image to png", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, vvic cover image to png conversion unavailable")
            }
        }
    }

    private fun installDisableVEAddLiveVideoWaterMarkHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.abTestServiceImpl.selfClass?.resolveMethod(
            packageInstance.abTestServiceImpl.enableVEAddLiveVideoWaterMark()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: disabling watermark for live video")
                }
                resultFalse()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook ABTestServiceImpl.enableVEAddLiveVideoWaterMark", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, unable to remove watermark from live video")
            }
        }

    private fun installConvertVvicImageToPngHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downLoadExecutor.selfClass?.resolveMethod(
            packageInstance.downLoadExecutor.execute()
        )?.hook {
            before {
                val downloadTask = args[0] ?: return@before

                val imageFilePath = downloadTask.invokeMethod<List<String?>>(
                    packageInstance.downLoadTask.getTargetFilePaths()
                )?.filterNotNull()?.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                } ?: run {
                    YLog.error("$TAG: failed to get image file path when downloading image")
                    return@before
                }

                imageFilePath.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert vvic image to png when downloading image: $it")
                    }
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

    private fun installConvertSingleVvicImageToMp4Hook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.singleImageToMp4Composer.selfClass?.resolveMethod(
            packageInstance.singleImageToMp4Composer.onLoad()
        )?.hook {
            before {
                // The instance currently holds both image paths and music paths,
                // and during the DexKit lookup phase I can't tell them apart, so we have to defer it to runtime
                val vvicImagePathList = instance.asResolver().field {
                    type = String::class
                }.mapNotNull {
                    it.getQuietly<String>()
                }.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                }

                if (verbose) {
                    YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
                }

                vvicImagePathList.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert single vvic image to png in mp4 composer: $it")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert single vvic image to png in mp4 composer", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, single vvic image to png conversion in mp4 composer unavailable")
            }
        }

    private fun installConvertMultiVvicImagesToMp4Hook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.multiImageToMp4Composer.selfClass?.resolveMethod(
            packageInstance.multiImageToMp4Composer.onLoad()
        )?.hook {
            before {
                val vvicImagePathList = instance.getField<List<List<String?>>>(
                    packageInstance.multiImageToMp4Composer.imagePathList()
                )?.flatten()?.filterNotNull()?.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                }

                if (verbose) {
                    YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
                }

                vvicImagePathList?.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert multi vvic images to png in mp4 composer: $it")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert multi vvic images to png in mutil mp4 composer", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, multi vvic images to png conversion in mp4 composer unavailable")
            }
        }

    private fun overwriteVvicWithPng(imageFilePath: String): Boolean {
        val imageFile = File(imageFilePath)
        if (!imageFile.exists()) {
            YLog.error("$TAG: failed to overwrite vvic image, source file does not exist: ${imageFile.absolutePath}")
            return false
        }
        if (verbose) {
            YLog.debug("$TAG: image file absolute path: ${imageFile.absolutePath}")
        }

        val fvvicInfo = FileTypeDetector.detect(imageFile)
        if (fvvicInfo.mimeType != "image/vvic") {
            if (verbose) {
                YLog.debug("$TAG: image is not in vvic format (got ${fvvicInfo.mimeType}), skipping")
            }
            return true
        }

        val imageBytes = runCatching {
            FileInputStream(imageFile).use { it.readBytes() }
        }.getOrElse {
            YLog.error("$TAG: failed to read vvic image for png conversion: ${imageFile.absolutePath}", it)
            return false
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
        )
        if (bitmap == null) {
            YLog.error("$TAG: failed to decode vvic image to bitmap: ${imageFile.absolutePath}")
            return false
        }

        val pngFile = imageFile.resolveSibling("${imageFile.nameWithoutExtension}.png")
        try {
            runCatching {
                pngFile.outputStream().use { out ->
                    require(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        "failed to compress vvic image to png"
                    }
                }
                imageFile.writeBytes(pngFile.readBytes())
            }.onFailure {
                YLog.error("$TAG: failed to overwrite vvic image with png: ${imageFile.absolutePath}", it)
                return false
            }
            if (verbose) {
                YLog.debug("$TAG: converted vvic image to png: ${imageFile.absolutePath}")
            }
            return true
        } finally {
            bitmap.recycle()
            pngFile.delete()
        }
    }
}
