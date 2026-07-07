package io.github.twyora.douyinenhancer.hook

import com.highcapable.kavaref.KavaRef.Companion.asResolver
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
            val packageInstance = DouyinPackage.instance

            packageInstance.downloadAction.selfClass?.resolveMethod(
                packageInstance.downloadAction.startDownload()
            )?.hook {
                before {
                    val aweme = instance.getField<Any>(
                        packageInstance.downloadAction.aweme()
                    ) ?: run {
                        YLog.error("$TAG: aweme is null")
                        return@before
                    }
                    if (aweme.invokeMethod<Boolean>(
                            packageInstance.aweme.isMultiImage()
                        ) == false
                    ) {
                        return@before
                    }

                    val awemeImages = aweme.getField<List<*>>(
                        packageInstance.aweme.images()
                    ) ?: run {
                        YLog.error("$TAG: aweme.images is null")
                        return@before
                    }
                    if (awemeImages.isEmpty()) {
                        YLog.error("$TAG: aweme.images is empty")
                        return@before
                    }

                    YLog.debug("$TAG: downloadAction.startDownload triggered")
                    awemeImages.forEachIndexed { index, imageStruct ->
                        if (imageStruct == null) {
                            YLog.error("$TAG: aweme.images[$index] is null")
                            return@forEachIndexed
                        }

                        val originUrlList = imageStruct.getField<List<*>>(
                            packageInstance.imageUrlStruct.urlList()
                        )
                        if (originUrlList.isNullOrEmpty()) {
                            YLog.error("$TAG: originUrlList is null or empty")
                            return@forEachIndexed
                        }
                        YLog.debug("$TAG: originUrlList[0]: ${originUrlList.first()}")

                        imageStruct.setField(
                            packageInstance.imageUrlStruct.downloadUrlList(),
                            originUrlList
                        )
                    }
                }
            }
        }
    }
}