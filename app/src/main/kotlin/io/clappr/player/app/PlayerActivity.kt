package io.clappr.player.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import io.clappr.player.Player
import io.clappr.player.base.Callback
import io.clappr.player.base.ErrorInfo
import io.clappr.player.base.Event
import io.clappr.player.base.Options
import io.clappr.player.log.Logger
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


class PlayerActivity : Activity() {

    lateinit var player: Player
    lateinit var mediaUrl: EditText
    lateinit var playerState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        mediaUrl = findViewById(R.id.url_text) as EditText
        playerState = findViewById(R.id.player_state) as TextView

        player = Player()

        updatePlayerState()

        Event.values().forEach { player.on(it.value, Callback.wrap { runOnUiThread { updatePlayerState() } }) }

        player.on(Event.BUFFER_UPDATE.value, Callback.wrap { Logger.info("Buffer update: " + it?.getDouble("percentage"), "App") })

        player.on(Event.ERROR.value, Callback.wrap {
            val errorInfo : ErrorInfo? = it?.getParcelable(Event.ERROR.value)
            Logger.info("Error: " + errorInfo?.code + " \"" + errorInfo?.message + "\"", "App")
            errorInfo?.let {
                AlertDialog.Builder(player.activity)
                        .setMessage("Code: " + it.code + "\nMessage: \"" + it.message + "\"")
                        .setTitle(R.string.error_dialog_title)
                        .create()
                        .show()
            }
        })
    }

    fun updatePlayerState() {
        playerState.text =
                when (player.state) {
                    Player.State.NONE -> "None"
                    Player.State.PLAYING -> "Playing"
                    Player.State.PAUSED -> "Paused"
                    Player.State.ERROR -> "Error"
                    Player.State.IDLE -> "Idle"
                    Player.State.STALLED -> "Stalled"
                }
    }

    fun playMedia(view: View) {
        val url = mediaUrl.text.toString()


        if (url.isNotEmpty()) {
            hideKeyboard()

            if (fragmentManager.findFragmentByTag("clappr_player") == null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.add(R.id.container, player, "clappr_player")
                fragmentTransaction.commit()

                player.configure(Options(source = url, autoPlay = false))
            } else {
                player.load(url)
            }

            player.play()
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0)
    }
}
