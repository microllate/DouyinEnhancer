package io.github.twyora.douyinenhancer.hook.ui

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.PlaybackComponentBlockKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedComponentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    private val blockComponentIds by lazy {
        listOf(
            PlaybackComponentBlockKey.MUSIC_COVER_BLOCK to packageInstance.fluxComponentId.musicCoverBlock(),
            PlaybackComponentBlockKey.MUSIC_LISTEN_COVER to packageInstance.fluxComponentId.musicListenCover(),
            PlaybackComponentBlockKey.DIGG to packageInstance.fluxComponentId.digg(),
            PlaybackComponentBlockKey.TITLE to packageInstance.fluxComponentId.title(),
            PlaybackComponentBlockKey.MUSIC_COVER to packageInstance.fluxComponentId.musicCover(),
            PlaybackComponentBlockKey.GENERAL_LABEL to packageInstance.fluxComponentId.generalLabel(),
            PlaybackComponentBlockKey.FEED_LABEL_CONTAINER to packageInstance.fluxComponentId.feedLabelContainer(),
            PlaybackComponentBlockKey.MUSIC_TITLE to packageInstance.fluxComponentId.musicTitle(),
            PlaybackComponentBlockKey.STORY_25_DIVERSE_DIGG to packageInstance.fluxComponentId.story25DiverseDigg(),
            PlaybackComponentBlockKey.ECOM_STORE to packageInstance.fluxComponentId.ecomStore(),
            PlaybackComponentBlockKey.BUTTON_IM_QUICK_SHARE to packageInstance.fluxComponentId.buttonImQuickShare(),
            PlaybackComponentBlockKey.BUTTON_FEED_IM_SHARE_GUIDE_V2 to packageInstance.fluxComponentId.buttonFeedImShareGuideV2(),
            PlaybackComponentBlockKey.BUTTON_FORCE_FEED_IM_SHARE_GUIDE to packageInstance.fluxComponentId.buttonForceFeedImShareGuide(),
            PlaybackComponentBlockKey.SOCIAL_NEW_COMMENT_GUIDE_BUBBLE to packageInstance.fluxComponentId.socialNewCommentGuideBubble(),
            PlaybackComponentBlockKey.COMMENT_BOTTOM_ANIMATION to packageInstance.fluxComponentId.commentBottomAnimation(),
            PlaybackComponentBlockKey.NEARBY_IDENTITY_TAG to packageInstance.fluxComponentId.nearbyIdentityTag(),
            PlaybackComponentBlockKey.LIVE_PHOTO_TAG to packageInstance.fluxComponentId.livePhotoTag(),
            PlaybackComponentBlockKey.PHOTOS_TAG to packageInstance.fluxComponentId.photosTag(),
            PlaybackComponentBlockKey.STORY24_TAG to packageInstance.fluxComponentId.story24Tag(),
            PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_STORY_TAG to packageInstance.fluxComponentId.socialNewStyleStoryTag(),
            PlaybackComponentBlockKey.LONG_VIDEO_HIGHLIGHT_TAG to packageInstance.fluxComponentId.longVideoHighlightTag(),
            PlaybackComponentBlockKey.DANMAKU_VERTICAL to packageInstance.fluxComponentId.danmakuVertical(),
            PlaybackComponentBlockKey.AVATAR to packageInstance.fluxComponentId.avatar(),
            PlaybackComponentBlockKey.NICKNAME to packageInstance.fluxComponentId.nickname(),
            PlaybackComponentBlockKey.POST_TIME to packageInstance.fluxComponentId.postTime(),
            PlaybackComponentBlockKey.BELLOW_DESC_TIME to packageInstance.fluxComponentId.bellowDescTime(),
            PlaybackComponentBlockKey.COMMENT to packageInstance.fluxComponentId.comment(),
            PlaybackComponentBlockKey.REPLY to packageInstance.fluxComponentId.reply(),
            PlaybackComponentBlockKey.SHARE to packageInstance.fluxComponentId.share(),
            PlaybackComponentBlockKey.COLLECT to packageInstance.fluxComponentId.collect(),
            PlaybackComponentBlockKey.ANCHOR_FRAMEWORK to packageInstance.fluxComponentId.anchorFramework(),
            PlaybackComponentBlockKey.BOTTOM_BAR_COMMON to packageInstance.fluxComponentId.bottomBarCommon(),
            PlaybackComponentBlockKey.COMMON_BUTTON to packageInstance.fluxComponentId.commonButton(),
            PlaybackComponentBlockKey.STICKER to packageInstance.fluxComponentId.sticker(),
            PlaybackComponentBlockKey.AI_SEARCH to packageInstance.fluxComponentId.aiSearch(),
            PlaybackComponentBlockKey.C2_FEED to packageInstance.fluxComponentId.c2Feed(),
            PlaybackComponentBlockKey.FLOW to packageInstance.fluxComponentId.flow(),
            PlaybackComponentBlockKey.NEARBY_HOT_COMMENT to packageInstance.fluxComponentId.nearbyHotComment(),
            PlaybackComponentBlockKey.BUTTON_UNFOLLOW_FAMILIAR to packageInstance.fluxComponentId.buttonUnfollowFamiliar(),
            PlaybackComponentBlockKey.BUTTON_UNFOLLOW_FAMILIAR_REC to packageInstance.fluxComponentId.buttonUnfollowFamiliarRec(),
            PlaybackComponentBlockKey.CO_CREATOR_AUTHOR to packageInstance.fluxComponentId.coCreatorAuthor(),
            PlaybackComponentBlockKey.CHAPTER_TAG to packageInstance.fluxComponentId.chapterTag(),
            PlaybackComponentBlockKey.ECOM_TAG_FRIEND to packageInstance.fluxComponentId.ecomTagFriend(),
            PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_POST_TIME_BOTTOM to packageInstance.fluxComponentId.socialNewStylePostTimeBottom(),
            PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_MUSIC_BELOW to packageInstance.fluxComponentId.socialNewStyleMusicBelow(),
            PlaybackComponentBlockKey.CHAPTER_DETAIL to packageInstance.fluxComponentId.chapterDetail(),
            PlaybackComponentBlockKey.TITLE_TAG_CONTAINER to packageInstance.fluxComponentId.titleTagContainer(),
            PlaybackComponentBlockKey.RIGHT_MENU_LL to packageInstance.fluxComponentId.rightMenuLl(),
            PlaybackComponentBlockKey.MUSIC_MUTE_COVER to packageInstance.fluxComponentId.musicMuteCover(),
            PlaybackComponentBlockKey.JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG to packageInstance.fluxComponentId.jxLeftBottomLongVideoPlusTitleTag(),
            PlaybackComponentBlockKey.BOTTOM_BAR_MIX to packageInstance.fluxComponentId.bottomBarMix(),
            PlaybackComponentBlockKey.BOTTOM_BAR_NORMAL_SEARCH to packageInstance.fluxComponentId.bottomBarNormalSearch(),
            PlaybackComponentBlockKey.BOTTOM_BAR_COMMON_PRIORITY_SEARCH to packageInstance.fluxComponentId.bottomBarCommonPrioritySearch(),
            PlaybackComponentBlockKey.JX_PICK to packageInstance.fluxComponentId.jxPick(),
            PlaybackComponentBlockKey.BOTTOM_BAR_CONTAINER to packageInstance.fluxComponentId.bottomBarContainer(),
            PlaybackComponentBlockKey.AI_CO_CREATORS_THREE to packageInstance.fluxComponentId.aiCoCreatorsThree(),
            PlaybackComponentBlockKey.AIGC_COCREATE_STATUS_TITLE to packageInstance.fluxComponentId.aigcCocreateStatusTitle()
        ).filter {
            FastKVConfigManager.settings.getBoolean(it.first, false)
        }.map {
            it.second
        }.toSet()
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
