package io.github.twyora.douyinenhancer.hook

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
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
                Instrumentation::class.resolve().firstMethod {
                    name = "callApplicationOnCreate"
                    parameters(Application::class)
                }.hook {
                    before {
                        val context = args[0] as? Context ?: return@before

                        // hook main process only; skip plugin sub-processes
                        if (appInfo.sourceDir != context.applicationInfo.sourceDir) {
                            return@before
                        }

                        FastKVConfigManager.init(context)
                        // load cached HookInfo and run hooks when app context is available
                        DouyinPackage(appClassLoader!!, context)

                        YLog.info(
                            "verbose logging disabled is ${
                                FastKVConfigManager.module.getBoolean(
                                    ModuleKey.DISABLE_VERBOSE_LOGS,
                                    false
                                )
                            }"
                        )

                        HookerRegistry.mainProcessHookers.forEach {
                            loadHooker(it)
                        }
                    }
                }
            }
        }
    }
}
