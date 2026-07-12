package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.MiscKey
import io.github.twyora.douyinenhancer.config.key.RecommendedFeedFilterKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object RecommendedFeedHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val hiddenFeaturesEnabled
        get() = FastKVConfigManager.settings.getBoolean(MiscKey.ENABLE_HIDDEN_FEATURES, false)

    private val blockAdEnabled
        get() = FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.BLOCK_AD, false) && hiddenFeaturesEnabled

    private val blockEcomEnabled
        get() = FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.BLOCK_ECOM, false) && hiddenFeaturesEnabled

    private val blockGrouponLargeCardEnabled
        get() = FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.BLOCK_GROUPON, false) && hiddenFeaturesEnabled

    private val blockLiveEnabled
        get() = FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.BLOCK_LIVE, false) && hiddenFeaturesEnabled

    private val blockMultiImageEnabled
        get() = FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.BLOCK_MULTI_IMAGE, false) && hiddenFeaturesEnabled

    private val hideShortDurationLimit
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.SHORT_DURATION_LIMIT, 0)

    private val hideLongDurationLimit
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.LONG_DURATION_LIMIT, Int.MAX_VALUE)

    private val hideCollectCountMin
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.COLLECT_COUNT_MIN, 0)

    private val hideCollectCountMax
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.COLLECT_COUNT_MAX, Int.MAX_VALUE)

    private val hideCommentCountMin
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.COMMENT_COUNT_MIN, 0)

    private val hideCommentCountMax
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.COMMENT_COUNT_MAX, Int.MAX_VALUE)

    private val hideDiggCountMin
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.DIGG_COUNT_MIN, 0)

    private val hideDiggCountMax
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.DIGG_COUNT_MAX, Int.MAX_VALUE)

    private val hideShareCountMin
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.SHARE_COUNT_MIN, 0)

    private val hideShareCountMax
        get() = FastKVConfigManager.settings.getInt(RecommendedFeedFilterKey.SHARE_COUNT_MAX, Int.MAX_VALUE)


    private val kwdFilterTitleRegexMode by lazy {
        FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.TITLE_REGEX_MODE, false)
    }
    private val kwdFilterTitleRegexes by lazy {
        val titleList = FastKVConfigManager.settings.getStringSet(RecommendedFeedFilterKey.TITLE_KEYWORDS, null)
        if (kwdFilterTitleRegexMode) {
            titleList?.map {
                it.toRegex()
            }
        } else {
            titleList?.map {
                Regex.escape(it).toRegex()
            }
        }
    }

    private val kwdFilterAuthorUid by lazy {
        FastKVConfigManager.settings.getStringSet(RecommendedFeedFilterKey.AUTHOR_UID_KEYWORDS, null)
    }

    private val kwdFilterAuthorNicknames by lazy {
        FastKVConfigManager.settings.getStringSet(RecommendedFeedFilterKey.AUTHOR_NICKNAME_KEYWORDS, null)
    }

    private val kwdFilterDescRegexMode by lazy {
        FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.DESC_REGEX_MODE, false)
    }
    private val kwdFilterDescRegexes by lazy {
        val descList = FastKVConfigManager.settings.getStringSet(RecommendedFeedFilterKey.DESC_KEYWORDS, null)
        if (kwdFilterDescRegexMode) {
            descList?.map { it.toRegex() }
        } else {
            descList?.map { Regex.escape(it).toRegex() }
        }
    }

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(RecommendedFeedFilterKey.MAIN_SWITCH, false)) {
            return
        }

        val packageInstance = DouyinPackage.instance

        packageInstance.feedResponseHandler.selfClass?.resolveMethod(
            packageInstance.feedResponseHandler.processAwemeList()
        )?.hook {
            before {
                val awemeList = args[2] as? MutableList<*> ?: return@before

                val iter = awemeList.iterator()
                while (iter.hasNext()) {
                    val awemeObj = iter.next() ?: continue

                    if (blockAdEnabled && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.getAd()) == true) {
                        YLog.debug("$TAG: filtered by ad")
                        iter.remove()
                        continue
                    } else if (blockEcomEnabled && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isEcomAweme()) == true) {
                        // NOTE: this filter logic has not been rigorously verified
                        YLog.debug("$TAG: filtered by ecom aweme")
                        iter.remove()
                        continue
                    } else if (blockGrouponLargeCardEnabled && awemeObj.getField<Any?>(
                            packageInstance.aweme.grouponLargeCard()
                        ) != null
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        YLog.debug("$TAG: filtered by groupon large card")
                        iter.remove()
                        continue
                    } else if (blockLiveEnabled && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isLive()) == true) {
                        // NOTE: this filter logic has not been rigorously verified
                        YLog.debug("$TAG: filtered by live")
                        iter.remove()
                        continue
                    } else if (blockMultiImageEnabled &&
                        awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isMultiImage()) == true
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        YLog.debug("$TAG: filtered by multi image")
                        iter.remove()
                        continue
                    } else if (run {
                            if (hideShortDurationLimit > hideLongDurationLimit) {
                                return@run false
                            }

                            if (awemeObj.invokeMethod<Boolean?>(
                                    packageInstance.aweme.isNormalVideo()
                                ) == false
                            ) {
                                return@run false
                            }

                            val duration = awemeObj.getField<Int?>(
                                packageInstance.aweme.duration()
                            ) ?: return@run false

                            return@run duration != 0 && (duration !in hideShortDurationLimit..hideLongDurationLimit)
                        }
                    ) {
                        YLog.debug("$TAG: filtered by duration")
                        iter.remove()
                        continue
                    } else if (shouldFilterByInteractionStats(awemeObj)) {
                        //iter.remove()
                        continue
                    } else if (shouldFilterByKeyword(awemeObj)) {
                        iter.remove()
                        continue
                    }
                }
            }
        }?.result {
            onConductFailure { param, throwable ->
                YLog.error("$TAG: Feed hook runtime error", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: Failed to hook feed method", throwable)
            }
            onHooked {
                YLog.info("$TAG: Feed hook installed")
            }
        }
    }

    private fun shouldFilterByInteractionStats(aweme: Any): Boolean {
        val packageInstance = DouyinPackage.instance

        val statsMinLEMax = hideCollectCountMin <= hideCollectCountMax ||
                hideCommentCountMin <= hideCommentCountMax ||
                hideDiggCountMin <= hideDiggCountMax ||
                hideShareCountMin <= hideShareCountMax
        if (!statsMinLEMax) {
            return false
        }

        val statsObj = aweme.getField<Any?>(packageInstance.aweme.statistics())
            ?: return false

        if (hideCollectCountMin <= hideCollectCountMax) {
            val collectCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.collectCount())
            if (collectCount != null && (collectCount !in hideCollectCountMin..hideCollectCountMax)) {
                YLog.debug("$TAG: filtered by collect count: $collectCount")
                return true
            }
        }

        if (hideCommentCountMin <= hideCommentCountMax) {
            val commentCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.commentCount())
            if (commentCount != null && (commentCount !in hideCommentCountMin..hideCommentCountMax)) {
                YLog.debug("$TAG: filtered by comment count: $commentCount")
                return true
            }
        }

        if (hideDiggCountMin <= hideDiggCountMax) {
            val diggCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.diggCount())
            if (diggCount != null && (diggCount !in hideDiggCountMin..hideDiggCountMax)) {
                YLog.debug("$TAG: filtered by digg count: $diggCount")
                return true
            }
        }

        if (hideShareCountMin <= hideShareCountMax) {
            val shareCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.shareCount())
            if (shareCount != null && (shareCount !in hideShareCountMin..hideShareCountMax)) {
                YLog.debug("$TAG: filtered by share count: $shareCount")
                return true
            }
        }

        return false
    }

    private fun shouldFilterByKeyword(aweme: Any): Boolean {
        val packageInstance = DouyinPackage.instance

        val titleRegexes = kwdFilterTitleRegexes
        if (!titleRegexes.isNullOrEmpty()) {
            val title = aweme.getField<String?>(
                packageInstance.aweme.itemTitle()
            )
            if (!title.isNullOrBlank() && titleRegexes.any {
                    title.contains(it)
                }
            ) {
                YLog.debug("$TAG: filtered by title: $title")
                return true
            }
        }

        val uidFilters = kwdFilterAuthorUid
        if (!uidFilters.isNullOrEmpty()) {
            val authorObj = aweme.getField<Any?>(packageInstance.aweme.author())
            if (authorObj != null) {
                val uid = authorObj.getField<String?>(packageInstance.user.uid())
                if (uid != null && uid in uidFilters) {
                    YLog.debug("$TAG: filtered by author uid: $uid")
                    return true
                }
            }
        }

        val nicknameFilters = kwdFilterAuthorNicknames
        if (!nicknameFilters.isNullOrEmpty()) {
            val authorObj = aweme.getField<Any?>(packageInstance.aweme.author())
            if (authorObj != null) {
                val nickname = authorObj.getField<String?>(packageInstance.user.nickname())
                if (!nickname.isNullOrBlank() && nickname in nicknameFilters) {
                    YLog.debug("$TAG: filtered by author nickname: $nickname")
                    return true
                }
            }
        }

        val descRegexes = kwdFilterDescRegexes
        if (!descRegexes.isNullOrEmpty()) {
            val desc = aweme.getField<String?>(packageInstance.aweme.desc())
            if (!desc.isNullOrBlank() && descRegexes.any {
                    desc.contains(it)
                }
            ) {
                YLog.debug("$TAG: filtered by desc: $desc")
                return true
            }
        }

        return false
    }
}
