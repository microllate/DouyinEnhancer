/*
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/BiliBiliPackage.kt)
 */

package io.github.twyora.douyinenhancer.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.provider.Settings
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.matcher.extension.parameterizedBy
import com.highcapable.kavaref.condition.matcher.extension.toTypeMatcher
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.generated.AppProperties
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.weak
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import kotlin.time.measureTimedValue
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.matchers.base.OpCodesMatcher

val Configs.Class.nameOrNull
    get() =
        if (hasName()) {
            name
        } else {
            null
        }

val Configs.Field.nameOrNull
    get() =
        if (hasName()) {
            name
        } else {
            null
        }

val Configs.Method.nameOrNull
    get() =
        if (hasName()) {
            name
        } else {
            null
        }

val Configs.Method.Parameters.valuesListOrNull
    get() =
        valuesList.ifEmpty {
            null
        }

class DouyinPackage(private val classLoader: ClassLoader, context: Context) {
    init {
        instance = this
    }

    private val hookInfo: Configs.HookInfo =
        run {
            val (result, time) =
                measureTimedValue {
                    readHookInfo(context)
                }
            YLog.debug("$TAG: load hookInfo time: $time")
            YLog.debug("$TAG: hookInfo: $result")

            result
        }

    fun hostVersionCode() = hookInfo.hostVersionCode

    val commentImageStruct = CommentImageStructModule()
    val urlModel = UrlModelModule()
    val comment = CommentModule()
    val commentAudioStruct = CommentAudioStructModule()
    val emoji = EmojiModule()
    val commentActionParams = CommentActionParamsModule()
    val commentLongPressItemModel = CommentLongPressItemModelModule()
    val saveImageActionItem = SaveImageActionItemModule()
    val commentExtensionsKt = CommentExtensionsKtModule()
    val listenerProviderParam = ListenerProviderParamModule()
    val commentImageSaveDownloadListener = CommentImageSaveDownloadListenerModule()
    val downloadInfo = DownloadInfoModule()
    val digestUtils = DigestUtilsModule()
    val ugFileUtils = UGFileUtilsKtModule()
    val tokenCert = TokenCertModule()
    val commonItemView = CommonItemViewModule()
    val douYinSettingNewVersionActivity = DouYinSettingNewVersionActivityModule()
    val user = UserModule()
    val aweme = AwemeModule()
    val video = VideoModule()
    val imageUrlStruct = ImageUrlStructModule()
    val feedResponseHandler = FeedResponseHandlerModule()
    val commentLongPressWhiteListProvider = CommentLongPressWhiteListProviderModule()
    val miscDownloadAddrUtil = MiscDownloadAddrUtilModule()
    val downloadAction = DownloadActionModule()
    val abTestServiceImpl = ABTestServiceImplModule()
    val lppDownloadModule = LppDownloadModuleModule()
    val awemeStatistics = AwemeStatisticsModule()
    val heifDecoder = HeifDecoderModule()
    val heifBitmapFactoryImpl = HeifBitmapFactoryImplModule()
    val downLoadExecutor = DownLoadExecutorModule()
    val downLoadTask = DownLoadTaskModule()
    val downloadLivePhotoExecutor = DownloadLivePhotoExecutorModule()
    val singleImageToMp4Composer = SingleImageToMp4ComposerModule()
    val multiImageToMp4Composer = MultiImageToMp4ComposerModule()
    val mainActivity = MainActivityModule()

    inner class CommentImageStructModule {
        val selfClass by weak {
            hookInfo.commentImageStruct.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun originUrl() = Field(hookInfo.commentImageStruct.originUrl.nameOrNull)

        fun downloadUrl() = Field(hookInfo.commentImageStruct.downloadUrl.nameOrNull)

        fun getDownloadUrl() = Method(
            hookInfo.commentImageStruct.getDownloadUrl.nameOrNull,
            hookInfo.commentImageStruct.getDownloadUrl.parameters.valuesListOrNull
        )
    }

    inner class UrlModelModule {
        val selfClass by weak {
            hookInfo.urlModel.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun urlList() = Field(hookInfo.urlModel.urlList.nameOrNull)
    }

    inner class CommentModule {
        val selfClass by weak {
            hookInfo.comment.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun emoji() = Field(hookInfo.comment.emoji.nameOrNull)

        fun imageList() = Field(hookInfo.comment.imageList.nameOrNull)

        fun commentAudio() = Field(hookInfo.comment.commentAudio.nameOrNull)
    }

    inner class CommentAudioStructModule {
        val selfClass by weak {
            hookInfo.commentAudioStruct.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun content() = Field(hookInfo.commentAudioStruct.content.nameOrNull)
    }

    inner class EmojiModule {
        val selfClass by weak {
            hookInfo.emoji.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun animateUrl() = Field(hookInfo.emoji.animateUrl.nameOrNull)
    }

    inner class CommentActionParamsModule {
        val selfClass by weak {
            hookInfo.commentActionParams.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun comment() = Field(hookInfo.commentActionParams.comment.nameOrNull)

        fun imageIndex() = Field(hookInfo.commentActionParams.imageIndex.nameOrNull)
    }

    inner class CommentLongPressItemModelModule {
        val selfClass by weak {
            hookInfo.commentLongPressItemModel.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun commentActionParams() = Field(hookInfo.commentLongPressItemModel.commentActionParams.nameOrNull)
    }

    inner class SaveImageActionItemModule {
        val selfClass by weak {
            hookInfo.saveImageActionItem.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun commentActionParams() = Field(hookInfo.saveImageActionItem.cmtActionParams.nameOrNull)

        fun saveImageActionParams() = Field(hookInfo.saveImageActionItem.saveImgActionParams.nameOrNull)

        inner class OnClickExecutorModule {
            val selfClass by weak {
                hookInfo.saveImageActionItem.onClickExecutor.class_.nameOrNull?.toClass(classLoader)
            }

            fun onClick() = Method(
                hookInfo.saveImageActionItem.onClickExecutor.onClick.nameOrNull,
                hookInfo.saveImageActionItem.onClickExecutor.onClick.parameters.valuesListOrNull
            )

            fun hostItem() = Field(hookInfo.saveImageActionItem.onClickExecutor.hostItem.nameOrNull)
        }

        val onClickExecutor = OnClickExecutorModule()

        fun isVisible() = Method(
            hookInfo.saveImageActionItem.isVisible.nameOrNull,
            hookInfo.saveImageActionItem.isVisible.parameters.valuesListOrNull
        )
    }

    inner class CommentExtensionsKtModule {
        val selfClass by weak {
            hookInfo.commentExtensionKt.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun hasValidImageUrl() = Method(
            hookInfo.commentExtensionKt.hasValidImageUrl.nameOrNull,
            hookInfo.commentExtensionKt.hasValidImageUrl.parameters.valuesListOrNull
        )
    }

    inner class ListenerProviderParamModule {
        val selfClass by weak {
            hookInfo.listenerProviderParam.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun context() = Field(hookInfo.listenerProviderParam.context.nameOrNull)

        fun cert() = Field(hookInfo.listenerProviderParam.cert.nameOrNull)
    }

    inner class CommentImageSaveDownloadListenerModule {
        val selfClass by weak {
            hookInfo.commentImageSaveDownloadListener.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun onSuccessed() = Method(
            hookInfo.commentImageSaveDownloadListener.onSuccessed.nameOrNull,
            hookInfo.commentImageSaveDownloadListener.onSuccessed.parameters.valuesListOrNull
        )

        fun notifyResult() = Method(
            hookInfo.commentImageSaveDownloadListener.notifyResult.nameOrNull,
            hookInfo.commentImageSaveDownloadListener.notifyResult.parameters.valuesListOrNull
        )

        fun listenerProviderParam() = Field(hookInfo.commentImageSaveDownloadListener.listenerProviderParam.nameOrNull)
    }

    inner class DownloadInfoModule {
        val selfClass by weak {
            hookInfo.downloadInfo.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun url() = Field(hookInfo.downloadInfo.url.nameOrNull)

        fun getTargetFilePath() = Method(
            hookInfo.downloadInfo.getTargetFilePath.nameOrNull,
            hookInfo.downloadInfo.getTargetFilePath.parameters.valuesListOrNull
        )
    }

    inner class DigestUtilsModule {
        val selfClass by weak {
            hookInfo.digestUtils.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun md5Hex() = Method(
            hookInfo.digestUtils.md5Hex.nameOrNull,
            hookInfo.digestUtils.md5Hex.parameters.valuesListOrNull
        )
    }

    inner class UGFileUtilsKtModule {
        val selfClass by weak {
            hookInfo.ugFileUtils.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun context() = Field(hookInfo.ugFileUtils.context.nameOrNull)

        fun copyFile() = Method(
            hookInfo.ugFileUtils.copyFile.nameOrNull,
            hookInfo.ugFileUtils.copyFile.parameters.valuesListOrNull
        )

        fun getStorageDir() = Method(
            hookInfo.ugFileUtils.getStorageDir.nameOrNull,
            hookInfo.ugFileUtils.getStorageDir.parameters.valuesListOrNull
        )

        fun getExternalStorageDir() = Method(
            hookInfo.ugFileUtils.getExternalStorageDir.nameOrNull,
            hookInfo.ugFileUtils.getExternalStorageDir.parameters.valuesListOrNull
        )

        fun getImageUri() = Method(
            hookInfo.ugFileUtils.getImageUri.nameOrNull,
            hookInfo.ugFileUtils.getImageUri.parameters.valuesListOrNull
        )

        fun createUri() = Method(
            hookInfo.ugFileUtils.createUri.nameOrNull,
            hookInfo.ugFileUtils.createUri.parameters.valuesListOrNull
        )

        fun getAudioUri() = Method(
            hookInfo.ugFileUtils.getAudioUri.nameOrNull,
            hookInfo.ugFileUtils.getAudioUri.parameters.valuesListOrNull
        )
    }

    inner class TokenCertModule {
        val selfClass by weak {
            hookInfo.tokenCert.class_.nameOrNull
                ?.toClass(classLoader)
        }
    }

    inner class CommonItemViewModule {
        val selfClass by weak {
            hookInfo.commonItemView.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun setLeftText() = Method(
            hookInfo.commonItemView.setLeftText.nameOrNull,
            hookInfo.commonItemView.setLeftText.parameters.valuesListOrNull
        )

        fun setRightUIMode() = Method(
            hookInfo.commonItemView.setRightUiMode.nameOrNull,
            hookInfo.commonItemView.setRightUiMode.parameters.valuesListOrNull
        )

        fun setLeftIcon() = Method(
            hookInfo.commonItemView.setLeftIcon.nameOrNull,
            hookInfo.commonItemView.setLeftIcon.parameters.valuesListOrNull
        )

        fun setRightText() = Method(
            hookInfo.commonItemView.setRightText.nameOrNull,
            hookInfo.commonItemView.setRightText.parameters.valuesListOrNull
        )

        fun setLeftTextAndIcon() = Method(
            hookInfo.commonItemView.setLeftTextAndIcon.nameOrNull,
            hookInfo.commonItemView.setLeftTextAndIcon.parameters.valuesListOrNull
        )
    }

    inner class DouYinSettingNewVersionActivityModule {
        val selfClass by weak {
            hookInfo.douYinSettingNewVersionActivity.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun settingsScrollView() = Field(
            hookInfo.douYinSettingNewVersionActivity.settingsScrollView.nameOrNull
        )

        fun onResume() = Method(
            hookInfo.douYinSettingNewVersionActivity.onResume.nameOrNull,
            hookInfo.douYinSettingNewVersionActivity.onResume.parameters.valuesListOrNull
        )
    }

    inner class MainActivityModule {
        val selfClass by weak {
            "com.ss.android.ugc.aweme.main.MainActivity".toClassOrNull(classLoader)
        }

        fun onResume() = Method(
            "onResume",
            null
        )

        fun onNewIntent() = Method(
            "onNewIntent",
            listOf("android.content.Intent")
        )
    }

    inner class UserModule {
        val selfClass by weak {
            hookInfo.user.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun nickname() = Field(hookInfo.user.nickname.nameOrNull)
        fun uid() = Field(hookInfo.user.uid.nameOrNull)
    }

    inner class AwemeModule {
        val selfClass by weak {
            hookInfo.aweme.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun desc() = Field(hookInfo.aweme.desc.nameOrNull)

        fun author() = Field(hookInfo.aweme.author.nameOrNull)

        fun getAd() = Method(
            hookInfo.aweme.getAd.nameOrNull,
            hookInfo.aweme.getAd.parameters.valuesListOrNull
        )

        fun itemTitle() = Field(hookInfo.aweme.itemTitle.nameOrNull)

        fun duration() = Field(hookInfo.aweme.duration.nameOrNull)

        fun isNormalVideo() = Method(
            hookInfo.aweme.isNormalVideo.nameOrNull,
            hookInfo.aweme.isNormalVideo.parameters.valuesListOrNull
        )

        fun isEcomAweme() = Method(
            hookInfo.aweme.isEcomAweme.nameOrNull,
            hookInfo.aweme.isEcomAweme.parameters.valuesListOrNull
        )

        fun grouponLargeCard() = Field(hookInfo.aweme.grouponLargeCard.nameOrNull)

        fun isLive() = Method(
            hookInfo.aweme.isLive.nameOrNull,
            hookInfo.aweme.isLive.parameters.valuesListOrNull
        )

        fun isMultiImage() = Method(
            hookInfo.aweme.isMultiImage.nameOrNull,
            hookInfo.aweme.isMultiImage.parameters.valuesListOrNull
        )

        fun getVideo() = Method(
            hookInfo.aweme.getVideo.nameOrNull,
            hookInfo.aweme.getVideo.parameters.valuesListOrNull
        )

        fun images() = Field(hookInfo.aweme.images.nameOrNull)

        fun statistics() = Field(hookInfo.aweme.statistics.nameOrNull)

        fun getAid() = Method(
            hookInfo.aweme.getAid.nameOrNull,
            hookInfo.aweme.getAid.parameters.valuesListOrNull
        )
    }

    inner class AwemeStatisticsModule {
        val selfClass by weak {
            hookInfo.awemeStatistics.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun collectCount() = Field(hookInfo.awemeStatistics.collectCount.nameOrNull)

        fun commentCount() = Field(hookInfo.awemeStatistics.commentCount.nameOrNull)

        fun diggCount() = Field(hookInfo.awemeStatistics.diggCount.nameOrNull)

        fun shareCount() = Field(hookInfo.awemeStatistics.shareCount.nameOrNull)
    }

    inner class HeifDecoderModule {
        val selfClass by weak {
            hookInfo.heifDecoder.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun sBitmapFactory() = Field(hookInfo.heifDecoder.sBitmapFactory.nameOrNull)
    }

    inner class HeifBitmapFactoryImplModule {
        val selfClass by weak {
            hookInfo.heifBitmapFactoryImpl.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun decodeByteArray() = Method(
            hookInfo.heifBitmapFactoryImpl.decodeByteArray.nameOrNull,
            hookInfo.heifBitmapFactoryImpl.decodeByteArray.parameters.valuesListOrNull
        )
    }

    inner class DownLoadExecutorModule {
        val selfClass by weak {
            hookInfo.downLoadExecutor.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun execute() = Method(
            hookInfo.downLoadExecutor.execute.nameOrNull,
            hookInfo.downLoadExecutor.execute.parameters.valuesListOrNull
        )
    }

    inner class DownLoadTaskModule {
        val selfClass by weak {
            hookInfo.downLoadTask.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun getTargetFilePaths() = Method(
            hookInfo.downLoadTask.getTargetFilePaths.nameOrNull,
            hookInfo.downLoadTask.getTargetFilePaths.parameters.valuesListOrNull
        )
    }

    inner class DownloadLivePhotoExecutorModule {
        val selfClass by weak {
            hookInfo.downloadLivePhotoExecutor.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun encodeLivePhoto() = Method(
            hookInfo.downloadLivePhotoExecutor.encodeLivePhoto.nameOrNull,
            hookInfo.downloadLivePhotoExecutor.encodeLivePhoto.parameters.valuesListOrNull
        )
    }

    inner class SingleImageToMp4ComposerModule {
        val selfClass by weak {
            hookInfo.singleImageToMp4Composer.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun onLoad() = Method(
            hookInfo.singleImageToMp4Composer.onLoad.nameOrNull,
            hookInfo.singleImageToMp4Composer.onLoad.parameters.valuesListOrNull
        )
    }

    inner class MultiImageToMp4ComposerModule {
        val selfClass by weak {
            hookInfo.multiImageToMp4Composer.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun onLoad() = Method(
            hookInfo.multiImageToMp4Composer.onLoad.nameOrNull,
            hookInfo.multiImageToMp4Composer.onLoad.parameters.valuesListOrNull
        )

        fun imagePathList() = Field(
            hookInfo.multiImageToMp4Composer.imagePathList.nameOrNull
        )
    }

    inner class VideoModule {
        val selfClass by weak {
            hookInfo.video.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun getPlayAddr() = Method(
            hookInfo.video.getPlayAddr.nameOrNull,
            hookInfo.video.getPlayAddr.parameters.valuesListOrNull
        )

        fun hasSuffixWaterMark() = Field(hookInfo.video.hasSuffixWaterMark.nameOrNull)

        fun hasWaterMark() = Field(hookInfo.video.hasWaterMark.nameOrNull)

        fun downloadAddr() = Field(hookInfo.video.downloadAddr.nameOrNull)
    }

    inner class ImageUrlStructModule {
        val selfClass by weak {
            hookInfo.imageUrlStruct.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun watermarkFreeDownloadUrlList() = Field(
            hookInfo.imageUrlStruct.watermarkFreeDownloadUrlList.nameOrNull
        )

        fun urlList() = Field(
            hookInfo.imageUrlStruct.urlList.nameOrNull
        )

        fun downloadUrlList() = Field(
            hookInfo.imageUrlStruct.downloadUrlList.nameOrNull
        )

        fun video() = Field(
            hookInfo.imageUrlStruct.video.nameOrNull
        )
    }

    inner class FeedResponseHandlerModule {
        val selfClass by weak {
            hookInfo.feedResponseHandler.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun processAwemeList() = Method(
            hookInfo.feedResponseHandler.processAwemeList.nameOrNull,
            hookInfo.feedResponseHandler.processAwemeList.parameters.valuesListOrNull
        )
    }

    inner class CommentLongPressWhiteListProviderModule {
        val selfClass by weak {
            hookInfo.commentLongPressWhiteListProvider.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun buildWhiteList() = Method(
            hookInfo.commentLongPressWhiteListProvider.buildWhiteList.nameOrNull,
            hookInfo.commentLongPressWhiteListProvider.buildWhiteList.parameters.valuesListOrNull
        )
    }

    inner class MiscDownloadAddrUtilModule {
        val selfClass by weak {
            hookInfo.miscDownloadAddrUtil.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun getSuffixSceneDownloadAddr() = Method(
            hookInfo.miscDownloadAddrUtil.getSuffixSceneDownloadAddr.nameOrNull,
            hookInfo.miscDownloadAddrUtil.getSuffixSceneDownloadAddr.parameters.valuesListOrNull
        )
    }

    inner class DownloadActionModule {
        val selfClass by weak {
            hookInfo.downloadAction.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun startDownload() = Method(
            hookInfo.downloadAction.startDownload.nameOrNull,
            hookInfo.downloadAction.startDownload.parameters.valuesListOrNull
        )

        fun aweme() = Field(hookInfo.downloadAction.aweme.nameOrNull)
    }

    inner class ABTestServiceImplModule {
        val selfClass by weak {
            hookInfo.abTestServiceImpl.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun enableSaveImageToVideoLocalWaterMask() = Method(
            hookInfo.abTestServiceImpl.enableSaveImageToVideoLocalWaterMask.nameOrNull,
            hookInfo.abTestServiceImpl.enableSaveImageToVideoLocalWaterMask.parameters.valuesListOrNull
        )

        fun enableVEAddLiveVideoWaterMark() = Method(
            hookInfo.abTestServiceImpl.enableVEAddLiveVideoWaterMark.nameOrNull,
            hookInfo.abTestServiceImpl.enableVEAddLiveVideoWaterMark.parameters.valuesListOrNull
        )
    }

    inner class LppDownloadModuleModule {
        val selfClass by weak {
            hookInfo.lppDownloadModule.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun getVisibility() = Method(
            hookInfo.lppDownloadModule.getVisibility.nameOrNull,
            hookInfo.lppDownloadModule.getVisibility.parameters.valuesListOrNull
        )
    }

    companion object {
        private val TAG = DouyinPackage::class.simpleName

        @Volatile
        lateinit var instance: DouyinPackage

        private fun readHookInfo(context: Context): Configs.HookInfo {
            val androidId =
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown"
            val hookInfoFileName =
                "${AppProperties.PROJECT_APPLICATION_ID}-${androidId.hashCode().toUInt()}"
                    .hashCode().toHexString()
            YLog.debug("$TAG: hookInfoFileName: $hookInfoFileName")

            runCatching {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (!(hookInfoFile.isFile && hookInfoFile.canRead())) {
                    YLog.debug("$TAG: hookInfoFile is not a file or can not be read")
                    return@runCatching null
                }

                val hostAppPackageInfo =
                    context.packageManager.getPackageInfo(
                        AndroidAppHelper.currentPackageName(),
                        0
                    )
                val hostAppLastUpdateTime = hostAppPackageInfo.lastUpdateTime
                val hostAppVersionCode = hostAppPackageInfo.versionCode

                val moduleLastUpdateTime =
                    runCatching {
                        context.packageManager
                            .getPackageInfo(
                                AppProperties.PROJECT_APPLICATION_ID,
                                0
                            ).lastUpdateTime
                    }.getOrDefault(hostAppLastUpdateTime)

                val hookInfo =
                    FileInputStream(hookInfoFile).use {
                        runCatching {
                            Configs.HookInfo.parseFrom(it)
                        }.getOrNull() ?: Configs.HookInfo.newBuilder().build()
                    }

                if (hookInfo.lastUpdateTime >= moduleLastUpdateTime &&
                    hookInfo.lastUpdateTime >= hostAppLastUpdateTime &&
                    hookInfo.hostVersionCode == hostAppVersionCode &&
                    hookInfo.moduleVersionCode == BuildConfig.VERSION_CODE &&
                    hookInfo.moduleVersionName == BuildConfig.VERSION_NAME
                ) {
                    return hookInfo
                } else {
                    YLog.debug("$TAG: hookInfo is outdated, will re-generate")
                }
            }.onFailure {
                YLog.error("$TAG: failed to read hookInfo: ", it)
            }

            return initHookInfo(context).also {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (hookInfoFile.exists()) {
                    hookInfoFile.delete()
                }
                FileOutputStream(hookInfoFile).use { o ->
                    it.writeTo(o)
                }
            }
        }

        private fun initHookInfo(context: Context) = hookInfo {
            val hostAppClassLoader = context.classLoader
            val hostAppPackageInfo =
                context.packageManager.getPackageInfo(
                    AndroidAppHelper.currentPackageName(),
                    0
                )

            lastUpdateTime =
                maxOf(
                    hostAppPackageInfo.lastUpdateTime,
                    runCatching {
                        context.packageManager
                            .getPackageInfo(
                                AppProperties.PROJECT_NAMESPACE,
                                0
                            ).lastUpdateTime
                    }.getOrDefault(hostAppPackageInfo.lastUpdateTime)
                )
            moduleVersionCode = BuildConfig.VERSION_CODE
            moduleVersionName = BuildConfig.VERSION_NAME
            hostVersionCode = hostAppPackageInfo.versionCode
            generation = 0

            try {
                System.loadLibrary("dexkit")
            } catch (e: Throwable) {
                YLog.error("Failed to load DexKit native library: ${e.message}")
            }

            DexKitBridge.create(context.applicationInfo.sourceDir).use { bridge ->
                commentImageStruct =
                    commentImageStruct {
                        runCatching {
                            val cmtImgClsName =
                                "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                            val originUrlFieldName = "originUrl"
                            val downloadUrlFieldName = "downloadUrl"
                            val getDownloadUrlMethodData =
                                bridge
                                    .findMethod {
                                        matcher {
                                            declaredClass =
                                                "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                                            returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                            paramCount = 0
                                            addUsingField {
                                                name = "downloadUrl"
                                            }
                                        }
                                    }.singleOrNull() ?: run {
                                    YLog.error(
                                        "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                    )
                                    return@commentImageStruct
                                }

                            class_ =
                                class_ {
                                    name = cmtImgClsName
                                }
                            originUrl =
                                field {
                                    name = originUrlFieldName
                                }
                            downloadUrl =
                                field {
                                    name = downloadUrlFieldName
                                }
                            getDownloadUrl =
                                method {
                                    name = getDownloadUrlMethodData.methodName
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            values.addAll(getDownloadUrlMethodData.paramTypeNames)
                                        }
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                urlModel =
                    urlModel {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.base.model.UrlModel"
                            }
                        urlList =
                            field {
                                name = "urlList"
                            }
                    }

                comment =
                    comment {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.comment.model.Comment"
                            }
                        emoji =
                            field {
                                name = "emoji"
                            }
                        imageList =
                            field {
                                name = "imageList"
                            }
                        commentAudio =
                            field {
                                name = "commentAudio"
                            }
                    }

                commentAudioStruct =
                    commentAudioStruct {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.comment.model.CommentAudioStruct"
                            }
                        content =
                            field {
                                name = "content"
                            }
                    }

                emoji =
                    emoji {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.emoji.model.Emoji"
                            }
                        animateUrl =
                            method {
                                name = "animateUrl"
                            }
                    }

                commentActionParams =
                    commentActionParams {
                        runCatching {
                            val cmtActionParamsClsName =
                                "com.ss.android.ugc.aweme.comment.CommentActionParams"
                            val commentFieldName =
                                cmtActionParamsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.comment.model.Comment"
                                    }?.self
                                    ?.name
                            val imageFieldName =
                                cmtActionParamsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = Int::class
                                    }?.self
                                    ?.name
                            if (commentFieldName == null || imageFieldName == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@commentActionParams
                            }

                            class_ =
                                class_ {
                                    name = cmtActionParamsClsName
                                }
                            comment =
                                field {
                                    name = commentFieldName
                                }
                            imageIndex =
                                field {
                                    name = imageFieldName
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                commentLongPressItemModel =
                    commentLongPressItemModel {
                        runCatching {
                            val commentLongPressItemModelClsName =
                                "com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel"
                            val commentActionParamsFieldName =
                                commentLongPressItemModelClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                                    }?.self
                                    ?.name

                            if (commentActionParamsFieldName == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@commentLongPressItemModel
                            }

                            class_ =
                                class_ {
                                    name = commentLongPressItemModelClsName
                                }
                            commentActionParams =
                                field {
                                    name = commentActionParamsFieldName
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                saveImageActionItem =
                    saveImageActionItem {
                        runCatching {
                            val saveImageActionItemClsName =
                                "com.ss.android.ugc.aweme.comment.manager.longclickaction.actions.SaveImageActionItem"
                            // SaveImageActionItem extends CommentLongPressItemModel, ensure commentLongPressItemModel is populated first!
                            val cmtActionParamsFieldName =
                                this@hookInfo.commentLongPressItemModel.commentActionParams?.name
                            val saveImageActionParamsFieldName =
                                saveImageActionItemClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                                    }?.self
                                    ?.name
                            val onClickMethodData = bridge
                                .findMethod {
                                    matcher {
                                        modifiers = Modifier.STATIC + Modifier.FINAL + Modifier.PUBLIC
                                        returnType = "java.lang.Object"
                                        params {
                                            count = 1
                                        }
                                        addUsingString("bpea-comment_save_image_to_album")
                                    }
                                }.singleOrNull()
                            val onClickHostItemFieldName =
                                onClickMethodData?.declaredClassName?.toClass(hostAppClassLoader)?.resolve()?.firstFieldOrNull {
                                    type = Object::class
                                }?.self?.name
                            val isVisibleMethodData = bridge
                                .findMethod {
                                    matcher {
                                        modifiers = Modifier.PUBLIC or Modifier.FINAL
                                        declaredClass = saveImageActionItemClsName
                                        returnType = "boolean"
                                        usingFields {
                                            add {
                                                field {
                                                    cmtActionParamsFieldName?.let {
                                                        name = it
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }.singleOrNull()
                            if (cmtActionParamsFieldName == null || saveImageActionParamsFieldName == null || onClickMethodData == null ||
                                onClickHostItemFieldName == null ||
                                isVisibleMethodData == null
                            ) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@saveImageActionItem
                            }

                            class_ =
                                class_ {
                                    name = saveImageActionItemClsName
                                }
                            cmtActionParams =
                                field {
                                    name = cmtActionParamsFieldName
                                }
                            saveImgActionParams =
                                field {
                                    name = saveImageActionParamsFieldName
                                }
                            onClickExecutor = saveImageActionItemOnClickExecutor {
                                class_ = class_ {
                                    name = onClickMethodData.className
                                }
                                onClick = method {
                                    name = onClickMethodData.methodName
                                    parameters = MethodKt.parameters {
                                        values.clear()
                                        values.addAll(onClickMethodData.paramTypeNames)
                                    }
                                }
                                hostItem = field {
                                    name = onClickHostItemFieldName
                                }
                            }
                            isVisible = method {
                                name = isVisibleMethodData.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(isVisibleMethodData.paramTypeNames)
                                }
                            }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                commentExtensionKt = commentExtensionKt {
                    runCatching {
                        bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                declaredClass = "com.ss.android.ugc.aweme.comment.util.CommentExtensionsKt"
                                returnType = "boolean"
                                params {
                                    add("com.ss.android.ugc.aweme.comment.model.Comment")
                                    add("int")
                                }
                                invokeMethods {
                                    add {
                                        declaredClass =
                                            "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                                        returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                        paramCount = 0
                                        addUsingField {
                                            name = "downloadUrl"
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()
                            ?.also { match ->
                                class_ =
                                    class_ {
                                        name = match.className
                                    }
                                hasValidImageUrl =
                                    method {
                                        name = match.methodName
                                        parameters =
                                            MethodKt.parameters {
                                                values.clear()
                                                values.addAll(match.paramTypeNames)
                                            }
                                    }
                            } ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@commentExtensionKt
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                listenerProviderParam = listenerProviderParam {
                    runCatching {
                        val clsData = bridge.findClass {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                fields {
                                    add {
                                        type {
                                            descriptor = "Landroid/content/Context;"
                                        }
                                    }
                                    add {
                                        type {
                                            descriptor = "Lcom/bytedance/bpea/cert/token/TokenCert;"
                                        }
                                    }
                                }
                                method {
                                    name = "toString"
                                    usingStrings {
                                        add("ListenerProviderParam(context=")
                                    }
                                }
                            }
                        }.singleOrNull()

                        val clsName = clsData?.name

                        val contextFieldName = clsName
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                type = "android.content.Context"
                            }?.self?.name

                        val certFieldName = clsName
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                type = "com.bytedance.bpea.cert.token.TokenCert"
                            }?.self?.name

                        if (clsName == null || contextFieldName == null || certFieldName == null) {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated fields"
                            )
                            return@listenerProviderParam
                        }

                        class_ = class_ {
                            name = clsName
                        }
                        this.context = field {
                            name = contextFieldName
                        }
                        cert = field {
                            name = certFieldName
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                commentImageSaveDownloadListener =
                    commentImageSaveDownloadListener {
                        runCatching {
                            val onSuccessedMethodData = bridge
                                .findMethod {
                                    matcher {
                                        name = "onSuccessed"
                                        modifiers = Modifier.FINAL + Modifier.PUBLIC
                                        returnType = "void"
                                        params {
                                            add("com.ss.android.socialbase.downloader.model.DownloadInfo")
                                        }
                                        usingStrings {
                                            add("/douyin/comment")
                                            add("comment_")
                                        }
                                        invokeMethods {
                                            add {
                                                descriptor =
                                                    "Lcom/bytedance/android/ug/UGFileUtilsKt;->copyFile(Ljava/lang/String;Ljava/lang/String;Lcom/bytedance/bpea/cert/token/TokenCert;)Z"
                                            }
                                        }
                                    }
                                }.singleOrNull()

                            val clsName = onSuccessedMethodData?.declaredClassName

                            val notifyResultMethod =
                                clsName?.toClass(hostAppClassLoader)?.resolve()
                                    ?.firstMethodOrNull {
                                        modifiers(Modifiers.PUBLIC, Modifiers.FINAL)
                                        parameters(Context::class, Boolean::class)
                                        parameterCount = 2
                                        superclass()
                                    }?.self
                            val listenerProviderParamFieldName = clsName?.toClass(hostAppClassLoader)?.resolve()?.firstFieldOrNull {
                                type = this@hookInfo.listenerProviderParam.class_.nameOrNull
                            }?.self?.name
                            if (onSuccessedMethodData == null || notifyResultMethod == null || listenerProviderParamFieldName == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@commentImageSaveDownloadListener
                            }

                            class_ =
                                class_ {
                                    name = clsName
                                }
                            onSuccessed =
                                method {
                                    name = onSuccessedMethodData.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            values.addAll(onSuccessedMethodData.paramTypeNames)
                                        }
                                }
                            notifyResult =
                                method {
                                    name = notifyResultMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            notifyResultMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            listenerProviderParam = field {
                                name = listenerProviderParamFieldName
                            }
                            return@commentImageSaveDownloadListener
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                downloadInfo =
                    downloadInfo {
                        class_ =
                            class_ {
                                name = "com.ss.android.socialbase.downloader.model.DownloadInfo"
                            }
                        url =
                            field {
                                name = "url"
                            }
                        getTargetFilePath =
                            method {
                                name = "getTargetFilePath"
                            }
                    }

                digestUtils =
                    digestUtils {
                        runCatching {
                            val digestUtilsClsName = "com.bytedance.common.utility.DigestUtils"
                            val md5HexFieldMethod =
                                digestUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "md5Hex"
                                        returnType = String::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC)
                                        parameters(String::class)
                                    }?.self
                            if (md5HexFieldMethod == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@digestUtils
                            }

                            class_ =
                                class_ {
                                    name = digestUtilsClsName
                                }
                            md5Hex =
                                method {
                                    name = md5HexFieldMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            md5HexFieldMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                ugFileUtils =
                    uGFileUtilsKt {
                        runCatching {
                            val ugFileUtilsClsName = "com.bytedance.android.ug.UGFileUtilsKt"
                            val copyFileMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "copyFile"
                                        returnType = Boolean::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(
                                            String::class,
                                            String::class,
                                            "com.bytedance.bpea.cert.token.TokenCert"
                                        )
                                        parameterCount = 3
                                    }?.self
                            val getStorageDirMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "getStorageDir"
                                        returnType = String::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(String::class, Boolean::class)
                                        parameterCount = 2
                                    }?.self
                            val getExternalStorageDirectoryMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "getExternalStorageDirectory"
                                        returnType = String::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(String::class, Boolean::class)
                                        parameterCount = 2
                                    }?.self
                            val getImageUriMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "getImageUri"
                                        returnType = android.net.Uri::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(
                                            Context::class,
                                            String::class,
                                            String::class,
                                            String::class,
                                            "com.bytedance.bpea.cert.token.TokenCert"
                                        )
                                        parameterCount = 5
                                    }?.self
                            val createUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                                name = "createUri"
                                returnType = android.net.Uri::class
                                modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                parameters(
                                    String::class,
                                    Boolean::class,
                                    Array<android.net.Uri>::class,
                                    "com.bytedance.bpea.cert.token.TokenCert"
                                )
                                parameterCount = 4
                            }?.self
                            val getAudioUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                                name = "getAudioUri"
                                parameters(
                                    Context::class,
                                    String::class,
                                    String::class,
                                    String::class,
                                    "com.bytedance.bpea.cert.token.TokenCert"
                                )
                            }?.self
                            if (copyFileMethod == null || getStorageDirMethod == null || getExternalStorageDirectoryMethod == null ||
                                getImageUriMethod == null || createUriMethod == null || getAudioUriMethod == null
                            ) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@uGFileUtilsKt
                            }

                            class_ =
                                class_ {
                                    name = ugFileUtilsClsName
                                }
                            this.context =
                                field {
                                    name = "context"
                                }
                            copyFile =
                                method {
                                    name = copyFileMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            copyFileMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            getStorageDir =
                                method {
                                    name = getStorageDirMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            getStorageDirMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            getExternalStorageDir =
                                method {
                                    name = getExternalStorageDirectoryMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            getExternalStorageDirectoryMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            getImageUri =
                                method {
                                    name = getImageUriMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            getImageUriMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            createUri = method {
                                name = createUriMethod.name
                                parameters =
                                    MethodKt.parameters {
                                        values.clear()
                                        createUriMethod.parameterTypes.forEach { paramType ->
                                            values.add(paramType.name)
                                        }
                                    }
                            }
                            getAudioUri = method {
                                name = getAudioUriMethod.name
                                parameters =
                                    MethodKt.parameters {
                                        values.clear()
                                        getAudioUriMethod.parameterTypes.forEach { paramType ->
                                            values.add(paramType.name)
                                        }
                                    }
                            }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                tokenCert =
                    tokenCert {
                        class_ =
                            class_ {
                                name = "com.bytedance.bpea.cert.token.TokenCert"
                            }
                    }

                commonItemView = commonItemView {
                    class_ = class_ {
                        name = "com.bytedance.ies.dmt.ui.common.views.CommonItemView"
                    }
                    setLeftText = method {
                        name = "setLeftText"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                        }
                    }
                    setRightUiMode = method {
                        name = "setRightUIMode"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("int")
                        }
                    }
                    setLeftIcon = method {
                        name = "setLeftIcon"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("int")
                        }
                    }
                    setRightText = method {
                        name = "setRightText"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                        }
                    }
                    setLeftTextAndIcon = method {
                        name = "setLeftTextAndIcon"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                            values.add("int")
                        }
                    }
                }

                douYinSettingNewVersionActivity = douYinSettingNewVersionActivity {
                    runCatching {
                        val dySettingsNewVersionActivityClsName = "com.ss.android.ugc.aweme.setting.ui.DouYinSettingNewVersionActivity"
                        val settingsScrollViewFieldName = dySettingsNewVersionActivityClsName.toClass(
                            hostAppClassLoader
                        ).resolve().firstFieldOrNull {
                            type = "com.ss.android.ugc.aweme.setting.ui.SettingNestedScrollView"
                        }?.self?.name

                        if (settingsScrollViewFieldName == null) {
                            YLog.error("$TAG: Unable to populate config, settingsScrollViewFieldName is null")
                            return@runCatching
                        }

                        class_ = class_ {
                            name = dySettingsNewVersionActivityClsName
                        }
                        settingsScrollView = field {
                            name = settingsScrollViewFieldName
                        }
                        onResume = method {
                            name = "onResume"
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                user = user {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.profile.model.User"
                    }
                    nickname = field {
                        name = "nickname"
                    }
                    uid = field {
                        name = "uid"
                    }
                }

                aweme = aweme {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.feed.model.Aweme"
                    }
                    desc = field {
                        name = "desc"
                    }
                    author = field {
                        name = "author"
                    }
                    getAd = method {
                        name = "getAd"
                    }
                    itemTitle = field {
                        name = "itemTitle"
                    }
                    duration = field {
                        name = "duration"
                    }
                    isNormalVideo = method {
                        name = "isNormalVideo"
                    }
                    isEcomAweme = method {
                        name = "isEcomAweme"
                    }
                    grouponLargeCard = field {
                        name = "grouponLargeCard"
                    }
                    isLive = method {
                        name = "isLive"
                    }
                    isMultiImage = method {
                        name = "isMultiImage"
                    }
                    getVideo = method {
                        name = "getVideo"
                    }
                    images = field {
                        name = "images"
                    }
                    statistics = field {
                        name = "statistics"
                    }
                    getAid = method {
                        name = "getAid"
                    }
                }

                awemeStatistics = awemeStatistics {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.feed.model.AwemeStatistics"
                    }
                    collectCount = field {
                        name = "collectCount"
                    }
                    commentCount = field {
                        name = "commentCount"
                    }
                    diggCount = field {
                        name = "diggCount"
                    }
                    shareCount = field {
                        name = "shareCount"
                    }
                }

                heifDecoder = heifDecoder {
                    class_ = class_ {
                        name = "com.bytedance.fresco.heif.HeifDecoder"
                    }
                    sBitmapFactory = field {
                        name = "sBitmapFactory"
                    }
                }

                heifBitmapFactoryImpl = heifBitmapFactoryImpl {
                    class_ = class_ {
                        name = "com.bytedance.fresco.heif.HeifBitmapFactoryImpl"
                    }
                    decodeByteArray = method {
                        name = "decodeByteArray"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.addAll(
                                listOf(
                                    "[B",
                                    "int",
                                    "int",
                                    "android.graphics.BitmapFactory\$Options"
                                )
                            )
                        }
                    }
                }

                downLoadExecutor = downLoadExecutor {
                    runCatching {
                        val executeMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "boolean"
                                paramCount = 1
                                usingStrings {
                                    add("/douyin")
                                    add("share_")
                                    add(".png")
                                    add("DownLoadExecutor")
                                }
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/bytedance/android/ug/UGFileUtilsKt;->getExternalStorageDirectory(Ljava/lang/String;Z)Ljava/lang/String;"
                                    }
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@downLoadExecutor
                        }

                        class_ = class_ {
                            name = executeMethodData.className
                        }
                        execute = method {
                            name = executeMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(executeMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                downLoadTask = downLoadTask {
                    runCatching {
                        val downloadTaskClassName = this@hookInfo.downLoadExecutor.execute.parameters.valuesListOrNull?.firstOrNull()
                            ?: run {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated classes"
                                )
                                return@downLoadTask
                            }
                        val getTargetFilePathsMethodData = bridge.findMethod {
                            matcher {
                                declaredClass = downloadTaskClassName
                                returnType = "java.util.List"
                            }
                        }.singleOrNull()

                        if (getTargetFilePathsMethodData == null) {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@downLoadTask
                        }

                        class_ = class_ {
                            name = downloadTaskClassName
                        }
                        getTargetFilePaths = method {
                            name = getTargetFilePathsMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getTargetFilePathsMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                downloadLivePhotoExecutor = downloadLivePhotoExecutor {
                    runCatching {
                        val encodeLivePhotoMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "boolean"
                                usingStrings {
                                    add("DownloadLiveExecutor")
                                    add("encode live photo isFinish: ")
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@downloadLivePhotoExecutor
                        }

                        class_ = class_ {
                            name = encodeLivePhotoMethodData.className
                        }
                        encodeLivePhoto = method {
                            name = encodeLivePhotoMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(encodeLivePhotoMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                singleImageToMp4Composer = singleImageToMp4Composer {
                    runCatching {
                        val onLoadMethodData = bridge.findMethod {
                            matcher {
                                name = "onLoad"
                                usingStrings {
                                    add("[onLoad] failed, cause path not exist")
                                }
                                invokeMethods {
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/utils/FileUtils;->checkFileExists(Ljava/lang/String;)Z"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService;->convertImgToMp4(Landroid/content/Context;Landroidx/lifecycle/LifecycleOwner;Ljava/lang/String;Ljava/lang/String;ZJLjava/lang/String;Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService\$OnMuxImgToMp4Callback;)V"
                                    }
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@singleImageToMp4Composer
                        }

                        class_ = class_ {
                            name = onLoadMethodData.className
                        }
                        onLoad = method {
                            name = onLoadMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onLoadMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                multiImageToMp4Composer = multiImageToMp4Composer {
                    runCatching {
                        val onLoadMethodData = bridge.findMethod {
                            matcher {
                                name = "onLoad"
                                usingStrings {
                                    add("images file not exist!")
                                }
                                invokeMethods {
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/utils/FileUtils;->checkFileExists(Ljava/lang/String;)Z"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService;->convertImgListToMp4UseMusicUrl(Landroid/app/Activity;Landroidx/lifecycle/LifecycleOwner;Ljava/util/List;Lcom/ss/android/ugc/aweme/music/model/Music;ZZLjava/lang/String;ZLkotlin/jvm/functions/Function1;)V"
                                    }
                                }
                            }
                        }.singleOrNull()

                        val imagePathListFieldName = onLoadMethodData?.className
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                genericType = List::class.parameterizedBy(
                                    List::class.parameterizedBy(
                                        String::class.toTypeMatcher()
                                    )
                                )
                            }?.self?.name

                        if (onLoadMethodData == null || imagePathListFieldName == null) {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods or fields"
                            )
                            return@multiImageToMp4Composer
                        }

                        class_ = class_ {
                            name = onLoadMethodData.className
                        }
                        onLoad = method {
                            name = onLoadMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onLoadMethodData.paramTypeNames)
                            }
                        }
                        imagePathList = field {
                            name = imagePathListFieldName
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                video = video {
                    runCatching {
                        val getPlayAddrMethodData = bridge.findMethod {
                            matcher {
                                declaredClass = "com.ss.android.ugc.aweme.feed.model.Video"
                                returnType = "com.ss.android.ugc.aweme.feed.model.VideoUrlModel"
                                usingFields {
                                    add {
                                        name = "_playAddr"
                                    }
                                    add {
                                        name = "_playAddrH265"
                                    }
                                }
                            }
                        }.singleOrNull { methodData ->
                            methodData.usingFields.none {
                                it.field.fieldName == "ratio"
                            }
                        } ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@video
                        }

                        class_ = class_ {
                            name = getPlayAddrMethodData.className
                        }
                        getPlayAddr = method {
                            name = getPlayAddrMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getPlayAddrMethodData.paramTypeNames)
                            }
                        }
                        hasSuffixWaterMark = field {
                            name = "hasSuffixWaterMark"
                        }
                        hasWaterMark = field {
                            name = "hasWaterMark"
                        }
                        downloadAddr = field {
                            name = "downloadAddr"
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                feedResponseHandler = feedResponseHandler {
                    runCatching {
                        bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC + Modifier.STATIC
                                returnType = "void"
                                params {
                                    add("int")
                                    add("java.lang.String")
                                    add("java.util.List")
                                }
                                invokeMethods {
                                    add {
                                        descriptor = "Ljava/util/List;->size()I"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->setRequestId(Ljava/lang/String;)V"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAd()Z"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/awemeservice/api/IAwemeService;->updateAweme(Lcom/ss/android/ugc/aweme/feed/model/Aweme;I)Lcom/ss/android/ugc/aweme/feed/model/Aweme;"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->isLive()Z"
                                    }
                                }
                            }
                        }.singleOrNull()?.also { match ->
                            class_ = class_ {
                                name = match.className
                            }
                            processAwemeList = method {
                                name = match.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(match.paramTypeNames)
                                }
                            }
                        } ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@feedResponseHandler
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                commentLongPressWhiteListProvider = commentLongPressWhiteListProvider {
                    runCatching {
                        val buildWhiteListMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC
                                returnType = "java.util.Set"
                                params {
                                    add("com.ss.android.ugc.aweme.comment.CommentActionParams")
                                }
                                usingStrings {
                                    add("custom")
                                    add("default")
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@commentLongPressWhiteListProvider
                        }

                        class_ = class_ {
                            name = buildWhiteListMethodData.className
                        }
                        buildWhiteList = method {
                            name = buildWhiteListMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(buildWhiteListMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                miscDownloadAddrUtil = miscDownloadAddrUtil {
                    runCatching {
                        val getSuffixSceneDownloadAddrMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                returnType {
                                    descriptor = "Lcom/ss/android/ugc/aweme/feed/model/VideoUrlModel;"
                                }
                                params {
                                    add("com.ss.android.ugc.aweme.feed.model.Aweme")
                                }
                                opCodes(
                                    OpCodesMatcher().opNames(
                                        listOf("const-class")
                                    )
                                )
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getVideo()Lcom/ss/android/ugc/aweme/feed/model/Video;"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/bytedance/mt/protector/impl/GsonProtectorUtils;->fromJson(Lcom/google/gson/Gson;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"
                                    }
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@miscDownloadAddrUtil
                        }

                        class_ = class_ {
                            name = getSuffixSceneDownloadAddrMethodData.className
                        }
                        getSuffixSceneDownloadAddr = method {
                            name = getSuffixSceneDownloadAddrMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getSuffixSceneDownloadAddrMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                downloadAction = downloadAction {
                    runCatching {
                        val downloadActionClsName = "com.ss.android.ugc.aweme.share.improve.action.DownloadAction"
                        val resolvedClass = downloadActionClsName
                            .toClass(hostAppClassLoader)
                            .resolve()

                        val startDownloadMethod = resolvedClass.firstMethodOrNull {
                            modifiers(Modifiers.PUBLIC, Modifiers.FINAL)
                            parameters(
                                "com.ss.android.ugc.aweme.sharer.ui.SharePackage"
                            )
                        }?.self

                        val awemeField = resolvedClass.firstFieldOrNull {
                            type = "com.ss.android.ugc.aweme.feed.model.Aweme"
                        }?.self

                        if (startDownloadMethod == null || awemeField == null) {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods or fields"
                            )
                            return@downloadAction
                        }

                        class_ = class_ {
                            name = downloadActionClsName
                        }
                        startDownload = method {
                            name = startDownloadMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                startDownloadMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        aweme = field {
                            name = awemeField.name
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                imageUrlStruct = imageUrlStruct {
                    class_ = class_ {
                        name = "com.ss.ugc.aweme.ImageUrlStruct"
                    }
                    watermarkFreeDownloadUrlList = field {
                        name = "watermarkFreeDownloadUrlList"
                    }
                    urlList = field {
                        name = "urlList"
                    }
                    downloadUrlList = field {
                        name = "downloadUrlList"
                    }
                    video = field {
                        name = "video"
                    }
                }

                abTestServiceImpl = aBTestServiceImpl {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.servicimpl.ABTestServiceImpl"
                    }
                    enableSaveImageToVideoLocalWaterMask = method {
                        name = "enableSaveImageToVideoLocalWaterMask"
                    }
                    enableVEAddLiveVideoWaterMark = method {
                        name = "enableVEAddLiveVideoWaterMark"
                    }
                }

                lppDownloadModule = lppDownloadModule {
                    runCatching {
                        val lppDownloadModuleClsName =
                            "com.ss.android.ugc.aweme.feed.long_press_panel.modules.business.homepage.LppDownloadModule"
                        val getVisibilityMethod = lppDownloadModuleClsName
                            .toClass(hostAppClassLoader)
                            .resolve()
                            .firstMethodOrNull {
                                modifiers(Modifiers.PUBLIC, Modifiers.FINAL)
                                parameters(
                                    "com.ss.android.ugc.aweme.feed.long_press_panel.model.LongPressPanelParams"
                                )
                                returnType = Int::class.java
                            }?.self

                        if (getVisibilityMethod == null) {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.java.enclosingClass?.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@lppDownloadModule
                        }

                        class_ = class_ {
                            name = lppDownloadModuleClsName
                        }
                        getVisibility = method {
                            name = getVisibilityMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                getVisibilityMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }
            }
        }
    }
}
