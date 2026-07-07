package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod

object VideoDownloadHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(name = mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.miscDownloadAddrUtil.selfClass?.resolveMethod(
                packageInstance.miscDownloadAddrUtil.getSuffixSceneDownloadAddr()
            )?.hook {
                before {
                    val aweme = args[0] ?: return@before

                    val playAddr = aweme.invokeMethod<Any>(
                        packageInstance.aweme.getVideo()
                    )?.invokeMethod<Any>(
                        packageInstance.video.getPlayAddr()
                    )
                    if (playAddr != null) {
                        YLog.debug(
                            "$TAG: playAddr urlList: ${
                                playAddr.getField<List<String>>(
                                    packageInstance.urlModel.urlList()
                                ) ?: "null"
                            }"
                        )
                        result = playAddr
                    }
                }
            }
        }
    }
}