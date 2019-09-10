package io.clappr.player.playback

import com.google.android.exoplayer2.C.TRACK_TYPE_AUDIO
import com.google.android.exoplayer2.C.TRACK_TYPE_TEXT
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import io.clappr.player.components.AudioLanguage
import io.clappr.player.components.SubtitleLanguage
import java.util.*

private val audioStandardMap = mapOf(
    AudioLanguage.ORIGINAL.value to listOf("original", "und"),
    AudioLanguage.PORTUGUESE.value to listOf("pt", "por"),
    AudioLanguage.ENGLISH.value to listOf("en", "eng")
)

private val subtitleStandardMap = mapOf(
    SubtitleLanguage.OFF.value to listOf("", "off"),
    SubtitleLanguage.PORTUGUESE.value to listOf("pt", "por")
)

val Player.selectedAudio: String
    get() {
        val selectedFormat = currentTrackSelections.get(TRACK_TYPE_AUDIO)?.selectedFormat
            ?: return AudioLanguage.UNSET.value

        return selectedFormat.language.toStandardAudioLanguage()
    }

val Player.selectedSubtitle: String
    get() = currentTrackSelections.get(TRACK_TYPE_TEXT)?.selectedFormat?.language.toStandardSubtitleLanguage()

private fun MappingTrackSelector.tracks(): List<TrackInfo> {

    val tracks = mutableListOf<TrackInfo>()

    val mappedTrackInfo = currentMappedTrackInfo ?: return tracks

    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {

        val rendererType = mappedTrackInfo.getRendererType(rendererIndex)
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)

        for (trackGroupIndex in 0 until trackGroupArray.length) {

            val trackGroup = trackGroupArray.get(trackGroupIndex)

            for (formatIndex in 0 until trackGroup.length) {

                val format = trackGroup.getFormat(formatIndex)

                val standardLanguage = when (rendererType) {
                    TRACK_TYPE_AUDIO -> format.language.toStandardAudioLanguage()
                    TRACK_TYPE_TEXT -> format.language.toStandardSubtitleLanguage()
                    else -> format.language.orEmpty()
                }

                tracks += TrackInfo(
                    rendererType,
                    rendererIndex,
                    trackGroupIndex,
                    formatIndex,
                    standardLanguage
                )
            }
        }
    }

    return tracks
}

fun MappingTrackSelector.audioTracks() = tracks().filter { it.rendererType == TRACK_TYPE_AUDIO }

fun MappingTrackSelector.subtitleTracks() = tracks().filter { it.rendererType == TRACK_TYPE_TEXT }

private fun String?.toStandardAudioLanguage() = audioStandardMap.entries
    .firstOrNull { this?.toLowerCase(Locale.getDefault()) in it.value }?.key
    ?: this?.toLowerCase(Locale.getDefault())
    ?: AudioLanguage.ORIGINAL.value

private fun String?.toStandardSubtitleLanguage() = subtitleStandardMap.entries
    .firstOrNull { this?.toLowerCase(Locale.getDefault()) in it.value }?.key
    ?: this?.toLowerCase(Locale.getDefault())
    ?: SubtitleLanguage.OFF.value

data class TrackInfo(
    val rendererType: Int,
    val rendererIndex: Int,
    val trackGroupIndex: Int,
    val formatIndex: Int,
    val language: String
)