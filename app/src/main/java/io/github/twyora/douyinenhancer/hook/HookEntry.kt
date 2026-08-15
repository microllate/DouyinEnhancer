package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    private val TAG = this::class.simpleName

    override fun onInit() = YukiHookAPI.configs {
        debugLog {
            tag = "DouyinEnhancer"
        }
        isDebug = false
    }

    override fun onHook() = encase {
        loadApp(name = "com.ss.android.ugc.aweme") {
            withProcess(mainProcessName) {
                onAppLifecycle {
                    onCreate {
                        // hook main process only; skip plugin sub-processes
                        if (appInfo.sourceDir != this.applicationInfo.sourceDir) {
                            return@onCreate
                        }

                        FastKVConfigManager.init(this)
                        // load cached HookInfo and run hooks when app context is available
                        DouyinPackage.init(this.classLoader, this)

                        val verboseDisabled = FastKVConfigManager.module.getBoolean(
                            ModuleKey.DISABLE_VERBOSE_LOGS,
                            false
                        )
                        YLog.info("$TAG: verbose log disabling is $verboseDisabled")

                        HookerRegistry.mainProcessHookers.forEach {
                            loadHooker(it)
                        }
                    }
                }
            }
        }
    }
}
