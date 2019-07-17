package io.clappr.player.base.keys

enum class Key(val value: String) {
    UNDEFINED("undefined"),
    PLAY("play"),
    PAUSE("pause"),
    STOP("strop");

    companion object {
        fun getByValue(value: String) = values().firstOrNull { it.value == value }?.let { it }
    }
}