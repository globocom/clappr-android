package io.clappr.player.plugin.Control

import android.support.annotation.Keep
import io.clappr.player.R
import io.clappr.player.base.Callback
import io.clappr.player.base.Event
import io.clappr.player.base.InternalEvent
import io.clappr.player.base.NamedType
import io.clappr.player.components.Core
import io.clappr.player.components.MediaOptionType
import io.clappr.player.components.Playback

@Keep
open class SettingButtonPlugin(core: Core) : ButtonPlugin(core) {

    @Keep
    companion object : NamedType {
        override val name = "settingButton"
    }

    override val resourceDrawable: Int
        get() = R.drawable.ic_settings

    override val idResourceDrawable: Int
        get() = R.id.setting_button

    override val resourceLayout: Int
        get() = R.layout.bottom_panel_button_plugin

    override var panel: Panel = Panel.BOTTOM
    override var position: Position = Position.RIGHT

    init {
        core.on(InternalEvent.DID_CHANGE_ACTIVE_CONTAINER.value, Callback.wrap { bindEvents() })
        core.on(InternalEvent.DID_OPEN_MODAL_PANEL.value, Callback.wrap { onModalPanelOpened() })
    }

    fun bindEvents() {
        hide()
        stopListening()
        handleMediaOptions()
        bindActiveContainerEvents()
        bindPlaybackEvents()
    }

    private fun bindActiveContainerEvents() {
        core.activeContainer?.let {
            listenTo(it, InternalEvent.DID_CHANGE_PLAYBACK.value, Callback.wrap { bindEvents() })
        }
    }

    private fun bindPlaybackEvents() {
        core.activePlayback?.let {
            listenTo(it, InternalEvent.MEDIA_OPTIONS_READY.value, Callback.wrap { handleMediaOptions() })
            listenTo(it, Event.DID_COMPLETE.value, Callback.wrap { hide() })
        }
    }

    override fun onClick() {
        core.trigger(InternalEvent.DID_TOUCH_MEDIA_CONTROL.value)
        core.trigger(InternalEvent.OPEN_MODAL_PANEL.value)

    }

    private fun handleMediaOptions() {
        core.activePlayback?.let {
            // TODO remove VOD verification when DVR be supported
            if (hasMoreThanOneMediaOptionAvailable(it) && it.mediaType == Playback.MediaType.VOD) {
                show()
            }
        }
    }

    private fun hasMoreThanOneMediaOptionAvailable(playback: Playback): Boolean {
        MediaOptionType.values().forEach { mediaOption ->
            if (playback.availableMediaOptions(mediaOption).size > 1)
                return true
        }
        return false
    }

    private fun onModalPanelOpened() {
        core.activeContainer?.playback?.let {
            if (it.canPause)
                it.pause()
        }
    }

    override fun render() {
        super.render()
    }
}