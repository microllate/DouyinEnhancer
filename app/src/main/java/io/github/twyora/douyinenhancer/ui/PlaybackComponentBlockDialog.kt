package io.github.twyora.douyinenhancer.ui

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.core.content.edit
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.PlaybackComponentBlockKey
import io.github.twyora.douyinenhancer.databinding.PlaybackComponentBlockDialogBinding

class PlaybackComponentBlockDialog(context: Context) : AlertDialog.Builder(ContextThemeWrapper(context, R.style.MainTheme)) {
    init {
        val playbackComponentBlockDialogBinding = PlaybackComponentBlockDialogBinding.inflate(
            LayoutInflater.from(ContextThemeWrapper(context, R.style.MainTheme))
        )
        val prefs = FastKVConfigManager.settings

        // a StringSet of enum names would be cleaner, but it would bypass the mapping DouyinPackage provides
        playbackComponentBlockDialogBinding.switchMainSwitch.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.MAIN_SWITCH, false)
        playbackComponentBlockDialogBinding.musicCoverBlock.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.MUSIC_COVER_BLOCK, false)
        playbackComponentBlockDialogBinding.musicListenCover.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.MUSIC_LISTEN_COVER, false)
        playbackComponentBlockDialogBinding.digg.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.DIGG, false)
        playbackComponentBlockDialogBinding.title.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.TITLE, false)
        playbackComponentBlockDialogBinding.musicCover.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.MUSIC_COVER, false)
        playbackComponentBlockDialogBinding.generalLabel.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.GENERAL_LABEL, false)
        playbackComponentBlockDialogBinding.feedLabelContainer.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.FEED_LABEL_CONTAINER, false)
        playbackComponentBlockDialogBinding.musicTitle.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.MUSIC_TITLE, false)
        playbackComponentBlockDialogBinding.story25DiverseDigg.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.STORY_25_DIVERSE_DIGG, false)
        playbackComponentBlockDialogBinding.ecomStore.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.ECOM_STORE, false)
        playbackComponentBlockDialogBinding.buttonImQuickShare.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.BUTTON_IM_QUICK_SHARE, false)
        playbackComponentBlockDialogBinding.buttonFeedImShareGuideV2.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.BUTTON_FEED_IM_SHARE_GUIDE_V2, false)
        playbackComponentBlockDialogBinding.buttonForceFeedImShareGuide.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.BUTTON_FORCE_FEED_IM_SHARE_GUIDE, false)
        playbackComponentBlockDialogBinding.socialNewCommentGuideBubble.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.SOCIAL_NEW_COMMENT_GUIDE_BUBBLE, false)
        playbackComponentBlockDialogBinding.commentBottomAnimation.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.COMMENT_BOTTOM_ANIMATION, false)
        playbackComponentBlockDialogBinding.nearbyIdentityTag.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.NEARBY_IDENTITY_TAG, false)
        playbackComponentBlockDialogBinding.livePhotoTag.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.LIVE_PHOTO_TAG, false)
        playbackComponentBlockDialogBinding.photosTag.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.PHOTOS_TAG, false)
        playbackComponentBlockDialogBinding.story24Tag.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.STORY24_TAG, false)
        playbackComponentBlockDialogBinding.socialNewStyleStoryTag.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_STORY_TAG, false)
        playbackComponentBlockDialogBinding.longVideoHighlightTag.isChecked =
            prefs.getBoolean(PlaybackComponentBlockKey.LONG_VIDEO_HIGHLIGHT_TAG, false)
        playbackComponentBlockDialogBinding.danmakuVertical.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.DANMAKU_VERTICAL, false)
        playbackComponentBlockDialogBinding.avatar.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.AVATAR, false)
        playbackComponentBlockDialogBinding.nickname.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.NICKNAME, false)
        playbackComponentBlockDialogBinding.postTime.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.POST_TIME, false)
        playbackComponentBlockDialogBinding.bellowDescTime.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.BELLOW_DESC_TIME, false)
        playbackComponentBlockDialogBinding.comment.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.COMMENT, false)
        playbackComponentBlockDialogBinding.reply.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.REPLY, false)
        playbackComponentBlockDialogBinding.share.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.SHARE, false)
        playbackComponentBlockDialogBinding.collect.isChecked = prefs.getBoolean(PlaybackComponentBlockKey.COLLECT, false)

        setView(playbackComponentBlockDialogBinding.root)
        setTitle(context.getString(R.string.playback_component_block_dialog_title))
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.edit(commit = true) {
                putBoolean(PlaybackComponentBlockKey.MAIN_SWITCH, playbackComponentBlockDialogBinding.switchMainSwitch.isChecked)
                putBoolean(PlaybackComponentBlockKey.MUSIC_COVER_BLOCK, playbackComponentBlockDialogBinding.musicCoverBlock.isChecked)
                putBoolean(PlaybackComponentBlockKey.MUSIC_LISTEN_COVER, playbackComponentBlockDialogBinding.musicListenCover.isChecked)
                putBoolean(PlaybackComponentBlockKey.DIGG, playbackComponentBlockDialogBinding.digg.isChecked)
                putBoolean(PlaybackComponentBlockKey.TITLE, playbackComponentBlockDialogBinding.title.isChecked)
                putBoolean(PlaybackComponentBlockKey.MUSIC_COVER, playbackComponentBlockDialogBinding.musicCover.isChecked)
                putBoolean(PlaybackComponentBlockKey.GENERAL_LABEL, playbackComponentBlockDialogBinding.generalLabel.isChecked)
                putBoolean(PlaybackComponentBlockKey.FEED_LABEL_CONTAINER, playbackComponentBlockDialogBinding.feedLabelContainer.isChecked)
                putBoolean(PlaybackComponentBlockKey.MUSIC_TITLE, playbackComponentBlockDialogBinding.musicTitle.isChecked)
                putBoolean(
                    PlaybackComponentBlockKey.STORY_25_DIVERSE_DIGG,
                    playbackComponentBlockDialogBinding.story25DiverseDigg.isChecked
                )
                putBoolean(PlaybackComponentBlockKey.ECOM_STORE, playbackComponentBlockDialogBinding.ecomStore.isChecked)
                putBoolean(
                    PlaybackComponentBlockKey.BUTTON_IM_QUICK_SHARE,
                    playbackComponentBlockDialogBinding.buttonImQuickShare.isChecked
                )
                putBoolean(
                    PlaybackComponentBlockKey.BUTTON_FEED_IM_SHARE_GUIDE_V2,
                    playbackComponentBlockDialogBinding.buttonFeedImShareGuideV2.isChecked
                )
                putBoolean(
                    PlaybackComponentBlockKey.BUTTON_FORCE_FEED_IM_SHARE_GUIDE,
                    playbackComponentBlockDialogBinding.buttonForceFeedImShareGuide.isChecked
                )
                putBoolean(
                    PlaybackComponentBlockKey.SOCIAL_NEW_COMMENT_GUIDE_BUBBLE,
                    playbackComponentBlockDialogBinding.socialNewCommentGuideBubble.isChecked
                )
                putBoolean(
                    PlaybackComponentBlockKey.COMMENT_BOTTOM_ANIMATION,
                    playbackComponentBlockDialogBinding.commentBottomAnimation.isChecked
                )
                putBoolean(PlaybackComponentBlockKey.NEARBY_IDENTITY_TAG, playbackComponentBlockDialogBinding.nearbyIdentityTag.isChecked)
                putBoolean(PlaybackComponentBlockKey.LIVE_PHOTO_TAG, playbackComponentBlockDialogBinding.livePhotoTag.isChecked)
                putBoolean(PlaybackComponentBlockKey.PHOTOS_TAG, playbackComponentBlockDialogBinding.photosTag.isChecked)
                putBoolean(PlaybackComponentBlockKey.STORY24_TAG, playbackComponentBlockDialogBinding.story24Tag.isChecked)
                putBoolean(
                    PlaybackComponentBlockKey.SOCIAL_NEW_STYLE_STORY_TAG,
                    playbackComponentBlockDialogBinding.socialNewStyleStoryTag.isChecked
                )
                putBoolean(
                    PlaybackComponentBlockKey.LONG_VIDEO_HIGHLIGHT_TAG,
                    playbackComponentBlockDialogBinding.longVideoHighlightTag.isChecked
                )
                putBoolean(PlaybackComponentBlockKey.DANMAKU_VERTICAL, playbackComponentBlockDialogBinding.danmakuVertical.isChecked)
                putBoolean(PlaybackComponentBlockKey.AVATAR, playbackComponentBlockDialogBinding.avatar.isChecked)
                putBoolean(PlaybackComponentBlockKey.NICKNAME, playbackComponentBlockDialogBinding.nickname.isChecked)
                putBoolean(PlaybackComponentBlockKey.POST_TIME, playbackComponentBlockDialogBinding.postTime.isChecked)
                putBoolean(PlaybackComponentBlockKey.BELLOW_DESC_TIME, playbackComponentBlockDialogBinding.bellowDescTime.isChecked)
                putBoolean(PlaybackComponentBlockKey.COMMENT, playbackComponentBlockDialogBinding.comment.isChecked)
                putBoolean(PlaybackComponentBlockKey.REPLY, playbackComponentBlockDialogBinding.reply.isChecked)
                putBoolean(PlaybackComponentBlockKey.SHARE, playbackComponentBlockDialogBinding.share.isChecked)
                putBoolean(PlaybackComponentBlockKey.COLLECT, playbackComponentBlockDialogBinding.collect.isChecked)
            }
        }
    }
}
