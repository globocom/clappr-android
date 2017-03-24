package io.clappr.player.base

class Options(
    var source: String? = null,
    var mimeType: String? = null,
    var autoPlay: Boolean = true,
    val extraOptions: MutableMap<String, Any> = mutableMapOf<String, Any>())

enum class ClapprOption(val value: String) {
    /**
     * Media start position
     */
    START_AT("startAt")
}
