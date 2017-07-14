package io.clappr.player.base

import io.clappr.player.BuildConfig
import io.clappr.player.components.MediaOption
import io.clappr.player.components.MediaOptionType
import io.clappr.player.components.Playback
import io.clappr.player.components.PlaybackSupportInterface
import io.clappr.player.playback.NoOpPlayback
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
open class PlaybackTest {

    class SomePlayback(source: String, options: Options = Options()) : Playback(source, null, options) {
        companion object : PlaybackSupportInterface {
            val validSource = "valid-source.mp4"
            override val name = ""

            @JvmStatic
            override fun supportsSource(source: String, mimeType: String?): Boolean {
                return source == validSource
            }
        }

        var playWasCalled = false

        override fun play(): Boolean {
            playWasCalled = true
            return super.play()
        }
    }

    @Before
    fun setup() {
        BaseObject.context = ShadowApplication.getInstance().applicationContext
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowWhenInstantiatingWithInvalidSource() {
        val playback = object : Playback("some-invalid-source.mp4") {}
    }

    @Test
    fun loadCallShouldReturnFalseForUnsupportedSource() {
        val playback = SomePlayback("valid-source.mp4")
        assertFalse("load call should return false for an unsupported source", playback.load(""))
    }

    @Test
    fun loadCallShouldReturnTrueForSupportedSource() {
        val playback = NoOpPlayback("supported-source.mp4")
        assertTrue("load call should return true for a supported source", playback.load(""))
    }

    @Test
    fun shouldStopListeningOnDestroy() {
        val triggerObject = BaseObject()
        val playback = SomePlayback("valid-source.mp4", Options())

        var numberOfTriggers = 0
        playback.listenTo(triggerObject, "playbackTest", Callback.wrap { numberOfTriggers++ })

        triggerObject.trigger("playbackTest")
        assertEquals("no trigger", 1, numberOfTriggers)

        playback.destroy()
        triggerObject.trigger("playbackTest")
        assertEquals("trigger", 1, numberOfTriggers)
    }

    @Test @Ignore
    fun shouldTriggerEventsOnDestroy() {
        val listenObject = BaseObject()
        val playback = SomePlayback("valid-source.mp4", Options())

        var willDestroyCalled = false
        var didDestroyCalled = false
        listenObject.listenTo(playback, InternalEvent.WILL_DESTROY.value, Callback.wrap { willDestroyCalled = true })
        listenObject.listenTo(playback, InternalEvent.DID_DESTROY.value, Callback.wrap { didDestroyCalled = true })

        playback.destroy()

        assertTrue("Will destroy not triggered", willDestroyCalled)
        assertTrue("Did destroy not triggered", didDestroyCalled)
    }

    @Test
    fun shouldReturnAllAvailableMediaOptionSubtitle() {
        checkAvailableMedia(MediaOptionType.SUBTITLE)
    }

    @Test
    fun shouldReturnAllAvailableMediaOptionAudio() {
        checkAvailableMedia(MediaOptionType.AUDIO)
    }

    private fun checkAvailableMedia(mediaOptionType: MediaOptionType) {
        val playback = SomePlayback("valid-source.mp4", Options())
        val random = Random()
        val randomNumber = random.nextInt(10)
        var mediaOptionList = insertMedia(playback, mediaOptionType, randomNumber)

        var addedMediaOptionList = playback.availableMediaOptions(mediaOptionType)
        assertEquals(mediaOptionList.size, addedMediaOptionList!!.size)
        for (i in 0..(randomNumber - 1)) {
            assertEquals(mediaOptionList.get(i), addedMediaOptionList.get(i))
        }
    }

    private fun insertMedia(playback: Playback, mediaOptionType: MediaOptionType, quantity: Int): MutableList<MediaOption> {
        var mediaOptionList: MutableList<MediaOption> = ArrayList()
        for (i in 1..quantity) {
            val mediaOption = MediaOption("Name $i", mediaOptionType, i, null)
            playback.addAvailableMediaOption(mediaOption)
            mediaOptionList.add(mediaOption)
        }

        return mediaOptionList
    }

    @Test
    fun shouldSetSelectedMediaOptionAudio() {
        testSetSelectedMediaOption(MediaOptionType.AUDIO)
    }

    @Test
    fun shouldSetSelectedMediaOptionSubtitle() {
        testSetSelectedMediaOption(MediaOptionType.SUBTITLE)
    }

    private fun testSetSelectedMediaOption(mediaOptionType: MediaOptionType) {
        val playback = SomePlayback("valid-source.mp4", Options())
        val random = Random()
        val randomNumber = random.nextInt(10)
        var mediaOptionList = insertMedia(playback, mediaOptionType, randomNumber)

        mediaOptionList.forEach {
            playback.setSelectedMediaOption(it)
            assertEquals(it, playback.selectedMediaOption(mediaOptionType))
        }
    }

    @Test
    fun shouldResetAvailableMediaOptions() {
        val playback = SomePlayback("valid-source.mp4", Options())
        val random = Random()
        val randomNumber = random.nextInt(10)
        insertMedia(playback, MediaOptionType.SUBTITLE, randomNumber)
        insertMedia(playback, MediaOptionType.AUDIO, random.nextInt(10))
        playback.resetAvailableMediaOptions()

        assertTrue(playback.availableMediaOptions(MediaOptionType.SUBTITLE).isEmpty())
        assertTrue(playback.availableMediaOptions(MediaOptionType.AUDIO).isEmpty())
    }

    @Test
    fun shouldReturnNoOneSelectedMediaOption() {
        val playback = SomePlayback("valid-source.mp4", Options())

        playback.setSelectedMediaOption(MediaOption("Name", MediaOptionType.SUBTITLE, "name", null))
        val mediaSelectedAudio = playback.selectedMediaOption(MediaOptionType.AUDIO)

        assertNull(mediaSelectedAudio)
    }

    @Test
    fun shouldTriggerMediaOptionsUpdateEvents() {
        val playback = SomePlayback("valid-source.mp4", Options())

        val listenObject = BaseObject()

        var mediaOptionsUpdateCalled = false

        listenObject.listenTo(playback, InternalEvent.MEDIA_OPTIONS_UPDATE.value, Callback.wrap { mediaOptionsUpdateCalled = true })

        playback.setSelectedMediaOption(MediaOption("Name", MediaOptionType.SUBTITLE, "name", null))

        assertTrue("Media_Options_Update was not called", mediaOptionsUpdateCalled)
    }
}
