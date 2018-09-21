package io.clappr.player.plugin.Control

import io.clappr.player.BuildConfig
import io.clappr.player.base.*
import io.clappr.player.components.*
import io.clappr.player.plugin.Loader
import io.clappr.player.plugin.UIPlugin
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [23])
class SettingButtonPluginTest {

    private lateinit var settingButtonPlugin: SettingButtonPlugin
    private lateinit var core: Core
    private lateinit var container: Container
    private lateinit var fakePlayback: FakePlayback

    @Before
    fun setUp() {
        BaseObject.context = ShadowApplication.getInstance().applicationContext
        container = Container(Loader(), Options())
        core = Core(Loader(), Options())

        settingButtonPlugin = SettingButtonPlugin(core)

        //In this case the order matters due we must to load the video and find out witch playback
        //will be observed, after initialize SettingButtonPlugin.
        //But, it is manually attached on container, for easiness.
        fakePlayback = FakePlayback()
        container.playback = fakePlayback
        core.activeContainer = container
    }

    @Test
    fun shouldOpenSettingsWhenSettingsButtonsWasClicked() {
        var didTouchMediaControlEventWasCalled = false
        var openSettingsEventWasCalled = false

        core.on(InternalEvent.DID_TOUCH_MEDIA_CONTROL.value, Callback.wrap { didTouchMediaControlEventWasCalled = true })
        core.on(InternalEvent.OPEN_MODAL_PANEL.value, Callback.wrap { openSettingsEventWasCalled = true })

        settingButtonPlugin.onClick()

        assertTrue(didTouchMediaControlEventWasCalled, "media control not touched")
        assertTrue(openSettingsEventWasCalled, "modal panel not opened")
    }

    @Test
    fun shouldBeHiddenWhenRendered() {
        settingButtonPlugin.render()

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeVisibleOnMediaOptionReadyWithOptions() {
        addOptions(fakePlayback, Playback.MediaType.VOD)
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        assertEquals(UIPlugin.Visibility.VISIBLE, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenOnMediaOptionReadyWithOptionsAndLive() {
        addOptions(fakePlayback, Playback.MediaType.LIVE)
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenOnMediaOptionReadyWithOnlyOneOption() {
        fakePlayback.internalMediaType = Playback.MediaType.VOD
        fakePlayback.addAvailableMediaOption(MediaOption("Português", MediaOptionType.AUDIO, null, null))
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenOnMediaOptionReadyWithoutOptions() {
        fakePlayback.internalMediaType = Playback.MediaType.VOD
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenOnMediaOptionReadyWithoutOptionsAndLive() {
        fakePlayback.internalMediaType = Playback.MediaType.LIVE
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenWhenVideoFinishes() {
        core.activePlayback?.trigger(Event.DID_COMPLETE.value)

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenWhenActivePlaybackChangesWithoutOptions() {
        addOptions(fakePlayback, Playback.MediaType.VOD)
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        core.activeContainer?.playback = FakePlayback()

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    private fun addOptions(playback: FakePlayback, mediaType: Playback.MediaType) {
        playback.internalMediaType = mediaType
        playback.addAvailableMediaOption(MediaOption("Inglês", MediaOptionType.AUDIO, null, null))
        playback.addAvailableMediaOption(MediaOption("Português", MediaOptionType.AUDIO, null, null))
    }

    @Test
    fun shouldBeVisibleWhenActivePlaybackChangesWithOptions() {
        fakePlayback.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)

        val newPlayback = FakePlayback()
        addOptions(newPlayback, Playback.MediaType.VOD)
        core.activeContainer?.playback = newPlayback

        assertEquals(UIPlugin.Visibility.VISIBLE, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldBeHiddenWhenActiveContainerChanges() {
        core.activePlayback?.trigger(io.clappr.player.base.InternalEvent.MEDIA_OPTIONS_READY.value)
        core.activeContainer = Container(Loader(), Options())

        assertEquals(UIPlugin.Visibility.HIDDEN, settingButtonPlugin.visibility)
    }

    @Test
    fun shouldStopListeningActivePlaybackWhenItChange() {
        var numberOfTriggeredEvent = 0

        //Make SettingButtonPlugin listen any event from actual playback
        settingButtonPlugin.core.activePlayback?.let {
            settingButtonPlugin.listenTo(it, "aEvent", Callback.wrap { numberOfTriggeredEvent++ })
        }

        //Make playback trigger a event and increment numberOfTriggeredEvent
        settingButtonPlugin.core.activePlayback?.trigger("aEvent")

        val oldPlayback = settingButtonPlugin.core.activeContainer?.playback

        //Change playback
        settingButtonPlugin.core.activeContainer?.playback = FakePlayback()

        //Trigger a event from old playback. If SettingButtonPlugin still listening the old playback,
        //numberOfTriggeredEvent will count 2
        oldPlayback?.trigger("aEvent")

        assertEquals(1, numberOfTriggeredEvent)
    }

    @Test
    fun shouldPausePlaybackWhenCoreTriggeredDidOpenModalPanelEventAndPlaybackCanPause() {
        fakePlayback.internalCanPause = true

        core.trigger(InternalEvent.DID_OPEN_MODAL_PANEL.value)

        assertEquals(Playback.State.PAUSED, core.activePlayback?.state)
    }

    @Test
    fun shouldKeepPlaybackStateWhenCoreTriggeredDidOpenModalPanelEventAndPlaybackCanNotPause() {
        fakePlayback.internalCanPause = false
        fakePlayback.internalState = Playback.State.NONE

        core.trigger(InternalEvent.DID_OPEN_MODAL_PANEL.value)

        assertEquals(Playback.State.NONE, core.activePlayback?.state)
    }

    class FakePlayback(source: String = "aSource", mimeType: String? = null, options: Options = Options()) : Playback(source, mimeType, options) {
        companion object : PlaybackSupportInterface {
            override val name: String = "fakePlayback"

            override fun supportsSource(source: String, mimeType: String?): Boolean {
                return true
            }
        }

        var internalState = State.NONE
        var internalCanPause = false
        var internalMediaType = MediaType.UNKNOWN

        override val state: State
            get() = internalState

        override val canPause: Boolean
            get() = internalCanPause

        override val canPlay: Boolean
            get() = true

        override val mediaType: MediaType
            get() = internalMediaType

        override fun play(): Boolean {
            internalState = State.PLAYING
            return true
        }

        override fun pause(): Boolean {
            internalState = State.PAUSED
            return true
        }
    }
}