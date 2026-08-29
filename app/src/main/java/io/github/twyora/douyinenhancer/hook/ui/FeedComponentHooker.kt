package io.github.twyora.douyinenhancer.hook.ui

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.MiscKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.PlaybackComponentBlockKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedComponentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    private val hiddenFeaturesEnabled
        get() = FastKVConfigManager.settings.getBoolean(MiscKey.ENABLE_HIDDEN_FEATURES, false)

    private val blockComponentIds by lazy {
        val blockComponentIdsTemp = mutableListOf<Field>()
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.MUSIC_COVER_BLOCK, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.musicCoverBlock()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.MUSIC_LISTEN_COVER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.musicListenCover()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.DIGG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.digg()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.TITLE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.title()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.MUSIC_COVER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.musicCover()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.GENERAL_LABEL, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.generalLabel()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.FEED_LABEL_CONTAINER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.feedLabelContainer()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.MUSIC_TITLE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.musicTitle()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.STORY_25_DIVERSE_DIGG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.story25DiverseDigg()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.ECOM_STORE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.ecomStore()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BUTTON_IM_QUICK_SHARE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.buttonImQuickShare()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BUTTON_FEED_IM_SHARE_GUIDE_V2, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.buttonFeedImShareGuideV2()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BUTTON_FORCE_FEED_IM_SHARE_GUIDE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.buttonForceFeedImShareGuide()
            )
        }
        if (hiddenFeaturesEnabled &&
            FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.SOCIAL_NEW_COMMENT_GUIDE_BUBBLE, false)
        ) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.socialNewCommentGuideBubble()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.COMMENT_BOTTOM_ANIMATION, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.commentBottomAnimation()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.NEARBY_IDENTITY_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.nearbyIdentityTag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.LIVE_PHOTO_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.livePhotoTag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.PHOTOS_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.photosTag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.STORY24_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.story24Tag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_STORY_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.socialNewStyleStoryTag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.LONG_VIDEO_HIGHLIGHT_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.longVideoHighlightTag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.DANMAKU_VERTICAL, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.danmakuVertical()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.AVATAR, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.avatar()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.NICKNAME, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.nickname()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.POST_TIME, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.postTime()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BELLOW_DESC_TIME, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.bellowDescTime()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.COMMENT, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.comment()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.REPLY, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.reply()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.SHARE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.share()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.COLLECT, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.collect()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.ANCHOR_FRAMEWORK, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.anchorFramework()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BOTTOM_BAR_COMMON, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.bottomBarCommon()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.COMMON_BUTTON, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.commonButton()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.STICKER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.sticker()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.AI_SEARCH, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.aiSearch()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.C2_FEED, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.c2Feed()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.FLOW, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.flow()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.NEARBY_HOT_COMMENT, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.nearbyHotComment()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BUTTON_UNFOLLOW_FAMILIAR, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.buttonUnfollowFamiliar()
            )
        }
        if (hiddenFeaturesEnabled &&
            FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BUTTON_UNFOLLOW_FAMILIAR_REC, false)
        ) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.buttonUnfollowFamiliarRec()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.CO_CREATOR_AUTHOR, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.coCreatorAuthor()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.CHAPTER_TAG, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.chapterTag()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.ECOM_TAG_FRIEND, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.ecomTagFriend()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_POST_TIME_BOTTOM, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.socialNewStylePostTimeBottom()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_MUSIC_BELOW, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.socialNewStyleMusicBelow()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.CHAPTER_DETAIL, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.chapterDetail()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.TITLE_TAG_CONTAINER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.titleTagContainer()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.RIGHT_MENU_LL, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.rightMenuLl()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.MUSIC_MUTE_COVER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.musicMuteCover()
            )
        }
        if (hiddenFeaturesEnabled &&
            FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG, false)
        ) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.jxLeftBottomLongVideoPlusTitleTag()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BOTTOM_BAR_MIX, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.bottomBarMix()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BOTTOM_BAR_NORMAL_SEARCH, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.bottomBarNormalSearch()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BOTTOM_BAR_COMMON_PRIORITY_SEARCH, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.bottomBarCommonPrioritySearch()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.JX_PICK, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.jxPick()
            )
        }
        if (FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.BOTTOM_BAR_CONTAINER, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.bottomBarContainer()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.AI_CO_CREATORS_THREE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.aiCoCreatorsThree()
            )
        }
        if (hiddenFeaturesEnabled && FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.AIGC_COCREATE_STATUS_TITLE, false)) {
            blockComponentIdsTemp.add(
                packageInstance.fluxComponentId.aigcCocreateStatusTitle()
            )
        }
        blockComponentIdsTemp.toSet()
    }

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(PlaybackComponentBlockKey.MAIN_SWITCH, false)) {
            if (verbose) {
                YLog.debug("$TAG: playback component block is disabled, skipping hook")
            }
            return
        }
        installPlaybackComponentBlockHook()
    }

    private fun installPlaybackComponentBlockHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.fluxComponentDataAction.selfClass?.resolveMethod(
            packageInstance.fluxComponentDataAction.getSet()
        )?.hook {
            after {
                val allowComponentSet = result as? MutableSet<*> ?: run {
                    YLog.error("$TAG: ${result?.javaClass?.name} is not a mutable set")
                    return@after
                }
                allowComponentSet.removeAll(
                    blockComponentIds.mapNotNull { field ->
                        packageInstance.fluxComponentId.selfClass?.getStaticField(field)
                    }.toSet()
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to block playback components", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for blocking playback components", throwable)
            }
        }
    }
}
